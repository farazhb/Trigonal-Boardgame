/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;


import java.util.ArrayList;
import com.farazhb.trigonal.TrigonalShape.Trigon;
import com.farazhb.trigonal.TrigonalShape.TrigOnIdComparator;


/** This class holds all the necessary information to represent the state of a Trigonal game. Herein
 * applyMessage(sender,message) modifies the state object with content sent from a player through
 * the hub. By protocol any message is an instance of TrigonalMove and may contain a String or a
 * list of integers indexing trigons that were claimed by the sender last turn. */
public class TrigonalState implements TrigonalConsts {

  // iVars
  private TrigonalWindow window; // I decided to give state access to window and use it carefully
  public boolean playerDisconnected; // true if someone left. true? state sent to all & Clients exit
  public ArrayList<Trigon> virtBrd; // Virtual board, board content. null before game start.
  public TrigonalPlayer[] players; // Players with info.
  public int currentPlayer; // The ID of the player who is making the move.
  private double[] dims; // Dimensions of the board updated from class TrigonalDims
  public boolean gameInProgress; // true while game played; false before first game & btw games.
  public int nClicksLeft; // Number of clicks left this turn
  private int turnIndex; // Keeps track of times turns are passed among players
  private int realTurnIdx; // Keeps track of number of cycles of turns among group of players
  public int nTrigsLeft; // Number of trigons left unclaimed on board
  public boolean isPaused; // varied by window, accessed by board for its repaint()
  private int lastPlayingPlayer = -1; // After end game decision, indicates end game player's turn
  private ArrayList<Integer> winners;
  private boolean allIntroduced, allConnected; // each set by an event. Once both true, game starts.


  TrigonalState(TrigonalWindow window) { // constructor
    this.window = window;
  } // end constructor


  /* ------ the method that is called by the Hub to react to messages from players ------ */
  /** Updates state according to message received from Hub which itself received from last turn
   * player. Illegal messages (of the wrong type or coming at an illegal time) are simply ignored.
   * The messages that are understood include String "newgame" to start game and a State
   * object. @param sender player ID number sent message @param message the message received. */
  public void applyMessage(TrigonalMove inMove) {
    dims = TrigonalDims.getEffDims(); // Updating the dimensions if any change
    int sender = inMove.getSender();
    String msgText = inMove.getMsgString();

    if (msgText.equals(PLAYER_DISCONNECTED)) { // TODO: brought this here bcs Hub couldn't do it
      playerDisconnected = true;
      return;
    }

    if (!gameInProgress) { // i.e. game IS NOT in progress
      // one of new-game-req and player-intros-done happens 1st and the other 2nd. Since I don't
      // know which comes first, I need the cross conditional here; once both true, we start game.
      if (msgText.equals(NEW_GAME_REQ)) { // TODO: If like special case FIRST game, make unique msg
        allConnected = true;
        if (allIntroduced) {
          startGame();
        }
      } else if (msgText.equals(PLAYER_INTROS_DONE)) {
        allIntroduced = true;
        players = inMove.getPlayers();
        if (allConnected) {
          startGame();
        }
      }
      return;
    }

    else { // i.e. game IS in progress

      if (inMove.isMoveNotString() && sender == currentPlayer) {
        // TODO: Naturally sender is currentPlayer within game. condition misleads. test absence
        players[sender] = inMove.getPlayer(); // for later, when able to update players midgame
        nTrigsLeft -= inMove.getMoveSize();
        int claim;
        while (inMove.getMoveSize() > 0) { // We pop and empty the move's list here
          claim = inMove.popMoveSegment();
          virtBrd.get(claim).setOwnerId(sender);
          virtBrd.get(claim).setColor(players[sender].getColor());
        }
        realTurnIdx = 1 + (turnIndex + 1) / MAX_NUM_PLAYERS; // TODO: Calc. is off. Needs condition
        turnIndex++;
        // moves/turn grow initial few turns. Stop after that. This if-else ensures this.
        nClicksLeft = (realTurnIdx <= NUM_MOVES_PER_TURN) ? realTurnIdx : NUM_MOVES_PER_TURN;
        if (nTrigsLeft < 2 * NUM_MOVES_PER_TURN) { // few board blocks left, reduce numClicks
          // div by 2 reasonable for the game. fewer trigs left than that? set accordingly
          nClicksLeft = (nClicksLeft / 2 > nTrigsLeft) ? nTrigsLeft : nClicksLeft / 2;
          // This formula arbitrary works well for standard Num moves per turn of 2~5
        }
        if (boardFull()) { // end game if board has just filled up
          endGame();
        } else { // It's the other player's turn now.
          currentPlayer = (sender + 1 > MAX_NUM_PLAYERS) ? 1 : sender + 1; // just increment player
        }
        if (sender == lastPlayingPlayer) {
          // window.forceEndGame(); not needed it seems!
          // window.newState(this, false); not needed it seems!
          endGame();
        }

      } else { // i.e. message NOT move but String
        if (msgText.equals(END_GAME_INVT)) {
          // sender is assumed to be >= 1. Here, incrementing back one player.
          lastPlayingPlayer = (sender > 1) ? sender - 1 : MAX_NUM_PLAYERS;
          String auxMsg = inMove.getAuxMsgString();
          window.invitedToEndGame(sender, auxMsg);
        } else if (msgText.equals(CANCEL_END_GAME_INVT)) {
          lastPlayingPlayer = -1; // resetting it to value indicating nobody
          String auxMsg = inMove.getAuxMsgString();
          window.closeDialogs("Game goes on. " + auxMsg);
        } else if (msgText.equals(END_GAME_ENOUGH_VOTES)) {
          window.closeDialogs("Enough votes. Game will end at end of cycle.");
        }
      }
    }

  }



