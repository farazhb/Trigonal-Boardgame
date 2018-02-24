/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;

import com.farazhb.trigonal.TrigonalTimerTasks.TurnTimer;
import javafx.util.Pair;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

// TODO: The dialog between players don't work. Game goes on between them, but the dialog request
// to end game is not transmitted. Also, maybe the player disconnected end game dialog works in part
// i.e. once players are disconnected, waiting leads to exceptions after the disconnect

// TODO: (1/3) selected trigons don't show (2/3) background image and fillRect are misplaced
// (3/3) game doesn't recognize or react when it has ended! does calculate scores at some point
// TODO: Maybe, maybe the 0th player is null and has no score. Make sure it exists!

// TODO: from Welcome, we instantiate hub and window. Instead do a controller which will do player
// and state and later, when time comes, window and therefrom board.
// TODO: With player intros, dilemma: Hub calls, upon collection of players introduced OR connected,
// I'd prefer having players sent before startGame() but think players aren't constructed by then
// Please rearrange, maybe later; it's independent, safe. But even if code works, it's fragile,
// e.g. if startGame() called but players not handshaken, and someone decides to click on board...
// TODO: Allow players to force start game before all have connected < max_num_players
// TODO: The TextArea numClicks left, use some layout to move it farther from edge; add spacer
// TODO: change all "message" and "msg" to relay or packet or something else
// TODO: Add Comments, esp. Javadoc for all classes; its functions have weird criteria e.g. fade


/** Represents player in Trigonal networked game. Window meant created by farazhb.trigonal.Main. */
public class TrigonalWindow extends JFrame implements TrigonalConsts {

  /** The state of the game, copy of the official state, which is stored on the server(?). Upon
   * change, state is sent as a message to this window. (It is actually sent to the TrigonalClient
   * object that represents the connection to the server.) Received state replaces this value; board
   * and UI updated accordingly in the newState() method, called by the TrigonalClient object. */
  private TrigonalState state;
  private TrigonalMove move;
  private TrigonalBoard board; // A panel that displays board. User moves by clicking on it.
  private JLabel message; // Displays messages to the user about the status of the game.
  private int myID; // The ID number that identifies the player using this window.
  private TrigonalPlayer player;
  private TrigonalClient cnxn; /* The Client obj for sending/receiving network messages. */
  public TurnTimer turnTimer; // Timer object keeping track of turn timer countdown on side-thread
  private JDialog endReportDialog;
  private JLabel timeTeller; // JLabel showing number of seconds left from turn
  private JTextPane numClicksTeller; // JLabel showing number of clicks left this turn
  private File bgMusicFile, clickSoundYesFile, clickSoundNoFile, clickSoundUndoFile;
  private TrigonalSound bgMusic;
  private final JButton askEndGameBtn = new JButton("Ask to end Game");
  private final JButton undoBtn = new JButton("Undo move");
  private JDialog invitation;
  private JDialog pauseDialog;
  private JLabel pauseNote;



  /** Creates and configures the window, opens a connection to the server, and makes visible.
   * Constructor Can block until connection established @param hostName the name or IP address of
   * the host where server is running. @param serverPortNumber the port number on the server
   * computer when the Hub is listening for connections. @throws IOException if some I/O error
   * occurs while trying to open the connection. @throws Client.DuplicatePlayerNameException if
   * playerName is already in use by another in game. */
  public TrigonalWindow(String hostName, int serverPortNumber, String myName, String bgImgPath,
            boolean imgIsTiled) throws IOException {
    super("Trigonal");
    state = new TrigonalState(this);
    cnxn = new TrigonalClient(this, hostName, serverPortNumber);
    myID = cnxn.getID();
    myName = (myName.length() == 0) ? "Player " + myID : myName;

    player = new TrigonalPlayer(myID, myName, pickMyColor());
    player.setImgInfo(bgImgPath, imgIsTiled);
    // TODO: Last 2 lines could be combined into a constructor as argument next line. not need var
    cnxn.send(new TrigonalMove(myID, PLAYER_INTRO, "", player));
  }

