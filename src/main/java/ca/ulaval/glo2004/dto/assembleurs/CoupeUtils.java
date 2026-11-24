package ca.ulaval.glo2004.dto.assembleurs;

import java.awt.Point;

public class CoupeUtils {
  public static boolean isVertical(Point origine, Point destination) {
    return origine.x == destination.x && origine.y != destination.y;
  }

  public static boolean isHorizontal(Point origine, Point destination) {
    return origine.y == destination.y && origine.x != destination.x;
  }

  public static boolean isRectangular(Point origine, Point destination) {
    return origine != null && destination != null &&
           origine.x != destination.x && origine.y != destination.y;
  }

  public static int calculerLargeur(Point origine, Point destination) {
    return Math.abs(destination.x - origine.x);
  }

  public static int calculerLongueur(Point origine, Point destination) {
    return Math.abs(destination.y - origine.y);
  }
}