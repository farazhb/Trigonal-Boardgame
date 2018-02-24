/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;

import com.farazhb.netgame.*;
import java.io.IOException;


/** A "Hub" for Trigonal game. Only one Hub per game, in a centralized Hub-Clients network. Official
 * game state on Hub. Upon change, Hub sends all players new state, so all see same state. */
public class TrigonalHub extends Hub implements TrigonalConsts {

  TrigonalPlayer[] players = new TrigonalPlayer[MAX_NUM_PLAYERS + 1];
  int numPlayerIntros = 0; // Number of players having been introduced
  int numVotesEndGame = 0; // Num votes to end game

  /** Create a hub, listening on the specified port. Calls setAutoreset(true), which causes output
   * stream to each client to be reset before sending each message, essential bcs same state object
   * will be transmitted over and over, with changes between each transmission. @param port port
   * number hub will listen on. @throws IOException if a listener cannot open on specified port. */
  public TrigonalHub(int port) throws IOException {
    super(port);
    setAutoreset(true);
  }

  /** Responds when a message is received from a client. In this case, the message is applied to the
   * game state, by calling state.applyMessage(). Then the possibly changed state is transmitted to
   * all connected players. */
  protected void messageReceived(int playerID, Object message) {
    TrigonalMove msg = (TrigonalMove) message;
    String msgText = msg.getMsgString();
    String auxMsg = msg.getAuxMsgString();
    if (msgText.equals(END_GAME_INVT) || msgText.equals(END_GAME_VOTE)) {
      numVotesEndGame++;
      if (numVotesEndGame == MAX_NUM_PLAYERS) {
        sendToAll(new TrigonalMove(playerID, END_GAME_ENOUGH_VOTES, "")); // on behalf of last voter
        numVotesEndGame = 0; // Have to reset for potential next game
      }
      return; // TODO: omit this?
    } else if (msgText.equals(PLAYER_INTRO)) {
      players[playerID] = msg.getPlayer();
      numPlayerIntros++;
      if (numPlayerIntros == MAX_NUM_PLAYERS) {
        sendToAll(new TrigonalMove(playerID, PLAYER_INTROS_DONE, "", players));
      }
      return; // needed here since we just want to collect players until players full
    } else if (msgText.equals(CANCEL_END_GAME_INVT)) {
      numVotesEndGame = 0;
      sendToAll(new TrigonalMove(playerID, CANCEL_END_GAME_INVT, auxMsg));
    }
    sendToAll(message); // Frankly essentially this method's job: To take a message and send it
  }

  /** This method is called when a player connects. If that player is the second player, then the
   * server's listening socket is shut down (because only n players are allowed), the first game is
   * started, and the new state -- with the game now in progress -- is transmitted to all. */
  protected void playerConnected(int playerID) {
    if (getPlayerList().length == MAX_NUM_PLAYERS) {
      shutdownServerSocket();
      sendToAll(new TrigonalMove(playerID, NEW_GAME_REQ, ""));
    }
  }

  /** This method is called when a player disconnects. This will end the game and cause the other
   * player to shut down as well. This is accomplished by setting state.playerDisconnected to true
   * and sending the new state to the remaining player, if there is one, to notify that player that
   * the game is over. */
  protected void playerDisconnected(int playerID) {
    sendToAll(new TrigonalMove(playerID, PLAYER_DISCONNECTED, ""));
  }

}
