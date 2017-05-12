package gmail.pvbemmelen62.delaunay;

import java.util.*;

public class Triangle {
  /**
   * Returns i and j such that points[triangle0[i]] and points[triangle0[j]]
   * form the boundary between triangle0 and triangle1.
   * Returns {i,j} one of {0,1}, {1,2}, {2,0} , so that {i,j,(j+1)%3}
   * represents an orientation of triangle0 in -z direction.
   * @throws AssertionError if triangles do not share exactly 2 common
   *   points.
   */
  public static int[] findCommonPoints(int[] triangle0, int[] triangle1) {
    int[] equals = {-1,-1,-1};
    for(int i=0; i<3; ++i) {
      for(int j=0; j<3; ++j) {
        if(triangle0[i]==triangle1[j]) {
          equals[i] = j;
          break;
        }
      }
    }
    int count = 0;
    for(int i=0; i<3; ++i) {
      if(equals[i] != -1) {
        ++count;
      }
    }
    Util.myAssert(count==2);
    // 0,1  or 1,2  or 2,0  of triangle0 form the common boundary
    if(equals[1]==-1) {
      return new int[]{2,0};
    }
    if(equals[0]>-1) {
      return new int[] {0,1};
    }
    return new int[] {1,2};
  }
  /** Returns (commons[0]+2)%3 , thus {0,1}-&gt;2 , {1,2}-&gt;0, {2,0}-&gt;1 . */
  public static int nonCommon(int[] commons) {
    return (commons[0]+2)%3;
  }
  /** Returns i such that triangle[i] is not contained in twoPoints.
   * @throws AssertionError if twoPoints are not contained in triangle.
   */
  public static int theOtherPoint(int[] triangle, int[] twoPoints) {
    int[] tri = triangle;
    int[] two = twoPoints;
    Util.myAssert(contains(tri,two[0]));
    Util.myAssert(contains(tri,two[1]));
    for(int i=0; i<3; ++i) {
      int p = tri[i];
      boolean contains = (p==two[0] || p==two[1]);
      if(!contains) {
        return i;
      }
    }
    throw new IllegalArgumentException("triangle: " + triangle
        + ", twoPoints: " + twoPoints);
  }
  public static boolean contains(int[] triangle, int p) {
    boolean rv;
    int[] tri = triangle;
    rv = p==tri[0] || p==tri[1] || p==tri[2];
    return rv;
  }
  /** Rotates elements if needed, to get the element with minimum value at
   *  index 0, and does that in place, i.e. the input array is modified.
   *  @return the array after modification; this is for convenience, since
   *  it is the same array that was input.
   */
  public static int[] toCanonical(int[] triangle) {
    int iMin = Util.indexOfMinimum(triangle);
    if(iMin==0) {
      return triangle;
    }
    Util.rotateInPlace(triangle, iMin);
    return triangle;
  }
  /** Returns 1 if orientation p0,p1,p2 is in +z direction,
   *  -1 if in -z direction, 0 if p0,p1,p2 are colinear.
   *  p0,p1,p2 is in +z direction =def=
   *  outerProduct(p1-p0,p2-p1) > 1 
   */
  public static int getOrientation(Point p0, Point p1, Point p2) {
    Point p01 = p1.subtract(p0);
    Point p12 = p2.subtract(p1);
    double outerProd = outerProduct(p01, p12);
    int rv = outerProd > 0 ? 1 : (outerProd < 0 ? -1 : 0);
    return rv;
  }
  public static double outerProduct(Point p, Point q) {
    double rv = p.x * q.y - p.y * q.x;
    return rv;
  }
  public static Comparator<int[]> triangleComparator = new Comparator<int[]>() {
    @Override
    public int compare(int[] tri0, int[] tri1) {
      for(int i=0; i<3; ++i) {
        if(tri0[i] < tri1[i]) {
          return -1;
        }
        else if(tri0[i] > tri1[i]) {
          return 1;
        }
      }
      return 0;
    }
  };
  /**
   * @see http://stackoverflow.com/questions/5730149/how-to-generate-a-hash-code-from-three-longs
   */
  public static int hashCode(int x, int y, int z) {
    return x ^ y ^ z;
  }
  public static int hashCode(int[] tri) {
    return tri[0] ^ tri[1] ^ tri[2];
  }
}
