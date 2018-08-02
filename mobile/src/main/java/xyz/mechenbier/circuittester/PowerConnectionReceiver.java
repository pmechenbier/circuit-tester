package xyz.mechenbier.circuittester;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.view.View;
import android.widget.ToggleButton;

/**
 * Created by tgrannen on 5/15/2016.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

  protected final PowerStateAudio audio = new PowerStateAudio();
  private Context Context;
  public void init(Context context) {
    Context = context;
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

    Intent powerIntent = new Intent("xyz.mechenbier.circuittester.charging");
    powerIntent.putExtra("isCharging", audio.isCharging);
    Context.sendBroadcast(powerIntent);

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

  public void SetOnPowered(boolean checked) {
    audio.SetSoundOnPowered(!checked);
  }
}