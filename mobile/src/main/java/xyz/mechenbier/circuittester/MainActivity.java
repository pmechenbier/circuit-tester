package xyz.mechenbier.circuittester;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.ToggleButton;
import xyz.mechenbier.circuittester.PowerStateService.LocalBinder;

public class MainActivity extends AppCompatActivity {

  PowerStateService mService;
  boolean mBound = false;
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

    ToggleButton muteToggleButton = (ToggleButton) findViewById(R.id.toggle_mute);
    muteToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setMute(isChecked);
      }
    });
  }

  @Override
  protected void onDestroy() {
    stopService();
    super.onDestroy();
  }

  private void stopService() {
    unbindService(mConnection);
    mBound = false;
    Intent intent = new Intent(this, PowerStateService.class);
    stopService(intent);
  }

  private void setMute(boolean muted) {
    if (mBound) {
      mService.setMute(muted);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  public void startService(View view) {
    Intent intent = new Intent(this, PowerStateService.class);
    startService(intent);

    // Bind to LocalService
    Intent intent2 = new Intent(this, PowerStateService.class);
    bindService(intent2, mConnection, Context.BIND_AUTO_CREATE);
  }

  public void stopService(View view) {
    stopService();
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

  private void SetSoundOnPowered(boolean checked) {
    if (mBound) {
      mService.SetSoundOnPowered(checked);
    }
  }
}