package xyz.mechenbier.circuittester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.widget.Toast;

import java.security.PublicKey;

/**
 * Created by tgrannen on 5/15/2016.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    protected final PowerStateAudio audio = new PowerStateAudio();

    public void init(Context context){
        audio.parentContext = context;
        audio.InitMP();
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

}
