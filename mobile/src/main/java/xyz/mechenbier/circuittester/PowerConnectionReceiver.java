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

    private MediaPlayer mp;
    private Context parentContext;

    private boolean Muted;
    private boolean SoundOnPowered = true;
    private boolean isCharging;

    public void SetMuted(boolean isMuted){
        Muted = isMuted;
        if(Muted){
            StopMP();
        }
        else{
            AlterAudioState();
        }
    }

    public void SetSoundOnPowered(boolean isSoundOnPowered){
        SoundOnPowered = isSoundOnPowered;
        AlterAudioState();
    }

    public void init(Context context){
        parentContext = context;
        InitMP();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        EvaluateBattery(intent);
    }

    private void EvaluateBattery(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        AlterAudioState();
    }

    private void AlterAudioState() {
        if(isCharging){
            if(SoundOnPowered){
                StartMP();
            }
            else{
                StopMP();
            }

        }
        else{
            if(SoundOnPowered){
                StopMP();
            }
            else{
                StartMP();
            }
        }
    }

    private void InitMP() {
        mp = MediaPlayer.create(parentContext, R.raw.test_alert);
        mp.setLooping(true);
    }

    private void StartMP() {
        if(!mp.isPlaying() && !Muted){
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
