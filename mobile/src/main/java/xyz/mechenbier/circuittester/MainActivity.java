package xyz.mechenbier.circuittester;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ToggleButton;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import xyz.mechenbier.circuittester.PowerStateService.LocalBinder;

public class MainActivity extends AppCompatActivity {

  PowerStateService mService;
  boolean mBound = false;
  private UiUpdateReceiver uiUpdateReceiver;
  private static int notificationID = 5000;
  private static MainActivity instance;
  private FirebaseAnalytics mFirebaseAnalytics;
  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
        IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      LocalBinder binder = (LocalBinder) service;
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

    instance = this;

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    MobileAds.initialize(this, BuildConfig.AdMobAppApiKey);
    AdView adView = (AdView)findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build();
    adView.loadAd(adRequest);

    ToggleButton muteToggleButton = (ToggleButton) findViewById(R.id.toggle_mute);
    muteToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMute(isChecked);
      }
    });
    startService();
  }

  @Override
  protected void onStart() {
    super.onStart();
    bindService();
  }

  @Override
  protected void onResume() {
    if (uiUpdateReceiver == null){
      uiUpdateReceiver = new UiUpdateReceiver();
    }
    android.content.IntentFilter intentFilter = new android.content.IntentFilter(getString(R.string.charging_intent_name));
    registerReceiver(uiUpdateReceiver, intentFilter);

    super.onResume();
  }

  @Override
  protected void onPause() {
      if (uiUpdateReceiver != null){
          unregisterReceiver(uiUpdateReceiver);
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

  public static MainActivity getInstance(){
    return instance;
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

  public void createNotification() {

    Intent intent = new Intent(this, MainActivity.class);
    PendingIntent contentIntent = PendingIntent
        .getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder b = new NotificationCompat.Builder(this);
    b.setDefaults(NotificationCompat.DEFAULT_ALL)
        .setWhen(System.currentTimeMillis())
        .setOngoing(true)
        .setSmallIcon(R.drawable.bolt)
        .setContentTitle(getString(R.string.ApplicationTitle))
        .setContentText(getString(R.string.notification_details))
        .setContentIntent(contentIntent);

    NotificationManager nm = (NotificationManager) this
        .getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(notificationID, b.build());
  }

  public void dismissNotification() {
    NotificationManager nm = (NotificationManager) this
        .getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(notificationID);
  }

  public void bindService() {
    // Bind to LocalService
    Intent intent = new Intent(this, PowerStateService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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

  private void unbindService() {
    try {
      unbindService(mConnection);
    } catch (Exception e) {
      Log.e("SERVICE", "Issue unbinding the service");
    }
    mBound = false;
  }

  public void setChargingIconColor(boolean isCharging) {
    ImageView image = (ImageView)findViewById(R.id.image_powerstate);
    if (isCharging){
      image.setColorFilter(getResources().getColor(R.color.image_charging));
    } else {
      image.setColorFilter(getResources().getColor(R.color.image_not_charging));
    }
  }

  public void onRadioButtonClicked(View view) {
    boolean checked = ((RadioButton) view).isChecked();

    // Check which radio button was clicked
    switch (view.getId()) {
      case R.id.rb_sound_when_powered:
        SetSoundOnPowered(!checked);
        break;
      case R.id.rb_sound_when_not_powered:
        SetSoundOnPowered(checked);
        break;
    }
  }

  private void setMute(boolean muted) {
    if (mBound) {
      mService.setMute(muted);
    }
  }

  private void SetSoundOnPowered(boolean checked) {
    if (mBound) {
      mService.setSoundOnPowered(checked);
    }
  }
}