package xyz.mechenbier.circuittester

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

/**
 * Created by tgrannen on 5/15/2016.
 * Updated by pmechenbier on 10/8/18.
 */

const val INTENT_POWER_CONNECTION_RECEIVER_CHARGING = "xyz.mechenbier.circuittester.powerconnectionreceiver.intents.ischarging"
const val INTENT_POWER_CONNECTION_RECEIVER_CHARGING_EXTRA_ISCHARGING = "ischarging"

class PowerConnectionReceiver : BroadcastReceiver() {

    val audio = PowerStateAudio()
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
        audio.InitMP(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        evaluateBattery(intent)
    }

    private fun evaluateBattery(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        audio.IsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val powerIntent = Intent(INTENT_POWER_CONNECTION_RECEIVER_CHARGING)
        powerIntent.putExtra(INTENT_POWER_CONNECTION_RECEIVER_CHARGING_EXTRA_ISCHARGING, audio.IsCharging)
        context!!.sendBroadcast(powerIntent)

        audio.AlterAudioState()
    }

    fun pause() {
        audio.SetMuted(true)
    }

    fun resume(muted: Boolean, soundWhenPowered: Boolean) {
        audio.SetMuted(muted)
        audio.SetSoundOnPowered(soundWhenPowered)
    }

    fun setOnPowered(checked: Boolean) {
        audio.SetSoundOnPowered(checked)
    }
}