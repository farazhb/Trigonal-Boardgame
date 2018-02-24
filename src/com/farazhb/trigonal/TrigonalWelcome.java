/** Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;

import com.farazhb.netgame.Hub;
import javax.swing.*;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;


public class TrigonalWelcome /* extends JFrame */ implements TrigonalConsts {
  // TODO: Might as well be static! don't need "extends JFrame"

  // iVars
  private JFrame welcomeFrame;
  JLabel message = new JLabel("Trigonal!", JLabel.CENTER); // Why is this the first line here?!
  private final JTextField listeningPortInput = new JTextField("" + DEFAULT_PORT, 5);
  private final JTextField hostInput = new JTextField("localhost", 30);
  private final JTextField nameBox = new JTextField(30);
  private final JTextField imgNameBox = new JTextField("BG-test.png", 20);
  private final JTextField connectPortInput = new JTextField("" + DEFAULT_PORT, 5);
  private final JRadioButton selectServerMode = new JRadioButton("Start new Trigonal");
  private final JRadioButton selectClientMode = new JRadioButton("Connect to Trigonal");
  private final JRadioButton tiledYesBtn = new JRadioButton("Yes");
  private final JRadioButton tiledNoBtn = new JRadioButton("No");
  private final JButton okayBtn = new JButton("Okay");
  private final JButton cnclBtn = new JButton("Cancel");
  private boolean imageIsTiled;


  public TrigonalWelcome(boolean actualRun) { // meant to be false to initialise as empty object
  }

  public TrigonalWelcome() {
    super();
    welcome();
  }

