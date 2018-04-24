package xyz.mechenbier.circuittester;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.ToggleButton;

/**
 * Created by tgrannen on 5/15/2016.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

  protected final PowerStateAudio audio = new PowerStateAudio();

  public void init(Context context) {
    audio.InitMP(context);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    EvaluateBattery(intent);
  }

  private void EvaluateBattery(Intent intent) {
    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    audio.isCharging =
        status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL;
    audio.AlterAudioState();
  }

  public void Pause() {
    audio.SetMuted(true);
  }

  public void Resume() {
    View dv = ((Activity) audio.parentContext).getWindow().getDecorView();
    ToggleButton muteToggleButton = (ToggleButton) dv.findViewById(R.id.toggle_mute);
    audio.SetMuted(muteToggleButton.isChecked());
  }

  public void Resume(boolean muted) {
    audio.SetMuted(muted);
  }

  public void SoundButtonClicked(View view) {
    boolean checked = ((RadioButton) view).isChecked();

    // Check which radio button was clicked
    switch (view.getId()) {
      case R.id.rb_sound_when_powered:
        audio.SetSoundOnPowered(checked);
        break;
      case R.id.rb_sound_when_not_powered:
        audio.SetSoundOnPowered(!checked);
        break;
    }
  }

  public void SetOnPowered(boolean checked) {
    audio.SetSoundOnPowered(!checked);
  }

}
