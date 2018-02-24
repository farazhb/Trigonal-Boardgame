/** Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
/* Started development of game based on Hub-Client structure of NetworkGameManager (from David
 * Ecke). Started implementation 2016/1/22, completed interactive Trigonal board 2016/2/8, initial
 * scoring implemented by 2016/2/15, now improving dialogs with players. */
package com.farazhb.trigonal;

import javax.swing.JOptionPane;

/** Game board is a regular hexagon composed of closed-packed array of equilateral triangles.
 * Players mark n triangles with their corresponding color. Winner is determined based on specific
 * game rules (e.g. first to have a full 6-triangle hexagon, one to have the most integral region
 * when board full). Each turn is timed and has a deadline. Parameter n may be 1, 2, 3, or 4. In
 * first n turns of game, the selections per turn goes up from 1 to n.
 * 
 * Trigonal is played over network whereby initiating payer's computer acts as the hub (?) for the
 * game. Set number of additional players join as clients. The hub hosts the board data after any
 * change, the board is sent to all players in messages through ObjectStreams to all players and
 * each player redraws their updated boards graphically. */

public class Main implements TrigonalConsts {

  public static void main(String[] args) {

    TrigonalWelcome welcome = new TrigonalWelcome(false);

    try {
      welcome = new TrigonalWelcome();
    } catch (Exception e) {
      welcome = new TrigonalWelcome();
      // Pressing Cancel button on frame of welcome, calls System.exit(0)
      // welcome.dispose(); // doesn't have a dispose method
    }

  }

}
