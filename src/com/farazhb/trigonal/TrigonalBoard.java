/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;

import com.farazhb.trigonal.TrigonalShape.Trigon;
import javafx.util.Pair;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage; // TODO: now for source of bgImg. all g2 should be a bufImg too
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JPanel;


/** A JPanel that draws the TicTacToe board. */
public class TrigonalBoard extends JPanel implements TrigonalConsts { // Defines the board object

  // iVars: If assignment included, set at construction
  private TrigonalState state;
  private int panW, panH;
  private double[] dims;
  private ArrayList<Pair<BufferedImage, Boolean>> bgImages; // list of pairs<backgrndImg, isTiled>


  public TrigonalBoard(TrigonalState state) {
    int[] panDims = TrigonalDims.getSuitablePanWH(SCREEN_PAN_SZ_RATIO);
    this.setPreferredSize(new Dimension(panDims[0], panDims[1]));
    this.setSize(new Dimension(panDims[0], panDims[1]));
    findDims(); // Finds realXU, realYU of TrigonalDims
    this.setBackground(BOARD_BG_COLOR);
    this.state = state;
  }

  /** Calculates values of iVars defining unit dimensions and scale factors of panel component */
  private void findDims() {
    panW = getWidth();
    panH = getHeight();
    dims = TrigonalDims.getEffDims(panW, panH); // dims { realXU, realYU }
  }

