package xyz.mechenbier.circuittester

import android.content.Context
import android.media.MediaPlayer

/**
 * Created by tgrannen on 5/15/2016.
 */
class PowerStateAudio {

    private var mp: MediaPlayer? = null
    private var Muted: Boolean = false
    private var SoundOnPowered = true
    var parentContext: Context? = null
    var IsCharging: Boolean = false

    fun AlterAudioState() {
        if (IsCharging) {
            if (SoundOnPowered) {
                StartMP()
            } else {
                StopMP()
            }

        } else {
            if (SoundOnPowered) {
                StopMP()
            } else {
                StartMP()
            }
        }
    }

    fun SetSoundOnPowered(isSoundOnPowered: Boolean) {
        SoundOnPowered = isSoundOnPowered
        AlterAudioState()
    }

    fun SetMuted(isMuted: Boolean) {
        Muted = isMuted
        if (Muted) {
            StopMP()
        } else {
            AlterAudioState()
        }
    }

    fun InitMP() {
        if (parentContext == null) {
            return
        }
        mp = MediaPlayer.create(parentContext, R.raw.test_alert)
        mp!!.isLooping = true
    }

    fun InitMP(context: Context) {
        parentContext = context
        InitMP()
    }

    private fun StartMP() {
        if (mp == null) {
            InitMP()
        }
        if (mp != null && !mp!!.isPlaying && !Muted) {
            InitMP()
            mp!!.start()
        }
    }

    private fun StopMP() {
        if (mp != null && mp!!.isPlaying) {
            mp!!.stop()
            mp!!.reset()
        }
    }
}