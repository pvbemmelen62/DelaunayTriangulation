package gmail.pvbemmelen62.delaunay.test;

import java.util.*;

import gmail.pvbemmelen62.delaunay.*;

public class Points {

  public static Point[] randomPoints(int numPoints, Random random) {
    Point[] points = new Point[numPoints];
    for(int i=0; i<numPoints; ++i) {
      points[i] = new Point(random.nextDouble(), random.nextDouble());
    }
    return points;
  }
}
