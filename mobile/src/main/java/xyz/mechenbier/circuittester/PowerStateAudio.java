package xyz.mechenbier.circuittester;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by tgrannen on 5/15/2016.
 */
public class PowerStateAudio {

  private MediaPlayer mp;
  public Context parentContext;
  private boolean Muted;
  private boolean SoundOnPowered = true;
  public boolean isCharging;

  public void AlterAudioState() {
    if (isCharging) {
      if (SoundOnPowered) {
        StartMP();
      } else {
        StopMP();
      }

    } else {
      if (SoundOnPowered) {
        StopMP();
      } else {
        StartMP();
      }
    }
  }

  public void SetSoundOnPowered(boolean isSoundOnPowered) {
    SoundOnPowered = isSoundOnPowered;
    AlterAudioState();
  }

  public void SetMuted(boolean isMuted) {
    Muted = isMuted;
    if (Muted) {
      StopMP();
    } else {
      AlterAudioState();
    }
  }

  public void InitMP() {
    if (parentContext == null) {
      return;
    }
    mp = MediaPlayer.create(parentContext, R.raw.test_alert);
    mp.setLooping(true);
  }

  public void InitMP(Context context) {
    parentContext = context;
    InitMP();
  }

  private void StartMP() {
    if (mp == null) {
      InitMP();
    }
    if (mp != null && !mp.isPlaying() && !Muted) {
      InitMP();
      mp.start();
    }
  }

  private void StopMP() {
    if (mp != null && mp.isPlaying()) {
      mp.stop();
      mp.reset();
    }
  }
}
