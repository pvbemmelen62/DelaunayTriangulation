package gmail.pvbemmelen62.delaunay.dcel.test;

import gmail.pvbemmelen62.delaunay.*;
import gmail.pvbemmelen62.delaunay.dcel.*;

public class CircleTest {

  public static void main(String[] args) {
    boolean inC;
    Point p0 = new Point(0,1);
    Point p1 = new Point(1,0);
    Point p2 = new Point(0,-1);
    Point p3 = new Point(0.9,0);
    
    inC = Circle.inCircle(p0,p1,p2,p3);
    Util.myAssert(inC);
    
    p3 = new Point(1.1,0);

    inC = Circle.inCircle(p0,p1,p2,p3);
    Util.myAssert(!inC);
  }
}
