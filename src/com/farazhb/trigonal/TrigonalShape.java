/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/1/22 - ... */
package com.farazhb.trigonal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;


/** Collection of 2 classes: Trigon and Trigon Comparator based on its ID. Trigon provides getter
 * functions {getTrigDir, getId}, setters {setId(int), setFillable(bool)}, and drawTrig(g2d). */
public class TrigonalShape implements TrigonalConsts {


  public static class Trigon {

    // iVars
    private Point2D[] vertices = new Point2D[NUM_PTS];
    private int trigId = 0;
    private int ownerId;
    private Color color;
    public double baseX, baseY;
    private byte type;
    private int[] indicesIJT; // {I,J,Type} i,j are indices are basis pt, T up or down
    private Path2D trigShape;
    private boolean imgIsTiled;
    private BufferedImage bgImg;
    public static BufferedImage imgTile; // In case it is tiled, this will be set once for all


    // Constructor
    public Trigon(double baseX, double baseY, byte type) {
      trigId++;
      ownerId = NEUTRAL_TRIG_PLAYER_ID; // each trig owner id is initially 0 until claimed
      this.baseX = baseX;
      this.baseY = baseY;
      this.type = type;
    }

    public int getId() {
      return trigId;
    }

    /** Returns the player id of the current owner of this trigon object on board. */
    public int getOwnerId() {
      return ownerId;
    }

    /** Returns triangle type. */
    public byte getType() {
      return type;
    }

    public int[] getIndicesIJT() {
      return indicesIJT;
    }

    public Path2D getTrigShape() {
      return trigShape;
    }

    /** Returns coordinates of the trigon's (midX,midY) point as board defined in double[]{x,y} */
    public double[] getMedian(double realXU, double realYU) {
      double[] innerCoords = new double[2];
      innerCoords[0] = baseX + realXU / 2.0;
      if (type == UP_TRIG_NAME) {
        innerCoords[1] = baseY - realYU / 3.0;
      } else if (type == DN_TRIG_NAME) {
        innerCoords[1] = baseY + realYU / 3.0;
      } else {
        // throw Exception. Actually this function's only used internally and not think need throw
      }
      return innerCoords;
    }

    public void setId(int newId) {
      trigId = newId;
    }

    /** Sets given player id as owner id of this trigon object on board. */
    public void setOwnerId(int playerId) {
      ownerId = playerId;
    }

    public void setColor(Color newColor) {
      color = newColor;
    }

    public void setIndicesIJT(int[] indices) {
      indicesIJT = indices;
    }



    public void drawTrig(Graphics2D g2, double realXU, double realYU, BufferedImage img,
              boolean imgIsTiled) {
      if (ownerId == NEUTRAL_TRIG_PLAYER_ID) {
        g2.setColor(TRIG_COLOR_INI);
      } else {
        g2.setColor(color);
      }
      buildShape(realXU, realYU);
      g2.fill(trigShape); // TODO: Does this do the drawing live? or is that after g2.draw(trig2D) ?

      if (img != null) { // if a loaded image is used at all
        // bgG2.setClip(trig2D);
        if (imgIsTiled) { // if image is used as tiled. One tiled unit will be stored
          AffineTransform trans = new AffineTransform();
          trans.translate(baseX, baseY);
          if (type == DN_TRIG_NAME) {
            trans.scale(1, -1); // TODO: I want vertical flip, of course
          }
          Graphics2D bgG2 = img.createGraphics();
          bgG2.setTransform(trans);
          g2.drawImage(img, 0, 0, null);
          // TODO: clip bgG2 accordingly. maybe also any AffineTransform (if tiled? either way?)
          // TODO load a single triangle same place, same dims as our trig, from image, draw onto g2
          // This one's easier as source image can be that size, rect if easier, transparent around
        } else { // not tiled
          // After coming into this function, seems the img-bgG2 image-g2D relationship is lost
          double[] xYTranslations = TrigonalDims.getXYTranslations();
          // g2.translate(xYTranslations[0], xYTranslations[1]);
          // TODO: we've passed the mutable img here who's being controlled using bgG2 which we've
          // passed here as well. Do they work accordingly their interrelationship intact?
          g2.drawImage(img, (int) -xYTranslations[0], (int) -xYTranslations[1], null);
          // TODO: Instead of adding so many images to the g2, it would be better to once at end.
          // For that we'd need to add trig Shapes (Path2Ds) from all trigs on board that belong to
          // a player, add them all to clip, and clip them together

          // TODO load triangle where we're drawing the Path2D from image and draw onto g2
        }
      } // TODO: We're drawing one by one from source (a BufImg) for each trigon and transforming...
        // TODO: cont'd: maybe do less work by storing statically a part of it, or build trigs
        // before
      g2.draw(trigShape); // TODO: Does it draw the trig without this line? just test
    }

    public Path2D buildShape(double realXU, double realYU) {
      double dtr = TRIGON_DIM_SHRINKAGE; // Pixels along each triangle bisector from each vertex
      double drwBsX = baseX + dtr * HF_SQT_3;
      double drwBsY = (type == UP_TRIG_NAME) ? baseY - dtr / 2 : baseY + dtr / 2;
      double drwUX = realXU - 2 * dtr * HF_SQT_3;
      double drwUY = realYU - (3 / 2.0) * dtr;
      // implicit/unnecessary: vertices[0] = new Point2D.Double(baseX, baseY);
      vertices[1] = new Point2D.Double(drwBsX + drwUX, drwBsY);
      if (type == UP_TRIG_NAME) {
        vertices[2] = new Point2D.Double(drwBsX + drwUX / 2.0, drwBsY - drwUY);
      } else if (type == DN_TRIG_NAME) {
        vertices[2] = new Point2D.Double(drwBsX + drwUX / 2.0, drwBsY + drwUY);
      }
      Path2D trig = new Path2D.Double();
      trig.moveTo(drwBsX, drwBsY);
      for (int i = 1; i < NUM_PTS; i++) {
        trig.lineTo(vertices[i].getX(), vertices[i].getY());
      }
      trig.closePath();
      trigShape = trig;
      return trig; // TODO: Is the instance variable used for anything? just reuturn and remove iVar
    }

  }



  /** A comparator class basing comparison on ids of trigs */
  public static class TrigOnIdComparator implements Comparator<Trigon> {

    @Override
    public int compare(Trigon a, Trigon b) {
      return a.trigId < b.trigId ? -1 : a.trigId == b.trigId ? 0 : 1;
    }

  }


}
