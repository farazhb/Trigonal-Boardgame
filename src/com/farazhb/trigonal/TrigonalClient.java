/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;

import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.farazhb.netgame.Client;


/** Defines the client object that handles communication with the Hub. It's the Player */
public class TrigonalClient extends Client {

  private TrigonalWindow trigWindow;

  /** Connect to the hub at a specified host name and port number. */
  public TrigonalClient(TrigonalWindow trigWindow, String hubHostName, int hubPort)
            throws IOException {
    super(hubHostName, hubPort);
    this.trigWindow = trigWindow;
  }

  /** Responds to message received from Hub. Only supported messages are State objects here. Upon
   * receiving, Window.newState() called. To avoid synch problems, that method is called using
   * SwingUtilities.invokeLater() so will run in the GUI event thread. */
  protected void messageReceived(final Object message) {
    TrigonalMove msg = (TrigonalMove) message;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { // calls a method at the end of the TicTacToeWindow class
        trigWindow.declareMessage(msg);
      }
    });
  }

  /** If a shutdown message is received from the Hub, the user is notified and the program ends. */
  protected void serverShutdown(String message) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(trigWindow, "An opponent disconnected.\nGame is over!");
        System.exit(0);
      }
    });
  }

}

