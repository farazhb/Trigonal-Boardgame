/** For Trigonal by Faraz Hossein-Babaei, a game, in production 2016/2/1 - ... */
package com.farazhb.trigonal;

import java.awt.Color;
import java.io.Serializable;
import javafx.util.Pair;


public class TrigonalPlayer implements Serializable {

  // iVars:
  private int id;
  private Color color;
  private String name;
  private int score;
  private String bgImgPath;
  private boolean imgIsUsed, imgIsTiled;


  
  public TrigonalPlayer(int id) {
    this.id = id;
  }

  public TrigonalPlayer(int id, String name, Color color) {
    this.id = id;
    this.color = color;
    this.name = name;
    score = 0;
  }

  public int getId() { // No set id, as id is fixed upo construction
    return id;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color newColor) {
    color = newColor;
  }

  public String getName() {
    return name;
  }

  public void setName(String newName) {
    name = newName;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int newScore) {
    score = newScore;
  }

  public Pair<String,Boolean> getImageInfo() {
    return new Pair<String,Boolean>(bgImgPath, imgIsTiled);
  }

  public void setImgInfo(String bgImg, boolean imgIsTiled) {
    this.bgImgPath = bgImg;
    this.imgIsTiled = imgIsTiled;
    imgIsUsed = !bgImgPath.equals("");
  }

}