  /** Called upon repaint(). Draws onto Graphics context Board elements incl. trigons and lines. */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    // TODO: I think it would be nice to clear the g off previosuly drawn things!
    Graphics2D g2 = (Graphics2D) g;
    if (state == null || state.virtBrd == null) {
      return;
    }
    if (state.isPaused) {
      return;
    }
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    AffineTransform trans0 = g2.getTransform();
    double[] xYTranslations = TrigonalDims.getXYTranslations();
    AffineTransform trans1 = new AffineTransform();
    trans1.translate(xYTranslations[0], xYTranslations[1]);
    // Drawing the created triangles
    ArrayList<Trigon> boardTrigs = state.virtBrd;
    // System.out.println(boardTrigs.size() + " trigons in total.");
    drawBackgroundImages(g2, trans0, trans1);
    g2.setTransform(trans1);
    g2.setStroke(new BasicStroke(1));
    // for (Trigon trig : boardTrigs) {
    // trig.drawTrig(g2, dims[0], dims[1], bgImg, imgIsTiled);
    // TODO: I'm passing both the image and the graphics context. If possible, change it to use
    // just one of them. If isTiled, we have the image ready. Otherwise, still need to clip
    // TODO: Somehow put sleep() between drawing triangles, like flush, wait, notify, anything
    // }
    drawBorders(g2);
    // labelTrigs(g2, boardTrigs, xYTranslations); // TODO: Toggle for debug, indexes trigons
    g2.setTransform(trans0);
  }

  // private void labelTrigs(Graphics2D g2, ArrayList<Trigon> trigs, double[] xYTranslations) {
  // g2.setColor(Color.ORANGE);
  // for (Trigon trig : trigs) {
  // double[] median = trig.getMedian(dims[0], dims[1]);
  // int[] ijt = state.findIJType(median[0], median[1]);
  // int idx = TrigonalState.findTrigonAbsoluteIndex(ijt[0], ijt[1], ijt[2]);
  // Font font = g2.getFont();
  // g2.setFont(font.deriveFont(14f));
  // g2.drawString("" + idx, (float) (median[0] - 10), (float) (median[1]));
  // }
  // }

  /** Draws border lines between the triangles on the board. */
  private void drawBorders(Graphics2D g2) {
    double realXU = dims[0];
    double realYU = dims[1];
    g2.setColor(BORDER_COLOR);
    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    // 3rd param above not important; I had to add to match constructor arguments
    double x1, y1, x2, y2;
    for (int n = -NUM_TRIGS; n <= NUM_TRIGS; n++) {
      x1 = realXU * Math.abs(n) / 2.0;
      y1 = realYU * n;
      x2 = realXU * (2 * NUM_TRIGS - Math.abs(n) / 2.0);
      y2 = realYU * n;
      g2.draw(new Line2D.Double(x1, y1, x2, y2));
    }
    for (int n = 0; n <= NUM_TRIGS; n++) {
      x1 = realXU * n / 2.0;
      y1 = realYU * n;
      x2 = realXU * (n + NUM_TRIGS / 2.0);
      y2 = realYU * -NUM_TRIGS;
      g2.draw(new Line2D.Double(x1, y1, x2, y2));
      g2.draw(new Line2D.Double(x1, -y1, x2, -y2));
    }
    for (int n = 1; n <= NUM_TRIGS; n++) {
      x1 = realXU * (1.5 * NUM_TRIGS + n / 2.0);
      y1 = realYU * (-NUM_TRIGS + n);
      x2 = realXU * (NUM_TRIGS / 2.0 + n);
      y2 = realYU * NUM_TRIGS;
      g2.draw(new Line2D.Double(x1, y1, x2, y2));
      g2.draw(new Line2D.Double(x1, -y1, x2, -y2));
    }
  }



  // TODO: We do too much work for each repaint() at every click/second. Somehow have it draw only
  // for added trigons one by one, not redrawing entire board. For timer indicator separate panel
  private void drawBackgroundImages(Graphics2D g2, AffineTransform t0, AffineTransform t1) {
    ArrayList<ArrayList<Path2D>> domains = findInfluenceAreas();

    for (int id = 1; id <= MAX_NUM_PLAYERS; id++) {
      ArrayList<Path2D> domain = domains.get(id);

      if (state.players[id].getImageInfo().getValue()) { // i.e. if the bgImage is tiled

        g2.setTransform(t0);

        BufferedImage bgImgIni = // holds bgImg over a translucent "background"
                  new BufferedImage((int) dims[0], (int) dims[1], BufferedImage.TRANSLUCENT);
        Graphics2D bgG2 = bgImgIni.createGraphics();
        for (Path2D shape : domain) {
          bgG2.setClip(new Trigon(0, 0, UP_TRIG_NAME).buildShape(dims[0], dims[1]));
        }
        // TODO: This is wrong. The small BufImg should just be set to small triangular unit and
        // just be added to each position of the trig. Loop through player.trig and draw there
        // TODO: Instead of loop above we should do just get one shape but below it will become a
        // loop wherein we'll draw that which we had in many places.
        bgG2.drawImage(bgImages.get(id).getKey(), 0, 0, this); // 1st arg img
        bgG2.dispose();

      } else { // i.e. bgImg isn't tiled
        BufferedImage bgImgIni = new BufferedImage(panW + 10, panH, BufferedImage.TRANSLUCENT);
        Graphics2D bgG2 = bgImgIni.createGraphics();
         bgG2.clearRect(0, 0, panW, panH); ////////////////////////////////
         for (Path2D shape : domain) {
           // TODO: Change/Convert Path2D to Area object, in TrigonalShape as well!
           bgG2.setClip(shape);
           System.out.println("A SHAPE IN DOMAIN");
         }
        bgG2.setClip(new Rectangle2D.Double(20, 30, 200, 300)); /////////////////////////
        bgG2.drawImage(bgImages.get(id).getKey(), 0, 0, this);
        bgG2.setColor(state.players[id].getColor());
        bgG2.fillRect(0, 0, panW + 10, panH); // panW is 10 units too short!
        bgG2.dispose();
        g2.setTransform(t0);
        g2.drawImage(bgImgIni, 0, 0, this);
      }

      // TODO: fix panW and panH! e.g. getWidth() we know is wrong by 10 units (pixels?)
    }
    // TODO: set bgImg or bgImgIni to null? maybe wrong, maybe not need. garbage collected?
  }


  // TODO: Better way: Load/draw all state at once: setClip(shape) for all trigons belonging to
  // a player with certain id. Then, drawthatImage. Do this all to a BufffImg, draw that to g2.
  public void loadImages() {

    bgImages = new ArrayList<Pair<BufferedImage, Boolean>>(MAX_NUM_PLAYERS);
    bgImages.add(new Pair<BufferedImage, Boolean>(null, false)); // filling index 0
    // the Boolean: if isTiled, image source is small icon. Otherwise, it's screen-wide
    for (int id = 1; id <= MAX_NUM_PLAYERS; id++) {
      Pair<BufferedImage, Boolean> entry;
      BufferedImage entryK;
      boolean entryV = state.players[id].getImageInfo().getValue();
      String bgImgPath = state.players[id].getImageInfo().getKey();
      File imgRef = new File(state.players[id].getImageInfo().getKey());
      // URL imgRef = getClass().getClassLoader().getResource(bgImgPath);
      try {
        entryK = (bgImgPath == null || bgImgPath.trim().isEmpty()) ? null : ImageIO.read(imgRef);
      } catch (IOException e) {
        entryK = null;
        e.printStackTrace();
      }
      bgImages.add(id, new Pair<BufferedImage, Boolean>(entryK, entryV));
    }
    // If the clipping and bgImgIni work, then we may just use the bgImg alone, ...Ini so named too
  }


  /** Returns ArrayList of shapes collectively belonging to each player. */
  private ArrayList<ArrayList<Path2D>> findInfluenceAreas() {
    ArrayList<ArrayList<Path2D>> domains = new ArrayList<ArrayList<Path2D>>(MAX_NUM_PLAYERS + 1);
    for (int id = 0; id <= MAX_NUM_PLAYERS; id++) {
      domains.add(new ArrayList<Path2D>()); // filling . TODO: Ini capacity be board's tot numTrigs
    }
    ArrayList<Trigon> boardTrigs = state.virtBrd;
    for (int id = 1; id <= MAX_NUM_PLAYERS; id++) {
      for (Trigon trig : boardTrigs) { // checking each trig; if belongs to player, add to domain
        if (trig.getOwnerId() == state.players[id].getId()) {
          domains.get(state.players[id].getId()).add(trig.getTrigShape());
        }
      }
    }
    return domains;
  }


  public void takeNewState(TrigonalState newState) {
    state = newState;
    state.setDims(dims);
  }


  public double[] getTransDisplacement() {
    return dims;
  }

}
