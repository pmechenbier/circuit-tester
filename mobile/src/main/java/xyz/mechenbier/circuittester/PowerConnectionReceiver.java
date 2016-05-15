package xyz.mechenbier.circuittester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.widget.Toast;

/**
 * Created by tgrannen on 5/15/2016.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    private MediaPlayer mp;
    private Context parentContext;

    public void init(Context context){
        parentContext = context;
        InitMP();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"received",Toast.LENGTH_LONG).show();
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        if(isCharging){
            StartMP();
        }
        else{
            StopMP();
        }
    }
    private void InitMP() {
        mp = MediaPlayer.create(parentContext, R.raw.test_alert);
        mp.setLooping(true);
    }

    private void StartMP() {
        if(!mp.isPlaying()){
            InitMP();
            mp.start();
        }
    }

    private void StopMP() {
        if(mp.isPlaying()){
            mp.stop();
            mp.reset();
        }
    }

}
