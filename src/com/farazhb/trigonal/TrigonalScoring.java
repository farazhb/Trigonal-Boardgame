/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/2/14 - ... */
package com.farazhb.trigonal;

import java.util.ArrayList;
import com.farazhb.trigonal.TrigonalShape.Trigon;


public class TrigonalScoring implements TrigonalConsts {

  // List of all 6-trig hexagons, distinct but may overlap; for scoring at end. TL -> BR along rows
  private static ArrayList<Trigon[]> hexagons; // Note: It is static


  /** Returns scores in an int[] indexed by players, after calculating it: NumHexagons * 50 each */
  public static void findScores(TrigonalPlayer[] players) {
    int[] scores = new int[MAX_NUM_PLAYERS];
    int firstTrigOfHexOwner = 0;
    boolean sampleNotTaken;
    hexagonsLoop:
    for (Trigon[] hexagon : hexagons) {
      sampleNotTaken = true;
      for (Trigon trig : hexagon) {
        if (sampleNotTaken) { // Taking fist non-null trig's user ID as sample
          if (trig != null) {
            firstTrigOfHexOwner = trig.getOwnerId();
            sampleNotTaken = false;
          }
        }
        if (trig == null || trig.getOwnerId() != firstTrigOfHexOwner || trig.getOwnerId() == 0) {
          // null is possibility as intended
          continue hexagonsLoop;
        }
      }
      scores[firstTrigOfHexOwner - 1] += BASIC_HEXAGON_SCORE;
    }
    for (int i = 0; i < MAX_NUM_PLAYERS; i++) { // Setting Players' scores to those calculated
      players[i + 1].setScore(scores[i]);
    }
  }

  /** Intended to run once at game start after ordering of virtBrd. Precondition: trigs list must
   * have already been ordered from top=left to bottom right along rows. With some change, this
   * condition may be lifted if needed but would reduce efficiency. */
  public static void buildPtsNeighbors(ArrayList<Trigon> trigsList) {
    hexagons = new ArrayList<Trigon[]>();
    Trigon[] curPotNghbrs;
    int[] curTrigIdxs;
    int[][] neighborIdxs = new int[6][];
    int numTrigs = trigsList.size();

    for (int j = -NUM_TRIGS; j <= NUM_TRIGS; j++) { // j is vertical index of point
      for (int i = 0; i <= 2 * NUM_TRIGS - Math.abs(j); i++) { // i is horizontal index of point
        neighborIdxs[0] = new int[] { i - 1, j, UP_TRIG_NAME }; // 10.5 'clock direction
        neighborIdxs[1] = new int[] { i - 1, j - 1, DN_TRIG_NAME }; // 12 o'clock
        neighborIdxs[2] = new int[] { i, j, UP_TRIG_NAME }; // 1.5 'clock
        neighborIdxs[3] = new int[] { i - 1, j, DN_TRIG_NAME }; // 7.5 'clock
        neighborIdxs[4] = new int[] { i, j + 1, UP_TRIG_NAME }; // 6 'clock
        neighborIdxs[5] = new int[] { i, j, DN_TRIG_NAME }; // 4.5 'clock
        if (j > 0) {
          neighborIdxs[1] = new int[] { i, j - 1, DN_TRIG_NAME }; // correction for symmetry chng
        }
        if (j >= 0) {
          neighborIdxs[4] = new int[] { i - 1, j + 1, UP_TRIG_NAME }; // symmetry chng correction
        }

        // Need to look for trigons having the above indices above through list.
        Trigon curTrig;
        curPotNghbrs = new Trigon[6];
        nghbrLoop: // Label for the outer loop following we'll directly break out of from inner
        for (int potNghbr = 0; potNghbr < 6; potNghbr++) { // looking at pt's potential neighbors
          for (int trigIdx = 0; trigIdx < numTrigs; trigIdx++) {
            curTrig = trigsList.get(trigIdx);
            curTrigIdxs = curTrig.getIndicesIJT(); // mutable. change changes trig?!
            if (curTrigIdxs[0] == neighborIdxs[potNghbr][0] // .equals didn't work
                      && curTrigIdxs[1] == neighborIdxs[potNghbr][1]
                      && curTrigIdxs[2] == neighborIdxs[potNghbr][2]) {
              curPotNghbrs[potNghbr] = curTrig;
              potNghbr++; // Weird I know, helps skip loops, efficnc as potential nghbrs are ordered
              if (potNghbr == 6) { // i.e. we've finished the hexagon
                break nghbrLoop;
              }
              continue;
            }
          }
          // curPotNghbrs would be null
        }
        hexagons.add(curPotNghbrs);
      }
    }
    System.out.println("Number of board hexagons:  " + hexagons.size());
  }


}
