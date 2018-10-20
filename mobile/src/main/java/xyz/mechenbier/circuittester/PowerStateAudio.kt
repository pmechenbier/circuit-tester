package xyz.mechenbier.circuittester

import android.content.Context
import android.media.MediaPlayer

/**
 * Created by tgrannen on 5/15/2016.
 * Updated by pmechenbier on 10/9/2018.
 */
class PowerStateAudio {

    private var mMediaPlayer: MediaPlayer? = null
    private var mMuted: Boolean = false
    private var mSoundOnPowered = true
    private var mParentContext: Context? = null
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
        mParentContext = context
        initMP()
    }

    private fun initMP() {
        if (mParentContext == null) {
            return
        }
        mMediaPlayer = MediaPlayer.create(mParentContext, R.raw.test_alert)
        mMediaPlayer!!.isLooping = true
    }

    private fun startMP() {
        if (mMediaPlayer == null) {
            initMP()
        }
        if (mMediaPlayer != null && !mMediaPlayer!!.isPlaying && !mMuted) {
            initMP()
            mMediaPlayer!!.start()
        }
    }

    private fun stopMP() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
        }
    }
}