/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/2/3 - ... */
package com.farazhb.trigonal;

import java.awt.Dimension;
import java.awt.Toolkit;


public class TrigonalDims implements TrigonalConsts {

  // static vars
  static double[] dims; // dims { realXUnit, realYUnit }
  static double sideXExcess, sideYExcess;

  public static double[] getDims() {
    return dims;
  }

  private static void setDims(double[] newDims) {
    dims = newDims;
  }

  private static double[] getDesktopDims() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new double[] { screenSize.getWidth(), screenSize.getHeight() };
  }

  public static int[] getSuitablePanWH(double screenRatio) {
    double[] screenDims = getDesktopDims();
    double potentialPanW = screenDims[0] * screenRatio;
    double potentialPanH = screenDims[1] * screenRatio;
    double panW = (potentialPanH > potentialPanW) ? potentialPanW : potentialPanH; // min of two
    return new int[] { (int) panW, (int) panW }; // equal width and height
  }

  // For when state initially calls it without board having been created. There is no panel then
  public static double[] getEffDims() {
    int[] supposedPanWH = getSuitablePanWH(SCREEN_PAN_SZ_RATIO);
    int panW = supposedPanWH[0];
    int panH = supposedPanWH[1];
    return getEffDims(panW, panH);
  }

  public static double[] getEffDims(int panW, int panH) {
    // TODO: Something wrong: UNIT_EDGE shouldn't affect overall board size but does
    double xFactor = panW / (UNIT_EDGE * 2 * NUM_TRIGS + 2 * MARGIN); // potential factor
    double yFactor = panH / (UNIT_EDGE * 2 * NUM_TRIGS * HF_SQT_3 + 2 * MARGIN); // [pixels/units]
    double xSclFac, ySclFac;
    if (yFactor < xFactor) { // height limiting
      xSclFac = ySclFac = yFactor;
    } else { // width limiting
      ySclFac = xSclFac = xFactor;
    }
    sideXExcess = (panW - xSclFac * (UNIT_EDGE * 2 * NUM_TRIGS)) / 2.0; // includes MARGIN then
    sideYExcess = (panH - (UNIT_EDGE * 2 * NUM_TRIGS * HF_SQT_3) * ySclFac) / 2.0; // margin inclus
    double realXU = xSclFac * UNIT_EDGE;
    double realYU = ySclFac * HF_SQT_3 * UNIT_EDGE;
    double[] dimensions = new double[] { realXU, realYU};
    setDims(dimensions);
    return dimensions;
  }

  public static double[] getXYTranslations() {
    double xTransAd = NUM_TRIGS + sideXExcess; // +num_trigs bcs trigons overlap nghbrs by 1 pixel
    double yTransAd = NUM_TRIGS + NUM_TRIGS * dims[1] + sideYExcess; // +num_trigs bcs of overlap
    return new double[] { xTransAd, yTransAd };
  }

}
