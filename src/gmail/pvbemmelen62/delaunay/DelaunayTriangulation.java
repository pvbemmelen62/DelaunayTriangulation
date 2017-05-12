package gmail.pvbemmelen62.delaunay;

import java.util.*;

import gmail.pvbemmelen62.delaunay.PointLocationStructure.*;
import gmail.pvbemmelen62.delaunay.dcel.*;
import gmail.pvbemmelen62.delaunay.dcel.Dcel.*;

public class DelaunayTriangulation {
  /*
   * "the orientation of i0, i1, i2" =def=
   *    the rotation when moving from point i0 to point i1 to point i2
   * Since the points lie in the xy plane, the rotation will be in the
   * +z or -z direction, according to the corkscrew rule.
   */
  protected Dcel dcel;
  protected PointLocationStructure pls;
  protected Point[] points;

  public static Comparator<Point> yThenXComparator = new Comparator<Point>() {
    @Override
    public int compare(Point p0, Point p1) {
      if(p0.y > p1.y) {
        return 1;
      }
      else if(p0.y < p1.y) {
        return -1;
      }
      else {
        if(p0.x > p1.x) {
          return 1;
        }
        else if(p0.x < p1.x) {
          return -1;
        }
        else {
          return 0;
        }
      }
    }
  };
  public static void swap(Point[] points, int i, int j) {
    Point tmp = points[i];
    points[i] = points[j];
    points[j] = tmp;
  }
  /** Returns index of largest point, using yThenXComparator . */
  public static int findLargestPoint(Point[] points) {
    int iLargest = 0;
    for(int i=1; i<points.length; ++i) {
      if(yThenXComparator.compare(points[i], points[iLargest]) > 0) {
        iLargest = i;
      }
    }
    return iLargest;
  }
  /**
   * Calculates the Delaunay triangulation.
   * @param points The points for which to create the triangulation; points[0]
   *   must be largest among all points according to yThenXComparator.
   * @throws AssertionError if points[0] is not the largest point.
   */
  public DelaunayTriangulation(Point[] points) {
    int iLargest = findLargestPoint(points);
    Util.myAssert(iLargest==0);
    this.points = points;
    triangulate();
  }
  public Dcel getDcel() {
    return dcel;
  }
  protected void triangulate() {
    Long seed = 0L;
    Random random = new Random(seed);
    int[] permutation = Util.permutation(random, 1, points.length-1);
    pls = new PointLocationStructure(points);
    dcel = new Dcel(points);
    HalfEdge h_20 = dcel.getHalfEdge(-2, 0);
    h_20.face.data = pls.top;
    for(int i=0; i<permutation.length; ++i) {
      int pointIndex = permutation[i];
      Node[] nodes = pls.findContainingLeafNodes(pointIndex);
      if(nodes.length==1) {
        Node node = nodes[0];
        int[] tri = node.triangle;
        int i0 = tri[0];
        int i1 = tri[1];
        int i2 = tri[2];
        int i3 = pointIndex;
        pls.splitContainingLeafNode(node, i3);
        HalfEdge h30 = dcel.splitTriangle(tri, pointIndex);
        dcel.checkEdges();
        HalfEdge h31 = h30.prev.twin;
        HalfEdge h32 = h30.twin.next;
        Face[] facesNew = new Face[] { h30.face, h31.face, h32.face };
        for(int j=0; j<3; ++j) {
          facesNew[j].data = node.children[j];
        }
        legalizeEdge(i3, i0, i1);
        dcel.checkEdges();
        legalizeEdge(i3, i1, i2);
        dcel.checkEdges();
        legalizeEdge(i3, i2, i0);
        dcel.checkEdges();
      }
      else { // nodes.length==2
        int[] tri0 = nodes[0].triangle;
        int[] tri1 = nodes[1].triangle;
        int[] commons = Triangle.findCommonPoints(tri0, tri1);
        int i0 = tri0[commons[0]];
        int i1 = tri0[commons[1]];
        int i2 = tri0[Triangle.nonCommon(commons)];
        int i3 = tri1[Triangle.theOtherPoint(tri1, new int[] { i0, i1})];
        int i4 = pointIndex;
        pls.splitContainingLeafNodes(nodes[0], nodes[1], i4);
        HalfEdge h04 = dcel.splitTriangles(tri0, tri1, i4);
        dcel.checkEdges();
        h04.face.data = nodes[0].children[0]; // {i0,i4,i2}
        HalfEdge h41 = h04.next.twin.next;
        h41.face.data = nodes[0].children[1]; // {i4,i1,i2}
        HalfEdge h14 = h41.twin;
        h14.face.data = nodes[1].children[0]; // {i1,i4,i3}
        HalfEdge h40 = h04.twin;
        h40.face.data = nodes[1].children[1]; // {i4,i0,i3}
        legalizeEdge(i4,i2,i0);
        dcel.checkEdges();
        legalizeEdge(i4,i1,i2);
        dcel.checkEdges();
        legalizeEdge(i4,i3,i1);
        dcel.checkEdges();
        legalizeEdge(i4,i0,i3);
        dcel.checkEdges();
      }
    }
  }
  /** Legalize edge ie0,ie1 for the newly inserted point iNew .
   * Orientation of iNew, ie0, ie1 is in -z direction.
   */
  protected void legalizeEdge(int iNew, int ie0, int ie1) {
    HalfEdge h = dcel.getHalfEdge(ie0, ie1);
    if(h==null) {
//      throw new IllegalStateException("legalizeEdge with non-existing edge"
//         + "{" + ie0 + ", " + ie1 + "}" + "... may be alright !?");
      return;
    }
    if(h.isLegal()) {
      return;
    }
    if(!h.swapIsConvex()) {
      return;
    }
    int iOpposite = h.twin.prev.origin.index;     // opposite iNew wrt h.
    HalfEdge hNew = flipEdge(h);
    Util.myAssert(hNew.next.next.next == hNew);
    Util.myAssert(hNew.origin.index==iOpposite
        || hNew.twin.origin.index==iOpposite);
    legalizeEdge(iNew, ie0, iOpposite);
    legalizeEdge(iNew, iOpposite, ie1);
  }
  protected HalfEdge flipEdge(HalfEdge h) {
    Node node0 = h.face.data;
    Node node1 = h.twin.face.data;
    HalfEdge hNew = h.flip();
    Face[] facesNew = new Face[] { hNew.face, hNew.twin.face };
    int[] tri0 = facesNew[0].calcTriangle();
    int[] tri1 = facesNew[1].calcTriangle();
    Node[] nodesNew = pls.flipEdge(node0, node1, tri0, tri1);
    facesNew[0].data = nodesNew[0];
    facesNew[1].data = nodesNew[1];
    return hNew;
  }  
}
