package gmail.pvbemmelen62.delaunay;

import java.awt.geom.*;

public class Point implements Comparable<Point> {

  public double x;
  public double y;
  
  /** Point.
   * @param x double
   * @param y double
   */
  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }
  public Point(Point p) {
    this.x = p.x;
    this.y = p.y;
  }
  public double calcRadius() {
    double r = Math.sqrt(Util.sqr(x) + Util.sqr(y));
    return r;
  }
  /** Angle in radians, given by Math.atan2(y,x) . */
  public double calcAngle() {
    double a = Math.atan2(y, x);
    return a;
  }
  public Point subtract(Point p) {
    Point that = p;
    Point rv = new Point(this.x-that.x, this.y-that.y);
    return rv;
  }
  @Override
  public String toString() {
    return "Point [x=" + x + ", y=" + y + "]";
  }
  public double distance(Point p) {
    Point that = p;
    double rv = Math.sqrt(
        Util.sqr(this.x-that.x) + Util.sqr(this.y-that.y) );
    return rv;
  }
  public double distanceSqr(Point p) {
    Point that = p;
    double rv = Util.sqr(this.x-that.x) + Util.sqr(this.y-that.y);
    return rv;
  }
  public Point2D.Double toPoint2D() {
    return new Point2D.Double(x, y);
  }
  /**
   * Compares first y values (most significant), and for equal y values then
   * compares x values.
   */
  public int compareTo(Point p) {
    Point that = p;
    if(this.y > that.y) {
      return 1;
    }
    else if(this.y < that.y) {
      return -1;
    }
    else if(this.x > that.x) {
      return 1;
    }
    else if(this.x < that.x) {
      return -1;
    }
    else {
      return 0;
    }
  }
}
