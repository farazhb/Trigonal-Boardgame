/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/3/2 - ... */
package com.farazhb.trigonal;

import java.io.Serializable;
import java.util.Stack;


/** Contains as much info as a message to/from the Hub would do. */
public class TrigonalMove implements Serializable {

  // iVars:
  private boolean moveNotString; // whether move or only String TODO: If >2 choices, make into enum
  private Stack<Integer> claimList; // Track turn moves for Undo. Also moves to send
  private TrigonalPlayer player; // Player with info
  private TrigonalPlayer[] players; // Players w/ info. This is just for initial introductions
  private int sender; // This is set by the Hub before sendToAll(message)
  private String msgString;
  private String auxiliaryMsg;


  // TODO: We don't need so many constructors, e.g. 3,4. Find alternate way of sending player(s)
  public TrigonalMove(int sender) { // Constructor 1/4
    moveNotString = true;
    this.sender = sender;
    claimList = new Stack<Integer>();
    msgString = ""; // I assign it as empty String here to avoid NullPtrExc in State when getString
    auxiliaryMsg = "";
  } // end of constructor 1/4

  public TrigonalMove(int sender, String msgString, String auxiliaryTxt) { // Constructor 2/4
    moveNotString = false;
    this.sender = sender;
    this.msgString = msgString;
    auxiliaryMsg = auxiliaryTxt;
  } // end of constructor 2/4

  // Constructor 3/4
  public TrigonalMove(int sender, String msgString, String auxiliaryTxt, TrigonalPlayer player) {
    moveNotString = false;
    this.player = player;
    this.sender = sender;
    this.msgString = msgString;
    auxiliaryMsg = auxiliaryTxt;
  } // end of constructor 3/4

  // Constructor 4/4
  public TrigonalMove(int sender, String msgString, String auxiliaryTxt, TrigonalPlayer[] players) {
    moveNotString = false;
    this.players = players;
    this.sender = sender;
    this.msgString = msgString;
    auxiliaryMsg = auxiliaryTxt;
  } // end of constructor 4/4



  public boolean isMoveNotString() {
    return moveNotString;
  }

  public String getAuxMsgString() {
    return auxiliaryMsg;
  }

  public String getMsgString() {
    return msgString;
  }

  public int getSender() {
    return sender;
  }
  /* public void setSender(int senderID) { sender = senderID; } */

  public int getMoveSize() {
    return claimList.size();
  }

  public TrigonalPlayer[] getPlayers() {
    return players;
  }

  public void setPlayer(TrigonalPlayer player) {
    this.player = player;
  }

  public TrigonalPlayer getPlayer() {
    return player;
  }

  public void addMoveSegment(int trigIndex) {
    claimList.push(trigIndex); // Adding just-claimed trigon to undoList
  }

  public int popMoveSegment() {
    return claimList.pop(); // Adding just-claimed trigon to undoList
  }

  public boolean noMoveClaim() {
    return claimList.isEmpty();
  }

}
