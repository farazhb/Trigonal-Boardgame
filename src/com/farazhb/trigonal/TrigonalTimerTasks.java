/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/2/9 - ... */
package com.farazhb.trigonal;

import java.util.Timer;
import java.util.TimerTask;


public class TrigonalTimerTasks implements TrigonalConsts {

  // Timer keeping track of time left and relaying actuation of ending the turn!
  public static class TurnTimer {

    private boolean isPaused;
    private TrigonalWindow window;
    private Timer timer;
    private ATimerTask turnTimer;
    private int numSecLeft = TURN_TIME_LIMIT + 1;

    public void setTurnTimer(TrigonalWindow window, boolean turnSwitched) {
      if (timer != null && turnTimer != null) {
        turnTimer.cancel();
        timer.cancel();
      }
      if (turnSwitched) {
        numSecLeft = TURN_TIME_LIMIT + 1; // +1 bcs it starts after inrementing this down 1
      }
      this.window = window;
      timer = new Timer(true);
      turnTimer = new ATimerTask();
      timer.scheduleAtFixedRate(turnTimer, 0, 1000);
    }

    public void CancelTurnTimer() {
      turnTimer.cancel();
      timer.cancel();
    }


    public void pauseTimer() { // The effect is seen in ATimerTask class' run()
      isPaused = true;
    }


    public void unPauseTimer() { // The effect is seen in ATimerTask class' run()
      numSecLeft++;
      isPaused = false;
    }



    class ATimerTask extends TimerTask {
      @Override
      public void run() {
        if (!isPaused) {
          numSecLeft--;
          window.postNewTime(numSecLeft);
          if (numSecLeft <= 0) {
            numSecLeft = TURN_TIME_LIMIT;
            window.forceEndTurn();
          }
        }
      }
    }

  }

}
