package xyz.mechenbier.circuittester

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.Toast

class PowerStateService : Service() {
    var mStartMode: Int = 0       // indicates how to behave if the service is killed
    var mBinder: IBinder? = null      // interface for clients that bind
    var mAllowRebind: Boolean = false // indicates whether onRebind should be used
    var pConRec: PowerConnectionReceiver = PowerConnectionReceiver()
    private var ifilter: IntentFilter? = null

    override fun onCreate() {
        // The service is being created
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // The service is starting, due to a call to startService()
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        pConRec.init(this)
        ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        pConRec.Resume(false)
        registerReceiver(pConRec, ifilter);
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
        Toast.makeText(this, "service stopping", Toast.LENGTH_SHORT).show()
        unregisterReceiver(pConRec);
        pConRec.Pause()
    }
}