  public void setupWindow() {
    // TODO: Construction should hold on here until all players have been introduced!
    turnTimer = new TurnTimer();
    board = new TrigonalBoard(state);
    board.addMouseListener(new MouseAdapter() { // A mouse listener to respond to user's clicks.
      public void mousePressed(MouseEvent evt) {
        doMouseClick(evt.getX(), evt.getY());
      }
    });

    // TODO: Instead of asking for above var and transferring here, make HashMap of all res/Image
    // TODO: That HashMap, maybe read and build from a text file I update and often check/update it
    // The HashMpa listing makes visualizing pattern/image chooser dialog easier
    // TODO: Must send message here, all players communicate together, applyMessage w/ bgImage info
    board.loadImages(); // precondition: all players have been introduced

    bgMusicFile = new File(RES_PATH_BASE + "Sound/Babbling Brook-SoundBible.com-17660315.wav");
    clickSoundYesFile = new File(RES_PATH_BASE + "Sound/Woosh-Mark_DiAngelo-4778593.wav");
    clickSoundNoFile =
              new File(RES_PATH_BASE + "Sound/stephan_schutze-anvil_impact_1x-894647867.wav");
    clickSoundUndoFile = new File(RES_PATH_BASE + "Sound/flyby-Conor-1500306612.wav");

    message = new JLabel("Waiting for other players to connect.", JLabel.CENTER);
    message.setBackground(new Color(20, 20, 20));
    message.setForeground(Color.LIGHT_GRAY);
    message.setOpaque(true);
    message.setFont(MESSAGE_FONT);
    timeTeller = new JLabel("", JLabel.CENTER);
    timeTeller.setForeground(Color.PINK);
    timeTeller.setFont(TIMER_FONT);
    numClicksTeller = new JTextPane();
    numClicksTeller.setContentType("text/html");
    numClicksTeller.setOpaque(false);
    numClicksTeller.setEditable(false);

    JPanel subPanelInfo = new JPanel();
    subPanelInfo.setBackground(new Color(20, 20, 20));
    subPanelInfo.add(message, BorderLayout.CENTER); // TODO: The directions don't work! Layout?
    JPanel timePanel = new JPanel();
    timePanel.setBackground(new Color(0, 0, 0, 127));
    timePanel.add(timeTeller);
    board.setLayout(new BorderLayout());
    board.add(timePanel, BorderLayout.NORTH);
    board.add(numClicksTeller, BorderLayout.EAST);


    JPanel subButtons = new JPanel();
    subButtons.add(askEndGameBtn, BorderLayout.WEST);
    askEndGameBtn.setEnabled(false);
    subButtons.add(undoBtn, BorderLayout.EAST);
    undoBtn.setEnabled(false);
    board.add(subButtons, BorderLayout.SOUTH);


    JPanel content = new JPanel();
    content.setLayout(new BorderLayout(2, 2));
    content.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    content.setBackground(new Color(20, 20, 20, 100)); // TODO: Arg was just Color.GRAY
    content.add(board, BorderLayout.CENTER);
    content.add(subPanelInfo, BorderLayout.SOUTH);

    setContentPane(content);
    pack();
    setResizable(false);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    askEndGameBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        inviteEndGame();
      }
    });
    undoBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        undoMove();
      }
    });

    addWindowListener(new WindowAdapter() {
      // window's close box: sends Hub disconnect msg, exits, everyone is notified
      public void windowClosing(WindowEvent evt) {
        dispose();
        cnxn.disconnect(); // Send a disconnect message to the hub.
        try {
          Thread.sleep(333); // Wait to allow the message to be sent.
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        System.exit(0);
      }
    });
    setLocation(100, 2);
    setVisible(true);

    startBgMusic();
    newState(state, true);
  } // end of Window constructor


  private void startBgMusic() {
    bgMusic = new TrigonalSound("BackgroundMusicThread", bgMusicFile, true, true);
    bgMusic.start();
    if (bgMusic.isAlive()) {
      try { // TODO: Ask StackExchange if I really need this waiting!
        Thread.sleep(100); // The other thread needs time! 1 millisecond is enough!
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      synchronized (bgMusic) {
        bgMusic.option = new Pair<Byte, Integer>(OPTION_INPLAY, 4000);
        bgMusic.notify();
      }
    }
  }


  /** Called when user clicks Trigonal board. If legal move at a legal time, message sent to Hub to
   * inform of move. Hub changes state, sends new state to all players. Clients do not change state
   * directly, since the "official" state by Hub. This way guarantees players same board. */
  private void doMouseClick(int x, int y) { // x,y absolute coords, exclude prior transforms
    if (endReportDialog != null) { // The dialog's non-modal, and I have to dispose of it
      endReportDialog.dispose();
      endReportDialog = null; // TODO: bad to do this?
    }
    if (state == null || state.virtBrd == null) {
      return;
    }
    if (!state.gameInProgress) { // after game end any player can start anew by board click
      if (x > 0 && x < 100 && y > 0 && y < 100) { // Absolute board coords before any transform
        cnxn.send(new TrigonalMove(myID, NEW_GAME_REQ, ""));
      }
      return;
    }
    if (myID != state.currentPlayer) {
      return; // it's not this player's turn.
    }

    double[] xYTranslations = TrigonalDims.getXYTranslations();
    x -= xYTranslations[0];
    y -= xYTranslations[1];
    int[] ijt = state.findIJType(x, y); // x,y absolute (well rel panel corner) no transformation
    if (ijt.equals(NOT_A_TRIG_INDICES)) { // Out of valid range signal.
      return; // It works when use a constant as param rather than construct an int[]{-1,-1,-1}!
    }
    int trigIdx = TrigonalState.findTrigonAbsoluteIndex(ijt[0], ijt[1], ijt[2]);

    // If unclaimed, claim trig for player
    if (state.virtBrd.get(trigIdx).getOwnerId() == NEUTRAL_TRIG_PLAYER_ID) {
      move.addMoveSegment(trigIdx);
      undoBtn.setEnabled(true); // If already enabled, has no effect
      state.virtBrd.get(trigIdx).setOwnerId(myID); // state makes move change id
      state.virtBrd.get(trigIdx).setColor(player.getColor()); // state makes move change color

      if (state.nTrigsLeft == 0) {
        undoBtn.setEnabled(false); // forceEndGame(); not needed it seems
      }
      if (state.nClicksLeft == 1) { // End of current player's turn
        undoBtn.setEnabled(false);
        askEndGameBtn.setEnabled(false);

        if (state.players[myID] == null) {
          state.players[myID] = player;
        } // we can build this at the beginning once so we won't waste time/memory after built
        state.switchTurn();
        move.setPlayer(player);
        cnxn.send(move);
      } else { // Just claiming a trigon mid-turn
        board.takeNewState(state);
        board.repaint();
      }
      state.nClicksLeft--;
      numClicksTeller.setText(getNumClicksTxt(-1));
      TrigonalSound goodClick = new TrigonalSound("GoodClick", clickSoundYesFile, false, false);
      goodClick.start();

    } else { // clicked on a trigon that is not available
      TrigonalSound badClick = new TrigonalSound("BadClick", clickSoundNoFile, false, false);
      badClick.start();
    }
  }




  /** Manages different objects on current computer regarding the arriving message packet. It is
   * called by TrigonalClient as it relays message to window */
  public void declareMessage(TrigonalMove msg) {
    state.applyMessage(msg);
    if (msg.isMoveNotString() || msg.getMsgString().equals(PLAYER_DISCONNECTED)
              || msg.getMsgString().equals(END_GAME_ENOUGH_VOTES)) {
      // if (msg.getMsgString().equals(NEW_GAME_REQ)) {
      /* } else */ if (msg.getMsgString().equals(END_GAME_ENOUGH_VOTES)) {
        newState(state, false);
        askEndGameBtn.setEnabled(false); // not consequential practically, just appearance
      } else {
        newState(state, true);
      }
    }
  }


  /** Called when new state received from Hub. Stores new state in iVar state & updates UI
   * accordingly. Note that this method is called on the GUI event thread (using
   * SwingUtilitites.invokeLater()) to avoid synchronization problems. (Synchronization is an issue
   * when a method that manipulates the GUI is called from a thread other than the GUI event thread.
   * There is also the problem that a message can be received before constructor completed, errors
   * from uninitialized variables, if SwingUtilities.invokeLater() were not used.) */
  public void newState(TrigonalState state, boolean turnSwitched) {
    if (state.playerDisconnected) {
      JOptionPane.showMessageDialog(this, "Someone disconnected.\nGame is over!");
      System.exit(0);
    }
    board.takeNewState(state);
    board.repaint();
    if (state.virtBrd == null) {
      return; // haven't started yet -- waiting for other players
    } else if (!state.gameInProgress) { // Game has somehow ended
      board.takeNewState(state);
    } else { // Continuing game needs updating
      if (state.currentPlayer == myID) {
        message.setText("Your turn");
        numClicksTeller.setText(getNumClicksTxt(-1));
      } else {
        message.setText("An opponent's turn");
      }
      if (turnSwitched) {
        turnTimer.setTurnTimer(this, true); // "this" as in this window
        if (state.currentPlayer == myID) { // Note: Beginning of curr player's turn
          move = new TrigonalMove(myID); // move message (re)initiated, built on each trigon claim
          askEndGameBtn.setEnabled(true);
        }
      }
    }

  }


  public void gameEndedWork(String report) {
    if (bgMusic.isAlive()) {
      synchronized (bgMusic) {
        bgMusic.option = new Pair<Byte, Integer>(OPTION_OUTSTOP, -3000);
        bgMusic.notify();
      }
    }
    turnTimer.CancelTurnTimer();
    numClicksTeller.setText("");
    setTitle("Game End.");

    String scoresTxt = ""; // Setting text to messaeBar
    for (int i = 1; i <= MAX_NUM_PLAYERS; i++) {
      if (i == myID) {
        scoresTxt += "You: ";
      } else {
        scoresTxt += state.players[i].getName() + ": ";
      }
      scoresTxt += state.players[i].getScore() + "    ";
    }
    message.setText(scoresTxt); // Done setting text to messageBar

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    endReportDialog = new JDialog(null, "Game report card", ModalityType.MODELESS);
    // I had to make it MODELESS instead of APPLICATION_MODAL not to block board.paintComponent()
    endReportDialog.setLayout(new FlowLayout());
    endReportDialog.setAlwaysOnTop(true);
    JPanel reportBody = new JPanel(new FlowLayout());
    JTextArea reportLbl = new JTextArea(report);
    reportLbl.setEditable(false);
    JButton okayBtn = new JButton("OK");
    reportBody.add(reportLbl);
    reportBody.add(okayBtn);
    endReportDialog.add(reportBody);
    okayBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        endReportDialog.setVisible(false);
        endReportDialog.dispose();
      }
    });
    endReportDialog.pack();
    endReportDialog.setLocationRelativeTo(board);
    endReportDialog.setVisible(true);
  }


  public void postNewTime(int numSecLeft) {
    timeTeller.setText("" + numSecLeft);
  }

  public void forceEndGame() {
    state.gameInProgress = false;
    move.setPlayer(player);
    cnxn.send(move);
  }



  public void forceEndTurn() {
    state.switchTurn();
    if (state.currentPlayer == myID) {
      numClicksTeller.setText(getNumClicksTxt(0));
      move.setPlayer(player);
      cnxn.send(move);
    }
  }



  private Color pickMyColor() {
    return PLAYER_COLORS[myID - 1];
  }




  private String getNumClicksTxt(int optionalNum) {
    int num = (optionalNum == -1) ? state.nClicksLeft : optionalNum;
    String numClicksFieldText = "<html><font color=\"#BBBBBB77\" face=\"Courier New\" ";
    numClicksFieldText += "size=\"6\"> <b> <p align=\"center\">";
    switch (num) {
      case 0:
        switch (state.players.length) {
          case 3:
            numClicksFieldText += "Rival<br>Moves";
            break;
          default: // players.length isn't supposed to ever be < 3
            numClicksFieldText += "Rivals<br>Move";
        }
        break;
      case 1:
        numClicksFieldText += num + "<br>Move"; // singular
        break;
      default:
        numClicksFieldText += num + "<br>Moves"; // plural
    }
    return numClicksFieldText + "</b> </font> </html>";
  }


  private void undoMove() {
    int curIdx = move.popMoveSegment();
    state.virtBrd.get(curIdx).setOwnerId(0); // state changes board's trig's id back to neutral
    state.virtBrd.get(curIdx).setColor(state.players[0].getColor()); // color back to neutral
    board.takeNewState(state);
    board.repaint();
    state.nClicksLeft++;
    numClicksTeller.setText(getNumClicksTxt(-1));
    TrigonalSound undoClick = new TrigonalSound("UndoClick", clickSoundUndoFile, false, false);
    undoClick.start();
    if (move.noMoveClaim()) {
      undoBtn.setEnabled(false);
    }
  }



  private void pauseGame(boolean pause) {
    if (pause) {
      pauseDialog = new JDialog(this, "pauseNote", Dialog.ModalityType.MODELESS);
      pauseNote = new JLabel("GAME IS PAUSED!");
      pauseNote.setFont(pauseNote.getFont().deriveFont(24f));
      pauseNote.setForeground(new Color(255, 255, 255));
      pauseDialog.setUndecorated(true);
      pauseDialog.setBackground(new Color(0, 0, 0, 0));
      pauseDialog.add(pauseNote);
      turnTimer.pauseTimer();
      state.isPaused = true;
      board.repaint();
      pauseDialog.pack();
      pauseDialog.setLocationRelativeTo(board);
      pauseDialog.setVisible(true);
    } else {
      if (pauseDialog != null) {
        pauseDialog.setVisible(false);
        pauseDialog.dispose();
      }
      turnTimer.unPauseTimer();
      state.isPaused = false;
      board.repaint();
    }
  }



  private void popupDialog(String popMsg) {
    JDialog dialog = new JDialog(this, "PopUp", Dialog.ModalityType.MODELESS);
    dialog.setUndecorated(true); // removes title bar/closeButton; necessary for transparency
    dialog.setLayout(new FlowLayout());
    dialog.setAlwaysOnTop(true);
    JLabel popupLabel = new JLabel(popMsg);
    popupLabel.setFont(popupLabel.getFont().deriveFont(20f));
    dialog.add(popupLabel);
    dialog.setOpacity(0f);
    dialog.pack();
    dialog.setLocationRelativeTo(board);
    dialog.setVisible(true);

    Timer timer1 = new Timer(POPUP_TIME_STEP, new ActionListener() {
      int counter = 0;
      float increment = POPUP_TIME_STEP / (POPUP_DELAY / 3f);
      float invIncr = 1 / increment;
      float popupOpacity = 0f;

      public void actionPerformed(ActionEvent e) {
        counter++;
        popupOpacity += increment; // /2 so msg stays opaque a bit
        popupOpacity = (popupOpacity > 1f) ? 1 : popupOpacity;
        if (counter > invIncr) {
          ((Timer) e.getSource()).stop();
        }
        dialog.setOpacity(popupOpacity);
      } // makes popup fade in
    });

    Timer timer2 = new Timer(POPUP_TIME_STEP, new ActionListener() {
      float increment = POPUP_TIME_STEP / (POPUP_DELAY / 2f);
      float popupOpacity = 1f;

      public void actionPerformed(ActionEvent e) {
        popupOpacity -= increment; // /2 so msg stays opaque a bit
        if (popupOpacity < 0f) {
          dialog.setVisible(false);
          dialog.dispose();
          return;
        }
        dialog.setOpacity(popupOpacity);
      } // makes popup fade out
    });
    timer1.setInitialDelay(0);
    timer2.setInitialDelay(POPUP_DELAY);
    timer1.start();
    timer2.start(); // timers independent, not share var, could potentially shoot at any time.
    pauseGame(false); // Unpauses
  }


  private void inviteEndGame() { // called upon clicking button
    JDialog invite = new JDialog(null, "Invite to end game?", ModalityType.APPLICATION_MODAL);
    invite.setLayout(new FlowLayout());

    JPanel invBody = new JPanel(new FlowLayout());
    JLabel inviteLbl = new JLabel("Message to invite to end Game: ");
    JTextField endGameRequest = new JTextField(TEXTFIELD_SIZE);
    JButton okayBtn = new JButton("OK");
    JButton cnclBtn = new JButton("Cancel");
    invBody.add(inviteLbl);
    invBody.add(endGameRequest);
    invBody.add(okayBtn);
    invBody.add(cnclBtn);
    invite.add(invBody);
    okayBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        cnxn.send(new TrigonalMove(myID, END_GAME_INVT, endGameRequest.getText()));
        invite.setVisible(false);
        invite.dispose();
      }
    });
    cnclBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        invite.setVisible(false);
        invite.dispose();
      }
    });
    invite.pack();
    invite.setLocationRelativeTo(board);
    invite.setVisible(true);
  }

  /** This method is called by state upon reception of message indicating end Game invitation. */
  public void invitedToEndGame(int sender, String auxMsg) {
    pauseGame(true);
    invitation = new JDialog(null, "invitedToEndGameDialog", ModalityType.APPLICATION_MODAL);
    invitation.setLayout(new FlowLayout());
    JPanel inviteBody = new JPanel(new FlowLayout());

    if (sender != myID) { // Someone else has asked to end Game
      JTextField explain = new JTextField(TEXTFIELD_SIZE);
      inviteBody.add(explain);
      JLabel inviteLbl =
                new JLabel(state.players[sender].getName() + " asked \nto end the game. " + auxMsg);
      JButton yesBtn = new JButton("Yea");
      JButton noBtn = new JButton("Nay");
      invitation.add(inviteBody);
      invitation.add(inviteLbl);
      invitation.add(yesBtn);
      invitation.add(noBtn);
      yesBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          cnxn.send(new TrigonalMove(myID, END_GAME_VOTE, ""));
        }
      });
      noBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          cnxn.send(new TrigonalMove(myID, CANCEL_END_GAME_INVT, explain.getText()));
        }
      });
      invitation.pack();
      invitation.setLocationRelativeTo(this);
      invitation.setVisible(true);

    } else { // You've asked to end game
      JTextField cancelExplain = new JTextField(TEXTFIELD_SIZE);
      inviteBody.add(cancelExplain);
      JLabel inviteLbl = new JLabel("You've asked to end this game.\n Cancel this?" + auxMsg);
      JButton cancelBtn = new JButton("Cancel");
      invitation.add(inviteBody);
      invitation.add(inviteLbl);
      invitation.add(cancelBtn);
      cancelBtn.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent arg0) {
          cnxn.send(new TrigonalMove(myID, CANCEL_END_GAME_INVT, cancelExplain.getText()));
        }

      });
      invitation.pack();
      invitation.setLocationRelativeTo(this);
      invitation.setVisible(true);
    }
  }

  public void closeDialogs(String note) {
    if (invitation != null) {
      invitation.setVisible(false);
      invitation.dispose();
    }
    popupDialog(note);
  }

}