  private void endGame() {
    gameInProgress = false;
    lastPlayingPlayer = -1;
    TrigonalScoring.buildPtsNeighbors(virtBrd); // Builds hexagons list for scoring
    TrigonalScoring.findScores(players); // TODO: scores for players. Share on dialog
    for (int i = 1; i <= MAX_NUM_PLAYERS; i++) {
      System.out.println("" + players[i].getName() + ":  " + players[i].getScore());
    }

    int numPplSharingTopScore = 0;
    int topScore = 0;
    winners = new ArrayList<Integer>();
    for (TrigonalPlayer player : players) { // 1st loop: finds highest score
      topScore = (player.getScore() > topScore) ? player.getScore() : topScore;
    }
    for (int i = 1; i <= MAX_NUM_PLAYERS; i++) { // 2nd loop: finds ppl having top score from 1st
                                                 // loop
      if (players[i].getScore() == topScore) {
        numPplSharingTopScore++;
        winners.add(players[i].getId());
      }
    }
    int numWinners = winners.size();
    String winReport = (numPplSharingTopScore > 1) ? "Winners are:\n\n" : "Winner is:\n\n";
    for (int i = 0; i < numWinners; i++) {
      winReport += String.format("%1$s %2$10s \n", players[winners.get(i)].getName(),
                players[winners.get(i)].getScore());
    }
    window.gameEndedWork(winReport);
  }


  public void switchTurn() { // TODO: Nice to have here in State class, but so far does nothing!
  }



  /** Start a game. Initial board dimensions are determined. Board is initialized w/ virtual board
   * after buildTrigs. Players are randomly assigned their roles. This method is kind of like a
   * constructor but is run sometime after when all players have joined as called by Hub. */
  private void startGame() {
    // Building board triangles
    // players = new TrigonalPlayer[MAX_NUM_PLAYERS + 1];
    // players[0] = new TrigonalPlayer(0, "Nobody", TRIG_COLOR_INI);
    dims = TrigonalDims.getEffDims(); // Initial dimensions for use in building board
    nTrigsLeft = 0; // will be counted up in buildTrigs()
    virtBrd = buildTrigs(); // Virtual board, content of board
    assignIndices(); // Assigns indices to triangles based on coordinates TL -> BR in rows
    sortTrigsById(virtBrd); // Reorders trigons' list according to the index assigned
    // TrigonalScoring.buildPtsNeighbors(virtBrd); // Builds hexagons list for scoring
    turnIndex = 0;
    nClicksLeft = 1; // Start turn clicks at 1 and progressively raise them up to N
    // TODO: Important: Hub should determine first player. Maybe Hub make initial state & send it
    // to all to set as their state. Then we have to allow msg of State type, Hub call startGame()
    // TODO: 1. not coordinated, 1 for now. 2. Maybe winner of last game should be 1st player!
    currentPlayer = 1; // (Math.random() < 0.5) ? 1 : 2;
    
    gameInProgress = true;
    window.setupWindow();
  }



  /** Check if game has ended by means of checking if board is full. */
  private boolean boardFull() {
    if (nTrigsLeft == 0) {
      return true;
    }
    return false;
  }



