/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;

import java.awt.Color;
import java.awt.Font;


// TODO: Add TextField lengths of dialogs, they're all like 15

public interface TrigonalConsts {

  /* For TrigonalHub */
  public static final int MAX_NUM_PLAYERS = 2;

  /* For TrigonalScoring */
  public static final int BASIC_HEXAGON_SCORE = 50;

  /* For TrigonalDims */
  public static final double SCREEN_PAN_SZ_RATIO = 0.8;
  public static final double MARGIN = 10;

  /* For TrigonalBoard */
  public static final int NUM_TRIGS = 2; // >2 reasonable
  public static final double UNIT_EDGE = 100.0; // Triangle edge TODO Shouldn't but affects size
  public static final double HF_SQT_3 = Math.sqrt(3) / 2.0;
  public static final Color BOARD_BG_COLOR = new Color(90, 70, 10/*, 127*/);
  public static final Color BORDER_COLOR = new Color(18, 24, 12, 80);

  /* For TrigonalWindow */
  public static final int NUM_MOVES_PER_TURN = 4;
  public static final int TURN_TIME_LIMIT = 20; // Time limit per game turn [seconds]
  public static final Color[] PLAYER_COLORS = new Color[] { new Color(200, 50, 80, 180),
      new Color(20, 150, 200, 180), new Color(200, 30, 40, 130), new Color(80, 160, 70, 130),
      new Color(255, 174, 201, 130), new Color(200, 127, 0, 130), new Color(200, 0, 100, 130),
      new Color(0, 120, 50, 130) };
  public static final Font MESSAGE_FONT = new Font("Arial", Font.BOLD, 20);
  public static final Font TIMER_FONT = new Font("Arial", Font.BOLD, 48);
  public static final int POPUP_DELAY = 3000; // basis for time it take for popup to re-/dis-appear
  public static final int POPUP_TIME_STEP = 10; // time period for each opacity increment in popup
  public static final int TEXTFIELD_SIZE = 15; // any textfield I want to have this length

  /* For TrigonalState */
  public static final int[] NOT_A_TRIG_INDICES = new int[] { -1, -1, -1 };
  // TODO: Use enum instead of following. The actual String value doesn't matter. int?
  public static final String END_GAME_INVT = "endGameInvitation";
  public static final String END_GAME_VOTE = "endGameVote";
  public static final String CANCEL_END_GAME_INVT = "cancelEndGameInvitation";
  public static final String NEW_GAME_REQ = "newGame";
  public static final String PLAYER_INTRO = "playerIntroduction";
  public static final String PLAYER_INTROS_DONE = "playerIntrosAreDone";
  public static final String PLAYER_DISCONNECTED = "playerDisconnected";
  public static final String END_GAME_ENOUGH_VOTES = "enoughVotesToEndGame";
  

  /* For TrigonalClient */

  /* For TrigonalTrigon */
  public static final byte UP_TRIG_NAME = 1;
  public static final byte DN_TRIG_NAME = -1;
  public static final int NEUTRAL_TRIG_PLAYER_ID = 0; // each triangle has an owner player id
  public static final Color TRIG_COLOR_INI = new Color(5, 15, 15, 180); // TODO: is it r,g,b,a?
  public static final int NUM_PTS = 3; // number of vertices of a triangle, basis of this game
  public static final double TRIGON_DIM_SHRINKAGE = 2.0; // Pixels to shrink each trigon by

  /* For Main */
  public static final int DEFAULT_PORT = 45017;
  public static final Font WELCOME_FONT = new Font("Tahoe", Font.BOLD, 20);
  public static final Color WELCOME_BG_COLOR = new Color(255, 200, 100);

  /* For Sound */
  public static final String RES_PATH_BASE = "res/"; // TODO: only Windows? If so chng to 'res\' mac
  // TODO: Use enum for these too:
  public static final byte OPTION_INPLAY = 1; // key for playback control, i.e. fade in and continue
  public static final byte OPTION_OUTSTAY = 2; // fade out but stand by
  public static final byte OPTION_OUTSTOP = 3; // fade out and stop/end that sound thread

}
