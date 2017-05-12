package gmail.pvbemmelen62.delaunay;

public class MyRect {

  public double minX;
  public double maxX;
  public double minY;
  public double maxY;
  
  public static MyRect fromPoints(Point[] points) {
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for(Point p : points) {
      minX = Math.min(minX, p.x);
      maxX = Math.max(maxX, p.x);
      minY = Math.min(minY, p.y);
      maxY = Math.max(maxY, p.y);
    }
    return new MyRect(minX, maxX, minY, maxY);
  }
  public MyRect(double minX, double maxX, double minY, double maxY) {
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
  }
  public MyRect scale(double factor) {
    double w2 = width() * factor / 2;
    double h2 = height() * factor / 2;
    Point c = center();
    MyRect rect = new MyRect(c.x-w2, c.x+w2, c.y-h2, c.y+h2);
    return rect;
  }
  /** Returns ((minX + maxX)/2, (minY+maxY)/2). */
  public Point center() {
    return new Point((minX + maxX)/2, (minY+maxY)/2); 
  }
  public double width() {
    return maxX - minX;
  }
  public double height() {
    return maxY - minY;
  }
  public double surface() {
    return width()*height();
  }
  @Override
  public String toString() {
    return "MyRect [minX=" + minX + ", maxX=" + maxX + ", minY=" + minY
        + ", maxY=" + maxY + "]";
  }
  public boolean contains(double x, double y) {
    boolean b = minX <= x && x <= maxX && minY <= y && y <= maxY;
    return b;
  }
}