  /** Builds triangle objects for the board with specific dimensions. Also counts up numTrigs */
  public ArrayList<Trigon> buildTrigs() {
    ArrayList<Trigon> boardTrigs = new ArrayList<Trigon>();
    double newBasisX, newBasisY;
    Trigon newUpTrig, newDnTrig;
    for (int J = 1 - NUM_TRIGS; J < NUM_TRIGS; J++) { // J is vertical index
      for (int i = 0; i < 2 * NUM_TRIGS - Math.abs(J); i++) { // i is horizontal index
        newBasisX = dims[0] * (Math.abs(J) / 2.0 + i);
        newBasisY = dims[1] * J;
        newUpTrig = new Trigon(newBasisX, newBasisY, UP_TRIG_NAME); // construct, ref. change
        newDnTrig = new Trigon(newBasisX, newBasisY, DN_TRIG_NAME);
        newUpTrig.setIndicesIJT(new int[] { i, J, UP_TRIG_NAME });
        newDnTrig.setIndicesIJT(new int[] { i, J, DN_TRIG_NAME });
        boardTrigs.add(newUpTrig);
        boardTrigs.add(newDnTrig);
        nTrigsLeft += 2;
      }
    } // Now, draw top and bottom row triangles.
    for (int i = 0; i < NUM_TRIGS; i++) { // i is horizontal index; |J| is NUM_TRIGS
      newBasisX = dims[0] * (NUM_TRIGS / 2.0 + i);
      newDnTrig = new Trigon(newBasisX, -NUM_TRIGS * dims[1], DN_TRIG_NAME);
      newUpTrig = new Trigon(newBasisX, NUM_TRIGS * dims[1], UP_TRIG_NAME);
      newDnTrig.setIndicesIJT(new int[] { i, -NUM_TRIGS, DN_TRIG_NAME });
      newUpTrig.setIndicesIJT(new int[] { i, NUM_TRIGS, UP_TRIG_NAME });
      boardTrigs.add(newDnTrig);
      boardTrigs.add(newUpTrig);
      nTrigsLeft += 2;
    }
    return boardTrigs;
  }



  // Applies new IDs for the board triangles.
  public void assignIndices() {
    double[] medCoords;
    int[] indices;
    for (Trigon trig : virtBrd) {
      medCoords = trig.getMedian(dims[0], dims[1]);
      indices = findIJType(medCoords[0], medCoords[1]); // indices {i, J, type}
      trig.setId(findTrigonAbsoluteIndex(indices[0], indices[1], indices[2])); // ...(i,J,type)
    }
  }



  public static int findTrigonAbsoluteIndex(int i, int J, int type) {
    int JTrans = (type == DN_TRIG_NAME) ? J + 1 : J;
    int trigsPerRow;
    int trigsInPrecedRows = 0;
    for (int k = 1 - NUM_TRIGS; k < JTrans; k++) {
      if (k > 0) {
        trigsPerRow = 2 * k - 1;
      } else {
        trigsPerRow = -2 * k + 1;
      }
      trigsInPrecedRows += 4 * NUM_TRIGS - trigsPerRow;
    }
    int finalTrigContribution;
    if (JTrans > 0) {
      finalTrigContribution = (type == DN_TRIG_NAME) ? 2 : 1;
    } else {
      finalTrigContribution = (type == UP_TRIG_NAME) ? 2 : 1;
    }
    int trigIdx = trigsInPrecedRows + 2 * (i + 1) - finalTrigContribution;
    return trigIdx;
  }



  private static void sortTrigsById(ArrayList<Trigon> board) {
    board.sort(new TrigOnIdComparator());
  }



  /** Returns 3 parameters for specifying triangle object on board based on coordinates */
  public int[] findIJType(double x, double y) {
    // x,y are the coordinates relative to left corner of baord hexagon being the Origin
    dims = TrigonalDims.getDims();
    int N = NUM_TRIGS;
    double uX = dims[0]; // pixels along x-axis representing a trigon edge
    double uY = dims[1]; // sqrt(3)/2 . xU
    double yTr = y;
    double xTr = x - Math.abs(yTr) / (2 * HF_SQT_3); // See documentation image for visual
    // Ignore positions out of range
    if (y < (uY / uX) * (xTr - N * uX) - N * uY || y > (-uY / uX) * (xTr - N * uX) + N * uY
              || xTr <= 0 || Math.abs(yTr) > N * uY) {
      return NOT_A_TRIG_INDICES;
    }
    // System.out.println("x:" + x + " y:" + y + " xTrans:" + xTrans + " yTrans:" + yTrans);
    int i = (int) (xTr / uX); // horizontal index
    int J; // vertical index, a hassle to determine
    int type;
    int j2 = (int) (yTr / uY); // helper index, J-related
    double xUCell = xTr - i * uX;
    double yUCell = yTr - j2 * uY; // J sign agree w/ y's, so +- good
    // Correction for slope (represented by capital Y in paper notes reference):
    double yUCellTrans2 = (yUCell >= 0) ? yUCell - uY : yUCell + uY;
    double posUCell = yUCellTrans2 / (HF_SQT_3 * xUCell);
    if (Math.abs(posUCell) > 1) { // i.e. if it's p < -1 || p > +1
      if (posUCell < 0) { // i.e. p < -1
        J = j2;
        type = DN_TRIG_NAME;
      } else { // i.e. p > +1
        J = j2;
        type = UP_TRIG_NAME;
      }
    } else {
      if (posUCell < 0) { // i.e. -1 <= p < 0
        J = j2 + 1;
        type = UP_TRIG_NAME;
      } else { // i.e. 0 <= p <= +1
        J = j2 - 1;
        type = DN_TRIG_NAME;
      }
    }
    return new int[] { i, J, type };
  }



  public void setWindow(TrigonalWindow window) {
    this.window = window;
  }

  public void setDims(double[] dims) {
    this.dims = dims;
  }

}
