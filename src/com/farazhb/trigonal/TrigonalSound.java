/** Trigonal by Faraz Hossein-Babaei, a game, in production 2016/2/19 - ... */
/* Built up on StackOverflow answer of Ishwor edited by saman at:
 * http://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java */
package com.farazhb.trigonal;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javafx.util.Pair;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;


public class TrigonalSound extends Thread implements TrigonalConsts {

  private String threadName;
  public volatile boolean reallyEnd;
  public volatile Pair<Byte, Integer> option; // key: OPTION_INPLAY, OPTION_OUTSTOP, OPTION_OUTSTAY
  private File file;
  private Clip clip;
  private AudioInputStream audioIn;
  private boolean looped;
  private boolean anyFade;
  private FloatControl gainControl;


  public TrigonalSound(String threadName, File file, boolean looped, boolean anyFade) {
    super(threadName); // ?! this constructor of Thread takes a String and I used it hereby
    this.threadName = threadName;
    this.file = file;
    this.looped = looped;
    this.anyFade = anyFade;
  } // end Constructor


  @Override
  public synchronized void run() {
    try {
      // URL url = this.getClass().getClassLoader().getResource(filename);
      /* AudioInputStream */audioIn = AudioSystem.getAudioInputStream(file/* url */);
      clip = AudioSystem.getClip(); // Get a sound clip resource.
      clip.open(audioIn); // Open audio clip and load samples from the audio input stream.
      gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      e.printStackTrace();
    }

    if (!anyFade) {
      gainControl.setValue(gainControl.getMaximum());
      if (looped) {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
      } else {
        clip.start();
      }
      return;

    } else {
      gainControl.setValue(gainControl.getMinimum());
      boolean reallyEnd = false;

      while (!reallyEnd) {
        synchronized (this) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        switch (option.getKey()) { // TODO: Later we can add limits for fade e.g. 80% -> 20% in 5 s
          case OPTION_INPLAY:
            startSound(option.getValue());
            break;
          case OPTION_OUTSTAY:
            quietSound(option.getValue());
            break;
          case OPTION_OUTSTOP:
            quietSound(option.getValue());
            clip.stop();
            clip.close();
            reallyEnd = true;
            break;
        } // need default?
      }
    }
  } // end run()


  private void startSound(int time) {
    if (looped) {
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    } else {
      clip.start();
    }
    if (time != 0) { // If not check for this and 0, it will fade unaffectingly forever
      fade(time);
    }
  }

  private void quietSound(int time) {
    time = (time < -10000) ? -10000 : (time > 10000) ? 10000 : time; // less than 10 seconds fading
    if (clip != null && clip.isOpen()) {
      fade(time);
    }
  }


  /** param fadeRate {<0 fadeOut, >0 fadeIn}. The volume kind of goes up exponentilly */
  private void fade(int time) {
    if (time == 0) {
      return;
    }

    int fadeTime = time;
    int sleepPeriod = 40; // sleep period in milliseconds
    int numSteps = Math.abs(fadeTime / sleepPeriod) - 1; // The -1 prevents a potential range exces

    float realMin = gainControl.getMinimum();
    float realMax = gainControl.getMaximum();

    float realVolIni = gainControl.getValue();
    float normVolIni = normalize(realVolIni, realMin, realMax);
    float realTarget, normTarget;
    if (time > 0) {
      normTarget = 1f;
      realTarget = realMax;
    } else {
      normTarget = 0f;
      realTarget = realMin;
    }

    float normVolStep = (float) (normTarget - normVolIni) / numSteps; // <0 for outfd, vc vrs
    float normVol, normVolModif, realVol;

    for (int i = 0; i < numSteps; i++) {
      normVol = normVolIni + i * normVolStep; // May do functions with this [0,1] value, e.g. ^{0.3,0.5,2,3,...}

      // Applying fitting function
      normVolModif = (float) Math.pow(normVol, 0.5); // modified as vol[0,1] ^ power, e.g. ^0.1

      realVol = antiNormalize(normVolModif, realMin, realMax);
      gainControl.setValue(realVol);

      try {
        Thread.sleep(sleepPeriod); // fading is actuated every 0.1 second
      } catch (InterruptedException e) {
        e.printStackTrace();
        return;
      }
    }
    gainControl.setValue(realTarget); // Finally, set to target value correcting any residue/mainder
    // stop/close unneeded since fading out only called from closeSound() method which includes them
  }

  private float normalize(float num, float min, float max) {
    float normal = 1 + (num - max) / (max - min);
    return normal;
  }

  private float antiNormalize(float normNum, float min, float max) {
    float real = (max - min) * (normNum - 1) + max;
    return real;
  }

}
