package xyz.mechenbier.circuittester

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.view.View
import android.widget.ToggleButton

/**
 * Created by tgrannen on 5/15/2016.
 */
class PowerConnectionReceiver : BroadcastReceiver() {

    val audio = PowerStateAudio()
    private var context: Context? = null
    fun init(context: Context) {
        this.context = context
        audio.InitMP(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        EvaluateBattery(intent)
    }

    private fun EvaluateBattery(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        audio.IsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val powerIntent = Intent(context!!.getString(R.string.intent_charging_name))
        powerIntent.putExtra(context!!.getString(R.string.intent_charging_extra_name), audio.IsCharging)
        context!!.sendBroadcast(powerIntent)

        audio.AlterAudioState()
    }

    fun Pause() {
        audio.SetMuted(true)
    }

    fun Resume() {
        val dv = (audio.parentContext as Activity).window.decorView
        val muteToggleButton = dv.findViewById<View>(R.id.toggle_mute) as ToggleButton
        audio.SetMuted(muteToggleButton.isChecked)
    }

    fun Resume(muted: Boolean) {
        audio.SetMuted(muted)
    }

    fun SetOnPowered(checked: Boolean) {
        audio.SetSoundOnPowered(!checked)
    }
}