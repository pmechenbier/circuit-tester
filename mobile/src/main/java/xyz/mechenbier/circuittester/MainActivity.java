package xyz.mechenbier.circuittester;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ToggleButton;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import xyz.mechenbier.circuittester.PowerStateService.LocalBinder;

public class MainActivity extends AppCompatActivity {

    private PowerStateService mService;
    private boolean mBound = false;
    private UiUpdateReceiver mUiUpdateReceiver;
    private static MainActivity mActivityInstance;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final int mNotificationID = 5000;
    private static final int RATING_DAYS_UNTIL_PROMPT = 3;
    private static final int RATING_LAUNCHES_UNTIL_PROMPT = 3;

    //Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder)service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivityInstance = this;

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, BuildConfig.AdMobAppApiKey);
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);

        initializeRatingPrompt();

        startService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
    }

    @Override
    protected void onResume() {
        if (mUiUpdateReceiver == null) {
            mUiUpdateReceiver = new UiUpdateReceiver();
        }
        android.content.IntentFilter intentFilter = new android.content.IntentFilter(getString(R.string.intent_charging_name));
        registerReceiver(mUiUpdateReceiver, intentFilter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mUiUpdateReceiver != null) {
            unregisterReceiver(mUiUpdateReceiver);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }


    public static MainActivity getInstance() {
        return mActivityInstance;
    }


    public void createNotification() {
        createNotificationChannel(this);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent
                .getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.bolt)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_details))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager)this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(mNotificationID, notificationBuilder.build());
    }

    private void createNotificationChannel(Context context) {
        // we don't need to do this for devices running Android O or lower but NotificationChannel is not in appcompat so we have to check the version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(getString(R.string.notification_channel_description));
            notificationManager.createNotificationChannel((notificationChannel));
        }
    }

    public void dismissNotification() {
        NotificationManager notificationManager = (NotificationManager)this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mNotificationID);
    }


    public void bindService() {
        // Bind to LocalService
        Intent intent = new Intent(this, PowerStateService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            Log.e("SERVICE", "Issue unbinding the service");
        }
        mBound = false;
    }

    public void startService(View view) {
        startService();
        bindService();
    }

    public void startService() {
        Intent intent = new Intent(this, PowerStateService.class);
        startService(intent);
        createNotification();
    }

    public void stopService(View view) {
        stopService();
    }

    private void stopService() {
        unbindService();
        dismissNotification();
        Intent intent = new Intent(this, PowerStateService.class);
        stopService(intent);
    }


    public void onRadioButtonClicked(View view) {
        boolean isChecked = ((RadioButton)view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.rb_sound_when_powered:
                setSoundOnPowered(!isChecked);
                break;
            case R.id.rb_sound_when_not_powered:
                setSoundOnPowered(isChecked);
                break;
        }
    }

    private void setSoundOnPowered(boolean checked) {
        if (mBound) {
            mService.setSoundOnPowered(checked);
        }
    }


    public void onMuteToggleButtonClicked(View view) {
        boolean isChecked = ((ToggleButton)view).isChecked();
        setMute(isChecked);
    }

    private void setMute(boolean muted) {
        if (mBound) {
            mService.setMute(muted);
        }
    }


    public void setChargingIconColor(boolean isCharging) {
        ImageView image = (ImageView) findViewById(R.id.image_powerstate);
        if (isCharging) {
            image.setColorFilter(getResources().getColor(R.color.image_charging));
        } else {
            image.setColorFilter(getResources().getColor(R.color.image_not_charging));
        }
    }


    private void initializeRatingPrompt() {
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0);

        if (preferences.getBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), false)) {
            return;
        }

        SharedPreferences.Editor preferencesEditor = preferences.edit();

        // increment launch counter
        long launch_count = preferences.getLong(getString(R.string.preference_name_rating_launch_count), 0) + 1;
        preferencesEditor.putLong(getString(R.string.preference_name_rating_launch_count), launch_count);

        // get date of first launch
        long date_first_launch = preferences.getLong(getString(R.string.preference_name_rating_launch_date), 0);
        if (date_first_launch == 0) {
            date_first_launch = System.currentTimeMillis();
            preferencesEditor.putLong(getString(R.string.preference_name_rating_launch_date), date_first_launch);
        }

        preferencesEditor.apply();

        // wait at least n days before opening
        if (launch_count >= RATING_LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_first_launch + (RATING_DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                setVisibility(R.id.rating_frame_layout, View.VISIBLE);
                setVisibility(R.id.text_warning, View.INVISIBLE);
            }
        }
    }

    private void setVisibility(int id, int visibility) {
        View view = findViewById(id);
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    // when a user clicks the "not enjoying" rating button, hide the enjoyment prompts and show the feedback prompts
    public void onRatingNotEnjoyingButtonClicked(View view){
        setVisibility(R.id.enjoying_circuit_tester, View.INVISIBLE);
        setVisibility(R.id.button_enjoying_no, View.INVISIBLE);
        setVisibility(R.id.button_enjoying_yes, View.INVISIBLE);
        setVisibility(R.id.feedback_circuit_tester, View.VISIBLE);
        setVisibility(R.id.button_feedback_no, View.VISIBLE);
        setVisibility(R.id.button_feedback_yes, View.VISIBLE);
    }

    // when a user clicks the "yes enjoying" rating button, hide the enjoyment prompts and show the rating prompts
    public void onRatingYesEnjoyingButtonClicked(View view){
        setVisibility(R.id.enjoying_circuit_tester, View.INVISIBLE);
        setVisibility(R.id.button_enjoying_no, View.INVISIBLE);
        setVisibility(R.id.button_enjoying_yes, View.INVISIBLE);
        setVisibility(R.id.rate_circuit_tester, View.VISIBLE);
        setVisibility(R.id.button_rating_no, View.VISIBLE);
        setVisibility(R.id.button_rating_yes, View.VISIBLE);
    }

    // when a user clicks the "no feedback" rating button, hide the rating prompt
    public void onRatingNoFeedbackButtonClicked(View view){
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true);
        preferencesEditor.apply();
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE);
        setVisibility(R.id.text_warning, View.VISIBLE);
    }

    // when a user clicks the "yes feedback" rating button, hide the prompt and create an email intent
    public void onRatingYesFeedbackButtonClicked(View view){
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true);
        preferencesEditor.apply();
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE);
        setVisibility(R.id.text_warning, View.VISIBLE);
        Intent sendEmailIntent = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(getString(R.string.feedback_email_address)) + "?subject=" + Uri.encode("Circuit & Outlet Tester Feedback");
        Uri uri = Uri.parse(uriText);
        sendEmailIntent.setData(uri);
        startActivity(Intent.createChooser(sendEmailIntent, "Send e-mail..."));
    }

    // when a user clicks the "no rating" rating button, hide the rating prompt
    public void onRatingNoRatingButtonClicked(View view){
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true);
        preferencesEditor.apply();
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE);
        setVisibility(R.id.text_warning, View.VISIBLE);
    }

    // when a user clicks the "yes rating" rating button, hide the prompt and open google play
    public void onRatingYesRatingButtonClicked(View view){
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.preference_shared_key_name_rating), 0);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(getString(R.string.preference_name_rating_dont_show_rating_prompt), true);
        preferencesEditor.apply();
        setVisibility(R.id.rating_frame_layout, View.INVISIBLE);
        setVisibility(R.id.text_warning, View.VISIBLE);

        // try launching the google play app, otherwise fall back to the web browser
        try {
            Intent playStoreRatingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getString(R.string.app_package_name)));
            startActivity(playStoreRatingIntent);
        } catch (android.content.ActivityNotFoundException exception){
            Intent googlePlayBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getString(R.string.app_package_name)));
            startActivity(googlePlayBrowserIntent);
        }
    }
}