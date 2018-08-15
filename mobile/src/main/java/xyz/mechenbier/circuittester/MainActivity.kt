package xyz.mechenbier.circuittester

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.ToggleButton
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics

import xyz.mechenbier.circuittester.PowerStateService.LocalBinder

class MainActivity : AppCompatActivity() {

    private var mService: PowerStateService? = null
    private var mBound = false
    private var mUiUpdateReceiver: UiUpdateReceiver? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private val mNotificationID = 5000
    private val mRatingDaysUntilPrompt = 3
    private val mRatingLaunchesUntilPrompt = 3

    companion object {
        lateinit var instance: MainActivity
    }

    //Defines callbacks for service binding, passed to bindService()
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        instance = this

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        MobileAds.initialize(this, BuildConfig.AdMobAppApiKey)
        val adView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build()
        adView.loadAd(adRequest)

        initializeRatingPrompt()

        startService()
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    override fun onResume() {
        if (mUiUpdateReceiver == null) {
            mUiUpdateReceiver = UiUpdateReceiver()
        }
        val intentFilter = android.content.IntentFilter(getString(R.string.intent_charging_name))
        registerReceiver(mUiUpdateReceiver, intentFilter)

        super.onResume()
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


    private fun createNotification() {
        createNotificationChannel(this)

        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent
                .getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.bolt)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_details))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setContentIntent(contentIntent)

        val notificationManager = this
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(mNotificationID, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        // we don't need to do this for devices running Android O or lower but NotificationChannel is not in appcompat so we have to check the version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = getString(R.string.notification_channel_description)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun dismissNotification() {
        val notificationManager = this
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(mNotificationID)
    }


    fun bindService() {
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

    fun startService(view: View) {
        startService()
        bindService()
    }

    fun startService() {
        val intent = Intent(this, PowerStateService::class.java)
        startService(intent)
        createNotification()
    }

    fun stopService(view: View) {
        stopService()
    }

    private fun stopService() {
        unbindService()
        dismissNotification()
        val intent = Intent(this, PowerStateService::class.java)
        stopService(intent)
    }


    fun onRadioButtonClicked(view: View) {
        val isChecked = (view as RadioButton).isChecked

        // Check which radio button was clicked
        when (view.getId()) {
            R.id.rb_sound_when_powered -> setSoundOnPowered(!isChecked)
            R.id.rb_sound_when_not_powered -> setSoundOnPowered(isChecked)
        }
    }

    private fun setSoundOnPowered(checked: Boolean) {
        if (mBound) {
            mService!!.setSoundOnPowered(checked)
        }
    }


    fun onMuteToggleButtonClicked(view: View) {
        val isChecked = (view as ToggleButton).isChecked
        setMute(isChecked)
    }

    private fun setMute(muted: Boolean) {
        if (mBound) {
            mService!!.setMute(muted)
        }
    }


    fun setChargingIconColor(isCharging: Boolean) {
        val image = findViewById<View>(R.id.image_powerstate) as ImageView
        if (isCharging) {
            image.setColorFilter(resources.getColor(R.color.image_charging))
        } else {
            image.setColorFilter(resources.getColor(R.color.image_not_charging))
        }
    }


    private fun initializeRatingPrompt() {
        val preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0)

        if (preferences.getBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), false)) {
            return
        }

        val preferencesEditor = preferences.edit()

        // increment launch counter
        val launchCount = preferences.getLong(getString(R.string.preference_name_rating_launch_count), 0) + 1
        preferencesEditor.putLong(getString(R.string.preference_name_rating_launch_count), launchCount)

        // get date of first launch
        var dateFirstLaunch = preferences.getLong(getString(R.string.preference_name_rating_launch_date), 0)
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            preferencesEditor.putLong(getString(R.string.preference_name_rating_launch_date), dateFirstLaunch)
        }

        preferencesEditor.apply()

        // wait at least n days before opening
        if (launchCount >= mRatingLaunchesUntilPrompt) {
            if (System.currentTimeMillis() >= dateFirstLaunch + mRatingDaysUntilPrompt * 24 * 60 * 60 * 1000) {
                setVisibility(R.id.rating_frame_layout, View.VISIBLE)
                setVisibility(R.id.text_warning, View.INVISIBLE)
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
        val preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0)
        val preferencesEditor = preferences.edit()
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true)
        preferencesEditor.apply()
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE)
        setVisibility(R.id.text_warning, View.VISIBLE)
    }

    // when a user clicks the "yes feedback" rating button, hide the prompt and create an email intent
    fun onRatingYesFeedbackButtonClicked(view: View) {
        val preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0)
        val preferencesEditor = preferences.edit()
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true)
        preferencesEditor.apply()
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE)
        setVisibility(R.id.text_warning, View.VISIBLE)
        val sendEmailIntent = Intent(Intent.ACTION_SENDTO)
        val uriText = "mailto:" + Uri.encode(getString(R.string.feedback_email_address)) + "?subject=" + Uri.encode("Circuit & Outlet Tester Feedback")
        val uri = Uri.parse(uriText)
        sendEmailIntent.data = uri
        startActivity(Intent.createChooser(sendEmailIntent, "Send e-mail..."))
    }

    // when a user clicks the "no rating" rating button, hide the rating prompt
    fun onRatingNoRatingButtonClicked(view: View) {
        val preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0)
        val preferencesEditor = preferences.edit()
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true)
        preferencesEditor.apply()
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE)
        setVisibility(R.id.text_warning, View.VISIBLE)
    }

    // when a user clicks the "yes rating" rating button, hide the prompt and open google play
    fun onRatingYesRatingButtonClicked(view: View) {
        val preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0)
        val preferencesEditor = preferences.edit()
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true)
        preferencesEditor.apply()
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE)
        setVisibility(R.id.text_warning, View.VISIBLE)

        // try launching the google play app, otherwise fall back to the web browser
        try {
            val playStoreRatingIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getString(R.string.app_package_name)))
            startActivity(playStoreRatingIntent)
        } catch (exception: android.content.ActivityNotFoundException) {
            val googlePlayBrowserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getString(R.string.app_package_name)))
            startActivity(googlePlayBrowserIntent)
        }

    }
}