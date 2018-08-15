package xyz.mechenbier.circuittester

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by mechenbier on 8/1/2017.
 */
internal class UiUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.hasExtra(context.getString(R.string.intent_charging_extra_name))) {
            val isCharging = intent.getBooleanExtra(context.getString(R.string.intent_charging_extra_name), false)
            MainActivity.instance.setChargingIconColor(isCharging)
        }
    }
}