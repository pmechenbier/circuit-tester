package xyz.mechenbier.circuittester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mechenbier on 8/1/2017.
 */

class UiUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.hasExtra(context.getString(R.string.charging_intent_charging_extra_name))){
            boolean isCharging = intent.getBooleanExtra(context.getString(R.string.charging_intent_charging_extra_name),false);
            MainActivity.getInstance().setChargingIconColor(isCharging);
        }
    }
}