  public void welcome() {
    welcomeFrame = new JFrame();
    welcomeFrame.setEnabled(true);
    welcomeFrame.setLocation(400, 200);
    welcomeFrame.setResizable(true);
    welcomeFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    welcomeFrame.setTitle("Trigonal12345"); // Doesn't show up bcs undecorated
    welcomeFrame.setBackground(WELCOME_BG_COLOR);

    message.setFont(WELCOME_FONT);

    selectServerMode.setOpaque(false);
    selectClientMode.setOpaque(false);


    ButtonGroup clientServerBtnGroup = new ButtonGroup();
    clientServerBtnGroup.add(selectServerMode);
    clientServerBtnGroup.add(selectClientMode);
    ActionListener clientServerRadioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectServerMode) {
          listeningPortInput.setEnabled(true);
          hostInput.setEnabled(false);
          connectPortInput.setEnabled(false);
          listeningPortInput.setEditable(true);
          hostInput.setEditable(false);
          connectPortInput.setEditable(false);
        } else {
          listeningPortInput.setEnabled(false);
          hostInput.setEnabled(true);
          connectPortInput.setEnabled(true);
          listeningPortInput.setEditable(false);
          hostInput.setEditable(true);
          connectPortInput.setEditable(true);
        }
      }
    };
    selectServerMode.addActionListener(clientServerRadioListener);
    selectClientMode.addActionListener(clientServerRadioListener);
    selectServerMode.setSelected(true);
    hostInput.setEnabled(false);
    connectPortInput.setEnabled(false);
    hostInput.setEditable(false);
    connectPortInput.setEditable(false);


    ButtonGroup imageTiledGroup = new ButtonGroup();
    imageTiledGroup.add(tiledYesBtn);
    imageTiledGroup.add(tiledNoBtn);
    ActionListener tiledRadioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == tiledYesBtn) {
          imageIsTiled = true;
        } else {
          imageIsTiled = false;
        }
      }
    };
    tiledYesBtn.addActionListener(tiledRadioListener);
    tiledNoBtn.addActionListener(tiledRadioListener);
    tiledYesBtn.setSelected(true);
    imageIsTiled = true;


    ButtonGroup okCancelGroup = new ButtonGroup();
    okCancelGroup.add(okayBtn);
    okCancelGroup.add(cnclBtn);
    ActionListener okCancelListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        commit(e);
      }
    };
    okayBtn.addActionListener(okCancelListener);
    cnclBtn.addActionListener(okCancelListener);



    JPanel inputPanel = new JPanel();
    inputPanel.setOpaque(false);
    welcomeFrame.setContentPane(inputPanel);
    welcomeFrame.setAlwaysOnTop(true);

    inputPanel.setLayout(new GridLayout(0, 1, 5, 5));
    Border innerBorder = BorderFactory.createEmptyBorder(10, 100, 40, 100); //////////////////////
    Border compBorder = BorderFactory
              .createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 2), innerBorder);
    inputPanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(WELCOME_BG_COLOR.darker().darker(), 10), compBorder));
    inputPanel.add(message);
    inputPanel.add(selectServerMode);

    JPanel row;
    row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new FlowLayout(FlowLayout.LEFT));
    row.add(Box.createHorizontalStrut(40));
    row.add(new JLabel("Listen on port: "));
    row.add(listeningPortInput);
    inputPanel.add(row);

    inputPanel.add(selectClientMode);

    row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new FlowLayout(FlowLayout.LEFT));
    row.add(Box.createHorizontalStrut(40));
    row.add(new JLabel("Computer: "));
    row.add(hostInput);
    inputPanel.add(row);

    row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new FlowLayout(FlowLayout.LEFT));
    row.add(Box.createHorizontalStrut(40));
    row.add(new JLabel("Port Number: "));
    row.add(connectPortInput);
    inputPanel.add(row);

    row = new JPanel(); // just a vertical spacer
    row.setOpaque(false);
    row.add(Box.createHorizontalStrut(100));
    welcomeFrame.add(row);

    row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new FlowLayout(FlowLayout.LEFT));
    row.add(Box.createHorizontalStrut(40));
    row.add(new JLabel("Your Name: "));
    row.add(nameBox);
    inputPanel.add(row);


    row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new FlowLayout(FlowLayout.LEFT));
    row.add(Box.createHorizontalStrut(40));
    row.add(new JLabel("Image: "));
    row.add(imgNameBox);
    row.add(new JLabel("   Tiled? "));
    row.add(tiledYesBtn);
    row.add(tiledNoBtn);
    inputPanel.add(row);


    row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new FlowLayout(/* FlowLayout.LEFT */));
    row.add(okayBtn);
    row.add(Box.createHorizontalStrut(100));
    row.add(cnclBtn);
    welcomeFrame.add(row);


    welcomeFrame.setUndecorated(true); // for setting opacity < 1 as in below line
    welcomeFrame.setOpacity(0.9f); // TODO: Secure this, might not work on some systems

    welcomeFrame.pack();
    okayBtn.requestFocusInWindow();
    welcomeFrame.setVisible(true);

  }


  /* Show dialog, get user's response and start game if player not cancel. If choose to run as the
   * server then a TrigonalHub (server) created, then Window is created connecting to server on
   * localhost, which was just created. Game waits for more connections. If choose to connect to
   * existing server, only a Window is created, connect to specified server running host. */
  private void commit(ActionEvent evt) {
    int port = 1;
    String name; // Player's name
    String bgImageName = imgNameBox.getText();
    String bgImgPath = (bgImageName.equals("")) ? "" : RES_PATH_BASE + "Image/" + bgImageName;

    if (evt.getSource() == okayBtn) {
      okayBtn.setSelected(true);
      welcomeFrame.dispatchEvent(new WindowEvent(welcomeFrame, WindowEvent.WINDOW_CLOSING));
      name = nameBox.getText().trim();
      if (selectServerMode.isSelected()) {
        try {
          port = Integer.parseInt(listeningPortInput.getText().trim());
          if (port <= 0)
            throw new Exception();
        } catch (Exception e) {
          message.setText("Illegal port number!");
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
          new TrigonalWelcome();
        }
        Hub hub = new Hub(false);
        try {
          hub = new TrigonalHub(port);
          new TrigonalWindow("localhost", port, name, bgImgPath, imageIsTiled);
        } catch (IOException e) {
          message.setText("Could not connect to server on localhost!!");
          hub.shutDownHub();
        } catch (Exception e) {
          message.setText("Error: Can't listen on port " + port);
          listeningPortInput.selectAll();
          listeningPortInput.requestFocus();
        }

      } else {
        String host;
        host = hostInput.getText().trim();
        if (host.length() == 0) {
          message.setText("You must enter a computer name!");
          hostInput.requestFocus();
        }
        try {
          port = Integer.parseInt(connectPortInput.getText().trim());
          if (port <= 0)
            throw new Exception();
        } catch (Exception e) {
          message.setText("Illegal port number!");
          connectPortInput.selectAll();
          connectPortInput.requestFocus();
        }
        try {
          new TrigonalWindow(host, port, name, bgImgPath, imageIsTiled);
        } catch (IOException e) {
          message.setText("Could not connect to specified host and port.");
          hostInput.selectAll();
          hostInput.requestFocus();
        }
      }

    } else if (evt.getSource() == cnclBtn) {
      cnclBtn.setSelected(true);
      welcomeFrame.dispose();
      System.exit(0);
    }

    return;
  }

}
