package xyz.mechenbier.circuittester;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;

public class PowerStateMonitor extends IntentService {

    public PowerStateMonitor() {
        super("PowerStateMonitor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.test_alert);
        mp.setLooping(true);
        mp.start();
    }
}
