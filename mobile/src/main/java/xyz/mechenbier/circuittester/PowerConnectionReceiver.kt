package xyz.mechenbier.circuittester

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

/**
 * Created by tgrannen on 5/15/2016.
 * Updated by pmechenbier on 10/9/18.
 */

const val INTENT_POWER_CONNECTION_RECEIVER_CHARGING = "xyz.mechenbier.circuittester.powerconnectionreceiver.intents.ischarging"
const val INTENT_POWER_CONNECTION_RECEIVER_CHARGING_EXTRA_ISCHARGING = "ischarging"

class PowerConnectionReceiver : BroadcastReceiver() {

    val audio = PowerStateAudio()
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
        audio.initMP(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        evaluateBattery(intent)
    }

    private fun evaluateBattery(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        audio.isCharging = isCharging

        val powerIntent = Intent(INTENT_POWER_CONNECTION_RECEIVER_CHARGING)
        powerIntent.putExtra(INTENT_POWER_CONNECTION_RECEIVER_CHARGING_EXTRA_ISCHARGING, isCharging)
        context!!.sendBroadcast(powerIntent)

        audio.alterAudioState()
    }

    fun pause() {
        audio.setMuted(true)
    }

    fun resume(muted: Boolean, soundWhenPowered: Boolean) {
        audio.setMuted(muted)
        audio.setSoundOnPowered(soundWhenPowered)
    }

    fun setOnPowered(checked: Boolean) {
        audio.setSoundOnPowered(checked)
    }
}