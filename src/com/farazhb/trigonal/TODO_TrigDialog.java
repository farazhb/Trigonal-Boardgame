/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/3/8 - ... */
package com.farazhb.trigonal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class TODO_TrigDialog extends JDialog implements TrigonalConsts {

  JFrame frame;
  String title;
  ModalityType modality;
  String message;
  byte queryOption;
  // TODO: Please implement.
  // TODO: Our dialog in Window is created from a JOptionPane which takes format e.g. PLAIN,
  // OK-CANCEL, .... How can I keep that format here with this JDialog extending class ?

  public TODO_TrigDialog(JFrame parent, String title, ModalityType modality, String msg, byte queryOpt) {
    super(parent, title, modality);
    if (parent != null) {
      Dimension parentSize = parent.getSize();
      Point p = parent.getLocation();
      setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
    }

    // TODO: Set instance variables above to these. If just need the constructor, we won't need to

    JPanel panel = new JPanel(new FlowLayout());

    JTextField explain = new JTextField(20);
    JLabel message = new JLabel(msg);

    JButton yesBtn = new JButton();

    boolean a = true; // This is just so incomplete code doens't give error messages
    if (a) {
      yesBtn = new JButton();
      yesBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          // cnxn.send(new TrigonalMove(myID, END_GAME_VOTE));
          // TODO: about above and below lines, should I pass cnxn here? or pass window here and
          // call
          // a method therein?
          // Utilities
        }

      });
    } else if (!a) {

    } else {

    }

    getContentPane().add(message);
    getContentPane().add(explain);
    getContentPane().add(yesBtn, BorderLayout.SOUTH);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    
    pack();
    setVisible(true);
  }

  private void actionPerformed(ActionEvent e) {
    setVisible(false);
    dispose();
  }

}
