package xyz.mechenbier.circuittester

import android.content.Context
import android.media.MediaPlayer

/**
 * Created by tgrannen on 5/15/2016.
 * Updated by pmechenbier on 10/9/2018.
 */
class PowerStateAudio {

    private var mp: MediaPlayer? = null
    private var mMuted: Boolean = false
    private var mSoundOnPowered = true
    var parentContext: Context? = null
    var isCharging: Boolean? = null

    fun alterAudioState() {
        if (isCharging != null){
            if (isCharging!!) {
                if (mSoundOnPowered) {
                    startMP()
                } else {
                    stopMP()
                }

            } else {
                if (mSoundOnPowered) {
                    stopMP()
                } else {
                    startMP()
                }
            }
        }
    }

    fun setSoundOnPowered(isSoundOnPowered: Boolean) {
        mSoundOnPowered = isSoundOnPowered
        alterAudioState()
    }

    fun setMuted(isMuted: Boolean) {
        mMuted = isMuted
        if (mMuted) {
            stopMP()
        } else {
            alterAudioState()
        }
    }

    fun initMP(context: Context) {
        parentContext = context
        initMP()
    }

    private fun initMP() {
        if (parentContext == null) {
            return
        }
        mp = MediaPlayer.create(parentContext, R.raw.test_alert)
        mp!!.isLooping = true
    }

    private fun startMP() {
        if (mp == null) {
            initMP()
        }
        if (mp != null && !mp!!.isPlaying && !mMuted) {
            initMP()
            mp!!.start()
        }
    }

    private fun stopMP() {
        if (mp != null && mp!!.isPlaying) {
            mp!!.stop()
            mp!!.reset()
        }
    }
}