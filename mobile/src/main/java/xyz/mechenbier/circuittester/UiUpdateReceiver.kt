package xyz.mechenbier.circuittester

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by mechenbier on 8/1/2017.
 */
internal class UiUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.hasExtra(INTENT_POWER_CONNECTION_RECEIVER_CHARGING_EXTRA_ISCHARGING)) {
            val isCharging = intent.getBooleanExtra(INTENT_POWER_CONNECTION_RECEIVER_CHARGING_EXTRA_ISCHARGING, false)
            MainActivity.mInstance.setUiFromChargingState(isCharging)
        }
    }
}