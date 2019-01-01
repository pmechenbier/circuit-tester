package xyz.mechenbier.circuittester

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.crashlytics.android.Crashlytics

const val POWER_STATE_SERVICE_INTENT_EXTRA_IS_MUTED = "isMutedIntentExtra"
const val POWER_STATE_SERVICE_INTENT_EXTRA_SOUND_WHEN_POWERED = "soundWhenPoweredIntentExtra"

class PowerStateService : Service() {
    private var mStartMode: Int = 0       // indicates how to behave if the service is killed
    private val mBinder = LocalBinder()     // interface for clients that bind
    private var mAllowRebind: Boolean = false // indicates whether onRebind should be used
    private var mPowerConnectionReceiver: PowerConnectionReceiver = PowerConnectionReceiver()
    private var mIntentFilter: IntentFilter? = null
    private var mDebug: Boolean = false    // Setting to true will show toasts on start and stop of the service

    override fun onCreate() {
        // The service is being created
        mPowerConnectionReceiver.init(this)
        mIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // The service is starting, due to a call to startService()
        showToast("Service Starting")

        var isMuted = false
        var soundWhenPowered = false
        if (intent != null && intent.extras != null) {
            isMuted = intent.extras!!.getBoolean(POWER_STATE_SERVICE_INTENT_EXTRA_IS_MUTED, false)
            soundWhenPowered = intent.extras!!.getBoolean(POWER_STATE_SERVICE_INTENT_EXTRA_SOUND_WHEN_POWERED, false)
        }

        mPowerConnectionReceiver.resume(isMuted, soundWhenPowered)
        registerReceiver(mPowerConnectionReceiver, mIntentFilter)
        return mStartMode
    }

    override fun onBind(intent: Intent): IBinder? {
        // A client is binding to the service with bindService()
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // All clients have unbound with unbindService()
        return mAllowRebind
    }

    override fun onRebind(intent: Intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    override fun onDestroy() {
        // The service is no longer used and is being destroyed
        showToast("Service Stopping")

        try {
            unregisterReceiver(mPowerConnectionReceiver)
        } catch (e: IllegalArgumentException) {
            // this as already unregistered at one point
            Crashlytics.logException(e)
        }
        mPowerConnectionReceiver.pause()
    }

    fun setMute(muted: Boolean) {
        mPowerConnectionReceiver.audio.setMuted(muted)
    }

    fun setSoundOnPowered(checked: Boolean) {
        mPowerConnectionReceiver.setOnPowered(checked)
    }

    private fun showToast(message: String) {
        if (mDebug) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): PowerStateService {
            return this@PowerStateService
        }
    }
}
