package xyz.mechenbier.circuittester;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class PowerStateMonitor extends IntentService {

    public PowerStateMonitor() {
        super("PowerStateMonitor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
