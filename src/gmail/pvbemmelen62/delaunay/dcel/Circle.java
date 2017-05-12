package gmail.pvbemmelen62.delaunay.dcel;

import gmail.pvbemmelen62.delaunay.*;

public class Circle {

  private static double sqr(double x) {
    return x*x;
  }
  /** Returns true iff point p3 lies in circle through points p0, p1, p2,
   * Also tests that p0,p1,p2 is orientation in -z direction.
   * @throws AssertionError if orientation of points is not in -z direction.
   */
  public static boolean inCircle(Point p0, Point p1, Point p2, Point p3) {
    // clockwise order:
    Util.myAssert(Triangle.getOrientation(p0, p1, p2) == -1);
    double[][] matrix = {
        { p0.x, p0.y, sqr(p0.x)+sqr(p0.y), 1 },
        { p1.x, p1.y, sqr(p1.x)+sqr(p1.y), 1 },
        { p2.x, p2.y, sqr(p2.x)+sqr(p2.y), 1 },
        { p3.x, p3.y, sqr(p3.x)+sqr(p3.y), 1 }
    };
    // Overmars book, 3rd Ed., page 215:
    // if p0,p1,p2 are in clockwise order, then
    //    det>0 <==> p3 in interior of circle
    // (clockwise =eq= in -z direction)
    // boolean rv = determinant4x4(matrix) > 0;
    // Overmars book is wrong:
    boolean rv = determinant4x4(matrix) < 0;
    return rv;
  }
  public static double determinant4x4(double m[][]) {
    // https://en.wikipedia.org/wiki/Determinant
    double rv = 0.0;
    rv += m[0][0] * determinant3x3(new double[][] {
      { m[1][1], m[1][2], m[1][3] },
      { m[2][1], m[2][2], m[2][3] },
      { m[3][1], m[3][2], m[3][3] }
    });
    rv -= m[0][1] * determinant3x3(new double[][] {
      { m[1][0], m[1][2], m[1][3] },
      { m[2][0], m[2][2], m[2][3] },
      { m[3][0], m[3][2], m[3][3] }
    });
    rv += m[0][2] * determinant3x3(new double[][] {
      { m[1][0], m[1][1], m[1][3] },
      { m[2][0], m[2][1], m[2][3] },
      { m[3][0], m[3][1], m[3][3] }
    });
    rv -= m[0][3] * determinant3x3(new double[][] {
      { m[1][0], m[1][1], m[1][2] },
      { m[2][0], m[2][1], m[2][2] },
      { m[3][0], m[3][1], m[3][2] }
    });
    return rv;
  }
  public static double determinant3x3(double m[][]) {
    double rv = 0.0;
    rv += m[0][0]*(m[1][1]*m[2][2]-m[2][1]*m[1][2]);
    rv -= m[0][1]*(m[1][0]*m[2][2]-m[2][0]*m[1][2]);
    rv += m[0][2]*(m[1][0]*m[2][1]-m[2][0]*m[1][1]);
    return rv;
  }
  
}
