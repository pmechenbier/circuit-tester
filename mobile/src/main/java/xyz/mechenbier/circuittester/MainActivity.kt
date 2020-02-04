package xyz.mechenbier.circuittester

import android.app.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {

    private lateinit var mService: PowerStateService
    private var mBound = false
    private var mUiUpdateReceiver: UiUpdateReceiver? = null
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private val mRatingDaysUntilPrompt = 3
    private val mRatingLaunchesUntilPrompt = 3

    companion object {
        lateinit var mInstance: MainActivity
    }

    //Defines callbacks for service binding, passed to bindService()
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as PowerStateService.LocalBinder
            mService = binder.getService()
            mBound = true

            val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val prefValueSoundWhenPowered: Boolean = preferences.getBoolean(KEY_PREF_SOUND_WHEN_POWERED, false)
            val prefValueSoundMute: Boolean = preferences.getBoolean(KEY_PREF_SOUND_MUTE, false)

            mService.setSoundOnPowered(prefValueSoundWhenPowered)
            mService.setMute(prefValueSoundMute)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        mInstance = this
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        MobileAds.initialize(this)

        val adView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build()
        adView.loadAd(adRequest)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        initializeUiFromPreferences(preferences)
        initializeRatingPrompt()
        startService(preferences)
    }


    override fun onStart() {
        super.onStart()
        bindService()
    }

    override fun onResume() {
        super.onResume()

        if (mUiUpdateReceiver == null) {
            mUiUpdateReceiver = UiUpdateReceiver()
        }

        val intentFilter = android.content.IntentFilter(INTENT_POWER_CONNECTION_RECEIVER_CHARGING)
        registerReceiver(mUiUpdateReceiver, intentFilter)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        initializeUiFromPreferences(preferences)
    }

    override fun onPause() {
        if (mUiUpdateReceiver != null) {
            unregisterReceiver(mUiUpdateReceiver)
        }
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_action_help -> {

                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.help_dialog_message)
                        .setNeutralButton(R.string.launch_feedback_text) { dialogInterface, i ->
                            dialogInterface.cancel()
                            launchSupportEmailIntent()
                        }
                        .setNegativeButton(R.string.launch_privacy_policy_text) { dialogInterface, i ->
                            dialogInterface.cancel()
                            launchPrivacyPolicyIntent()
                        }
                        .setPositiveButton(R.string.help_dialog_close_button_text) { dialogInterface, i ->
                            dialogInterface.cancel()
                        }
                val alertDialog = builder.create()
                alertDialog.show()

                true
            }
            R.id.menu_action_settings -> {

                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun bindService() {
        // Bind to LocalService
        val intent = Intent(this, PowerStateService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        try {
            unbindService(mConnection)
        } catch (e: Exception) {
            Log.e("SERVICE", "Issue unbinding the service")
        }

        mBound = false
    }

    private fun startService(preferences: SharedPreferences) {
        val prefValueSoundWhenPowered: Boolean = preferences.getBoolean(KEY_PREF_SOUND_WHEN_POWERED, false)
        val prefValueSoundMute: Boolean = preferences.getBoolean(KEY_PREF_SOUND_MUTE, false)
        val powerStateServiceIntent = Intent(this, PowerStateService::class.java)

        powerStateServiceIntent.putExtra(POWER_STATE_SERVICE_INTENT_EXTRA_IS_MUTED, prefValueSoundMute)
        powerStateServiceIntent.putExtra(POWER_STATE_SERVICE_INTENT_EXTRA_SOUND_WHEN_POWERED, prefValueSoundWhenPowered)

        startService(powerStateServiceIntent)
    }

    private fun stopService() {
        unbindService()
        val intent = Intent(this, PowerStateService::class.java)
        stopService(intent)
    }


    fun onRadioButtonClicked(view: View) {
        // Check which radio button was clicked
        when (view.getId()) {
            R.id.rb_sound_when_powered -> setSoundOnPowered(true)
            R.id.rb_sound_when_not_powered -> setSoundOnPowered(false)
        }
    }

    fun onMuteToggleButtonClicked(view: View) {
        val isChecked = (view as ToggleButton).isChecked
        setMute(isChecked)
    }


    private fun setSoundOnPowered(checked: Boolean) {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_PREF_SOUND_WHEN_POWERED, checked)
        editor.apply()

        if (mBound) {
            mService!!.setSoundOnPowered(checked)
        }
    }

    private fun setMute(muted: Boolean) {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_PREF_SOUND_MUTE, muted)
        editor.apply()

        if (mBound) {
            mService!!.setMute(muted)
        }
    }


    fun setUiFromChargingState(isCharging: Boolean) {
        val image = findViewById<ImageView>(R.id.image_powerstate)
        val powerStateTextView = findViewById<TextView>(R.id.text_powerstate)

        if (isCharging) {
            image.setColorFilter(ContextCompat.getColor(this, R.color.image_charging))
            powerStateTextView.text = getString(R.string.power_state_powered)
        } else {
            image.setColorFilter(ContextCompat.getColor(this, R.color.image_not_charging))
            powerStateTextView.text = getString(R.string.power_state_not_powered)
        }
    }

    private fun initializeUiFromPreferences(preferences: SharedPreferences) {
        val prefValueShowPowerStateText = preferences.getBoolean(KEY_PREF_SHOW_STATE_TEXT, false)
        val prefValueSoundWhenPowered = preferences.getBoolean(KEY_PREF_SOUND_WHEN_POWERED, false)
        val prefValueSoundMute = preferences.getBoolean(KEY_PREF_SOUND_MUTE, false)

        val tbMute = findViewById<ToggleButton>(R.id.toggle_mute)
        val rbSoundWhenPowered = findViewById<RadioButton>(R.id.rb_sound_when_powered)
        val rbSoundWhenNotPowered = findViewById<RadioButton>(R.id.rb_sound_when_not_powered)
        val powerStateTextView = findViewById<TextView>(R.id.text_powerstate)
        val powerStateTextViewParams = powerStateTextView.layoutParams

        tbMute.isChecked = prefValueSoundMute
        rbSoundWhenPowered.isChecked = prefValueSoundWhenPowered
        rbSoundWhenNotPowered.isChecked = !prefValueSoundWhenPowered

        if (prefValueShowPowerStateText) {
            powerStateTextViewParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT
            setVisibility(R.id.text_powerstate, View.VISIBLE)
        } else {
            val viewHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.toFloat(), getResources().getDisplayMetrics()).toInt()
            powerStateTextViewParams.height = viewHeight
            setVisibility(R.id.text_powerstate, View.INVISIBLE)
        }

        powerStateTextView.layoutParams = powerStateTextViewParams
        powerStateTextView.requestLayout()
    }

    private fun initializeRatingPrompt() {
        val preferences = this.getSharedPreferences(KEY_PREF_SHARED_RATING_KEY_NAME, 0)

        if (preferences.getBoolean(KEY_PREF_RATING_DONT_SHOW_RATING_PROMPT, false)) {
            return
        }

        val preferencesEditor = preferences.edit()

        // increment launch counter
        val launchCount = preferences.getLong(KEY_PREF_RATING_LAUNCH_COUNT, 0) + 1
        preferencesEditor.putLong(KEY_PREF_RATING_LAUNCH_COUNT, launchCount)

        // get date of first launch
        var dateFirstLaunch = preferences.getLong(KEY_PREF_RATING_DATE_FIRST_LAUNCH, 0)
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            preferencesEditor.putLong(KEY_PREF_RATING_DATE_FIRST_LAUNCH, dateFirstLaunch)
        }

        preferencesEditor.apply()

        // wait at least n days before opening
        if (launchCount >= mRatingLaunchesUntilPrompt) {
            if (System.currentTimeMillis() >= dateFirstLaunch + mRatingDaysUntilPrompt * 24 * 60 * 60 * 1000) {
                showHideRatingView(true)
                showHideWarningText(false)
            }
        }
    }

    private fun setVisibility(id: Int, visibility: Int) {
        val view = findViewById<View>(id)
        if (view != null) {
            view.visibility = visibility
        }
    }

    // when a user clicks the "not enjoying" rating button, hide the enjoyment prompts and show the feedback prompts
    fun onRatingNotEnjoyingButtonClicked(view: View) {
        setVisibility(R.id.enjoying_circuit_tester, View.INVISIBLE)
        setVisibility(R.id.button_enjoying_no, View.INVISIBLE)
        setVisibility(R.id.button_enjoying_yes, View.INVISIBLE)
        setVisibility(R.id.feedback_circuit_tester, View.VISIBLE)
        setVisibility(R.id.button_feedback_no, View.VISIBLE)
        setVisibility(R.id.button_feedback_yes, View.VISIBLE)
    }

    // when a user clicks the "yes enjoying" rating button, hide the enjoyment prompts and show the rating prompts
    fun onRatingYesEnjoyingButtonClicked(view: View) {
        setVisibility(R.id.enjoying_circuit_tester, View.INVISIBLE)
        setVisibility(R.id.button_enjoying_no, View.INVISIBLE)
        setVisibility(R.id.button_enjoying_yes, View.INVISIBLE)
        setVisibility(R.id.rate_circuit_tester, View.VISIBLE)
        setVisibility(R.id.button_rating_no, View.VISIBLE)
        setVisibility(R.id.button_rating_yes, View.VISIBLE)
    }

    // when a user clicks the "no feedback" rating button, hide the rating prompt
    fun onRatingNoFeedbackButtonClicked(view: View) {
        setDontShowRatingPreference(true)
        showHideRatingView(false)
        showHideWarningText(true)
    }

    // when a user clicks the "yes feedback" rating button, hide the prompt and create an email intent
    fun onRatingYesFeedbackButtonClicked(view: View) {
        setDontShowRatingPreference(true)
        showHideRatingView(false)
        showHideWarningText(true)

        launchSupportEmailIntent()
    }

    // when a user clicks the "no rating" rating button, hide the rating prompt
    fun onRatingNoRatingButtonClicked(view: View) {
        setDontShowRatingPreference(true)
        showHideRatingView(false)
        showHideWarningText(true)
    }

    // when a user clicks the "yes rating" rating button, hide the prompt and open google play
    fun onRatingYesRatingButtonClicked(view: View) {
        setDontShowRatingPreference(true)
        showHideRatingView(false)
        showHideWarningText(true)

        launchGooglePlayIntent()
    }

    private fun setDontShowRatingPreference(dontShow: Boolean){
        val preferences = this.getSharedPreferences(KEY_PREF_SHARED_RATING_KEY_NAME, 0)
        val preferencesEditor = preferences.edit()
        preferencesEditor.putBoolean(KEY_PREF_RATING_DONT_SHOW_RATING_PROMPT, dontShow)
        preferencesEditor.apply()
    }

    private fun showHideRatingView(show: Boolean){
        val ratingView = findViewById<View>(R.id.rating_frame_layout)
        val ratingParams = ratingView.layoutParams

        if (show) {
            val ratingHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 115.toFloat(), getResources().getDisplayMetrics()).toInt()
            ratingParams.height = ratingHeight
            setVisibility(R.id.rating_frame_layout, View.VISIBLE)
        } else {
            val ratingHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.toFloat(), getResources().getDisplayMetrics()).toInt()
            ratingParams.height = ratingHeight
            setVisibility(R.id.rating_frame_layout, View.INVISIBLE)
        }

        ratingView.layoutParams = ratingParams
        ratingView.requestLayout()
    }

    private fun showHideWarningText(show: Boolean){
        val warningView = findViewById<View>(R.id.text_warning)
        val warningParams = warningView.layoutParams

        if (show){
            warningParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT
            setVisibility(R.id.text_warning, View.VISIBLE)
        } else {
            val warningHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.toFloat(), getResources().getDisplayMetrics()).toInt()
            warningParams.height = warningHeight
            setVisibility(R.id.text_warning, View.INVISIBLE)
        }

        warningView.layoutParams = warningParams
        warningView.requestLayout()
    }

    private fun launchGooglePlayIntent(){
        // try launching the google play app, otherwise fall back to the web browser
        try {
            val playStoreRatingIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=xyz.mechenbier.circuittester"))
            startActivity(playStoreRatingIntent)
        } catch (exception: android.content.ActivityNotFoundException) {
            val googlePlayBrowserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=xyz.mechenbier.circuittester"))
            startActivity(Intent.createChooser(googlePlayBrowserIntent, getString(R.string.intent_title_launch_browser)))
        }
    }

    private fun launchSupportEmailIntent(){
        val sendEmailIntent = Intent(Intent.ACTION_SENDTO)
        val uriText = "mailto:" + Uri.encode(getString(R.string.feedback_email_address)) + "?subject=" + Uri.encode(getString(R.string.app_name) + " " + getString(R.string.launch_feedback_text))
        val uri = Uri.parse(uriText)
        sendEmailIntent.data = uri
        startActivity(Intent.createChooser(sendEmailIntent, getString(R.string.intent_title_send_email)))
    }

    private fun launchPrivacyPolicyIntent(){
        val openPrivacyPolicyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)))
        startActivity(Intent.createChooser(openPrivacyPolicyIntent, getString(R.string.intent_title_launch_browser)))
    }
}