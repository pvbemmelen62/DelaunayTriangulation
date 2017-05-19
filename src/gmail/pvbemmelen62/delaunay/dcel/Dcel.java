package gmail.pvbemmelen62.delaunay.dcel;

import java.io.*;
import java.text.*;
import java.util.*;

import gmail.pvbemmelen62.delaunay.*;
import gmail.pvbemmelen62.delaunay.PointLocationStructure.*;

public class Dcel {
  /**
   * Half edge.
   * @see https://en.wikipedia.org/wiki/Doubly_connected_edge_list
   */
  public class HalfEdge {
    public HalfEdge twin;
    public HalfEdge next;
    public HalfEdge prev;
    public Vertex origin;
    public Face face;
    public int index;       // for debugging
    public boolean inUse;   // for debugging

    public HalfEdge() {
      edges.add(this);
      this.index = edges.size()-1;
      inUse = true;
    }
    public String toStringShort() {
      Integer i0 = origin==null ? null : origin.index;
      Integer i1 = twin==null ? null :
        (twin.origin==null ? null : twin.origin.index);
      String rv = "{" + i0 + "," + i1 + "}";
      return rv;
    }
    public String toString() {
      String rv = "{"
          + toStringShort()
          + ", t:" + dumbString(twin)
          + ", n:" + dumbString(next)
          + ", p:" + dumbString(prev)
          + ", o:" + dumbString(origin)
          + ", f:" + dumbString(face)
          + ", i:" + index
          + ", u:" + inUse
          + "}";
      return rv;
    }
    public boolean isLegal() {
      // Computational Geometry, Algorithms and Applications, 3rd Ed.
      // page 201, figure on the right:
      //   edge to test is pi,pj , with pi,pj,pk being one triangle,
      //   and pj,pi,pr being the other triangle
      // Compare:
      //   pi to v0
      //   pj to v1
      //   pk to v2
      //   pr to v3
      if(face.data==null || twin.face.data==null) {
        return true;
      }
      Vertex v0 = origin;
      Vertex v1 = next.origin;
      Vertex v2 = next.next.origin;
      Vertex v3 = twin.next.next.origin;
      // page 204:
      // Compare:
      //   pi to v0
      //   pj to v1
      //   pk to v2
      //   pl to v3
      //
      if((v0.index<0 || v0.index==iLargest)
          && (v1.index<0 || v1.index==iLargest)) {
        throw new IllegalStateException(
            "test for face.data==null should have handled this.");
        //return true;
      }
      int numNegs = 0;
      for(Vertex v : new Vertex[] {v0,v1,v2,v3}) {
        if(v.index<0) {
          numNegs++;
        }
      }
      if(numNegs==0) {
        boolean inC = Circle.inCircle(
            points[v0.index], points[v1.index], points[v2.index], points[v3.index]);
        return !inC;
      }
      else {
        // p204:
        //  Let pipj be the edge to be tested, and let pk and pl be the other
        //  vertices of the triangles incident to pipj (if they exist).
        //    ....
        //  pipj is legal if and only if min(k,l) < min(i,j)
        return Math.min(v2.index,v3.index) < Math.min(v0.index,v1.index);
      }
    }
    /**
     * Checks convexity constraints.
     * Flipping the common edge of v0,v1,v2 and v1,v0,v3 is only possible
     * if the polygon v0,v3,v1,v2 is convex, so that the flip affects
     * no other triangles.
     * Thus, all angles of v0,v3,v1,v2 must be < 180 degrees .
     * Angles v1,v2,v0 and v0,v3,v1 are already known to be < 180 degrees.
     * Angles v2,v0,v3 and v3,v1,v2 still need checking.
     */
    @SuppressWarnings("unused")
    public boolean swapIsConvex() {
      Vertex v0 = origin;
      Vertex v1 = next.origin;
      Vertex v2 = next.next.origin;
      Vertex v3 = twin.next.next.origin;
      int numNegs = 0;
      // There are two special vertices, that can be modelled like this:
      //   v.index == -2:   x,y = -r*s,ymax+h
      //   v.index == -1:   x,y = r,ymin-h
      // with
      //   limit r->inf
      //   limit s->inf
      //   ymax: max y of regular points
      //   ymin: min y of regular points
      //   h: ymax-ymin
      Vertex[] negVs = new Vertex[3];
      for(Vertex v : new Vertex[] {v0,v1,v2,v3}) {
        if(v.index<0) {
          numNegs++;
          negVs[-v.index] = v; 
        }
      }
      if(numNegs==2) {
        Util.myAssert(negVs[1]!=null && negVs[2]!=null);
      }
      Point p0 = v0.getPoint();
      Point p1 = v1.getPoint();
      Point p2 = v2.getPoint();
      Point p3 = v3.getPoint();
      Boolean convex = null;
      // Below, don't think about angles, but think about orientation of
      // triangles, using the corkscrew rule.
      // Both p2,p0,p3 and p3,p1,p2 must have orientation in -z direction.
      if(numNegs==0) {
        convex =
            Triangle.getOrientation(p2, p0, p3)==-1
         && Triangle.getOrientation(p3, p1, p2)==-1 ;
        // Email from Mark de Berg, dd 170516:
        //   For the “normal” points, the convexity test is not necessary: when
        //   the two triangles do not form a convex quadrilateral, then the
        //   in-circle test will always fail.
        // To preserve the semantics of swapIsConvex(), we will still calculate
        // the convexity.
      }
      else if(numNegs==1) {
        if(v0.index==-2) {
          convex =
              p3.compareTo(p2)>0     // triangle p2,p0,p3 == p0,p3,p2
           && Triangle.getOrientation(p3, p1, p2)==-1;
        }
        else if(v0.index==-1) {
          convex =
              p2.compareTo(p3)>0     // triangle p2,p0,p3 == p0,p3,p2
              && Triangle.getOrientation(p3, p1, p2)==-1;
        }
        else if(v1.index==-2) {
          convex =
              Triangle.getOrientation(p2, p0, p3)==-1
           && p2.compareTo(p3)>0;    // triangle p3,p1,p2 == p1,p2,p3
        }
        else if(v1.index==-1) {
          convex =
              Triangle.getOrientation(p2, p0, p3)==-1
           && p3.compareTo(p2)>0;
        }
        else if(v2.index==-2) {
          convex =
              p0.compareTo(p3)>0     // triangle p2,p0,p3
           && p3.compareTo(p1)>0;    // triangle p3,p1,p2 == p2,p3,p1
        }
        else if(v2.index==-1) {
          convex =
              p3.compareTo(p0)>0     // triangle p2,p0,p3
           && p1.compareTo(p3)>0;    // triangle p3,p1,p2 == p2,p3,p1
        }
        else if(v3.index==-2) {
          convex =
              p2.compareTo(p0)>0     // triangle p2,p0,p3 = p3,p2,p0
           && p1.compareTo(p2)>0;    // triangle p3,p1,p2
        }
        else if(v3.index==-1) {
          convex =
              p0.compareTo(p2)>0     // triangle p2,p0,p3 = p3,p2,p0
           && p2.compareTo(p1)>0;    // triangle p3,p1,p2
        }
        else {
          throw new IllegalStateException("no negative index found.");
        }
      }
      else if(numNegs==2) {
        if(v0.index<0) {
          if(v1.index<0) {
            throw new IllegalStateException("illegal edge to flip");
          }
          else if(v2.index<0) {
            if(v0.index==-2) {
              Util.myAssert(v2.index==-1);
              convex =
                  true                   // triangle p2,p0,p3
               && p1.compareTo(p3)>0;    // triangle p3,p1,p2 = p2,p3,p1
            }
            else if(v0.index==-1) {
              Util.myAssert(v2.index==-2);
              convex =
                  false                  // triangle p2,p0,p3
               && p3.compareTo(p1)>0;    // triangle p3,p1,p2 = p2,p3,p1
            }
          }
          else if(v3.index<0) {
            if(v3.index==-2) {
              Util.myAssert(v0.index==-1);
              convex =
                  true                   // triangle p2,p0,p3 = p3,p2,p0
               && p1.compareTo(p2)>0;    // triangle p3,p1,p2
            }
            else if(v3.index==-1) {
              Util.myAssert(v0.index==-2);
              convex =
                  false                  // triangle p2,p0,p3 = p3,p2,p0
               && p2.compareTo(p1)>0;    // triangle p3,p1,p2
            }
          }
        }
        else if(v1.index<0) {
          Util.myAssert(v0.index>=0);
          if(v2.index<0) {
            if(v2.index==-2) {
              Util.myAssert(v1.index==-1);
              convex =
                  p0.compareTo(p3)>0     // triangle p2,p0,p3
               && true;                  // triangle p3,p1,p2 = p2,p3,p1
            }
            else if(v2.index==-1) {
              Util.myAssert(v1.index==-2);
              convex =
                  p3.compareTo(p0)>0     // triangle p2,p0,p3
               && false;                 // triangle p3,p1,p2 = p2,p3,p1
            }
          }
          else if(v3.index<0) {
            if(v3.index==-2) {
              Util.myAssert(v1.index==-1);
              convex =
                  p2.compareTo(p0)>0     // triangle p2,p0,p3 = p3,p2,p0
               && false;                 // triangle p3,p1,p2
            }
            else if(v3.index==-1) {
              Util.myAssert(v1.index==-2);
              convex =
                  p0.compareTo(p2)>0     // triangle p2,p0,p3 = p3,p2,p0
               && true;                  // triangle p3,p1,p2
            }
          }
        }
        else if(v2.index<0) {
          Util.myAssert(v3.index<0);
          if(v3.index==-2) {
            Util.myAssert(v2.index==-1);
            convex =
                false                  // triangle p2,p0,p3
             && true;                  // triangle p3,p1,p2
          }
          else if(v3.index==-1) {
            Util.myAssert(v2.index==-2);
            convex =
                true                   // triangle p2,p0,p3
             && false;                 // triangle p3,p1,p2
          }
        }
        else {
          throw new IllegalStateException("no two negative indexes found.");
        }
      }
      else {
        throw new IllegalStateException("numNegs: " + numNegs);
      }
      Util.myAssert(convex!=null);
      return convex;
    }
    /** Flips this halfedge h01, part of triangle {i0,i1,i2}, plus twin h10,
     *  part of triangle {i1,i0,i3} , creating a new halfedge h23 plus twin h32.
     *  Also creates two new faces, {i0,i3,i2} plus {i1,i2,i3} , disconnecting
     *  the old faces.
     *  @returns h23.
     */
    public HalfEdge flip() {
      HalfEdge h01 = this;
      HalfEdge h12 = h01.next;
      HalfEdge h20 = h12.next;
      Util.myAssert(h20.next == h01);
      HalfEdge h10 = h01.twin;
      HalfEdge h03 = h10.next;
      HalfEdge h31 = h03.next;
      Util.myAssert(h31.next == h10);
      Vertex v0 = h01.origin;
      Vertex v1 = h10.origin;
      Vertex v2 = h20.origin;
      Vertex v3 = h31.origin;
      HalfEdge h23 = new HalfEdge();
      HalfEdge h32 = new HalfEdge();
      linkTwins(h23,h32);
      linkPrevNextTriangle(h03,h32,h20);
      linkPrevNextTriangle(h12,h23,h31);
      h23.origin = v2;
      h32.origin = v3;
      v0.repair(h01,h03);
      v1.repair(h10, h12);
      Face f032 = new Face();
      Face f123 = new Face();
      linkEdgesAndFace(h03,f032);
      linkEdgesAndFace(h12,f123);
      h01.inUse = false;
      h10.inUse = false;
      return h23;
    }
  }
  /** pTo.subtract(pFrom), or null if pFrom or pTo equals null. */
  public static Point getVector(Point pFrom, Point pTo) {
    if(pFrom==null || pTo==null) {
      return null;
    }
    return pTo.subtract(pFrom);
  }
  private static String dumbString(Object obj) {
    String rv = obj==null ? null : "*";
    return rv;
  }
  private static String dumbString(HalfEdge h) {
    String rv = h==null ? null : ""+h.index;
    return rv;
  }
  private static String dumbString(Vertex v) {
    String rv = v==null ? null : ""+v.index;
    return rv;
  }
  public static void linkTwins(HalfEdge h0, HalfEdge h1) {
    h0.twin = h1;
    h1.twin = h0;
  }
  public static void linkPrevNext(HalfEdge h0, HalfEdge h1) {
    h0.next = h1;
    h1.prev = h0;
  }
  public static void linkPrevNextTriangle(HalfEdge h0, HalfEdge h1,
      HalfEdge h2) {
    linkPrevNext(h0,h1);
    linkPrevNext(h1,h2);
    linkPrevNext(h2,h0);
  }
  public static void linkEdgesAndFace(HalfEdge h, Face f) {
    HalfEdge h0 = h;
//  int count = 0;
    do {
//    if(count > 3) {
//      System.out.println("count:" + count);
//    }
//    if(count > 1000) {
//      throw new IllegalStateException();
//    }
      h.face = f;
      h = h.next;
//    ++count;
    }
    while(h!=h0);
    f.edge = h0;
  }
  public class Vertex {
    Vertex(int index) {
      this.index = index;
      if(index>=0) {
        vertices[index] = this;
      }
    }
    public HalfEdge edge;
    public int index;
    
    public String toString() {
      String rv = "{"
          + "index:" + index
          + ", edge:" + (edge==null ? null : edge.toStringShort())
          + ", point:" + (index>=0 ? points[index] : "?")
          + "}"
          ;
      return rv;
    }
    /** points[vertex.index], or null if vertex.index<0 */
    public Point getPoint() {
      if(index>=0) {
        return points[index];
      }
      else {
        return null;
      }
    }
    /** Repair if needed, for edge h0 no longer being used: if this.edge
     * equals h0, then change it to h1.
     */ 
    public void repair(HalfEdge h0, HalfEdge h1) {
      if(edge==h0) {
        edge = h1;
      }
    }
    public Iterator<HalfEdge> edgeIterator() {
      return new Iterator<HalfEdge>() {
        boolean hasNext = true;
        HalfEdge edge0 = Vertex.this.edge;
        HalfEdge edge = edge0;
        @Override
        public boolean hasNext() {
          return hasNext;
        }
        @Override
        public HalfEdge next() {
          edge = edge.prev.twin;
          hasNext = edge != edge0;
          return edge;
        }
      };
    }
    public String printEdges() {
      String nl = System.getProperty("line.separator");
      Iterator<HalfEdge> hIter = edgeIterator();
      StringBuilder sb = new StringBuilder();
      while(hIter.hasNext()) {
        HalfEdge h = hIter.next();
        sb.append(""+h+nl);
      }
      return sb.toString();
    }
  }
  public class Face implements Comparable<Face> {
    public HalfEdge edge;
    /** The corresponding node in PointLocationStructure, or null if the face
     * is the outer face.
     */
    public Node data;
    /** Calculates and creates triangle from info on half edges.*/ 
    public int[] calcTriangle() {
      int[] tri = new int[3];
      HalfEdge h = edge;
      for(int i=0; i<3; ++i) {
        tri[i] = h.origin.index;
        h = h.next;
      }
      Util.myAssert(h==edge);
      Triangle.toCanonical(tri);
      return tri;
    }
    public int compareTo(Face f) {
      Face f0 = this;
      Face f1 = f;
      int[] tri0 = f0.calcTriangle();
      int[] tri1 = f1.calcTriangle();
      int rv = Triangle.triangleComparator.compare(tri0, tri1);
      return rv;
    }
    public boolean equals(Face f) {
      boolean rv = compareTo(f) == 0;
      return rv;
    }
    @Override
    public int hashCode() {
      int rv = Triangle.hashCode(calcTriangle());
      return rv;
    }
    public String toString() {
      String rv = "{"
          + "edge: " + (edge==null ? null : edge.toStringShort())
          + "data: " + dumbString(data)
          + "}";
      return rv;
    }
  }
  
  protected Point[] points;
  protected int iLargest;
  protected Vertex[] vertices;
  protected ArrayList<HalfEdge> edges; // : for debugging only.
  /** Vertex -2 . */
  protected Vertex v_2;
  /** Vertex -1 . */
  protected Vertex v_1;

  public Dcel(Point[] points, int iLargest) {
    this.points = points;
    this.iLargest = iLargest;
    vertices = new Vertex[points.length];
    edges = new ArrayList<>();
    // create top triangle {-2,0,-1}
    v_2 = new Vertex(-2);
    v_1 = new Vertex(-1);
    Vertex vL = new Vertex(iLargest);
    HalfEdge h_2L = new HalfEdge();
    HalfEdge hL_2 = new HalfEdge();
    HalfEdge hL_1 = new HalfEdge();
    HalfEdge h_1L = new HalfEdge();
    HalfEdge h_1_2 = new HalfEdge();
    HalfEdge h_2_1 = new HalfEdge();
    linkTwins(h_2L, hL_2);
    linkTwins(hL_1, h_1L);
    linkTwins(h_1_2, h_2_1);
    linkPrevNextTriangle(h_2L, hL_1, h_1_2);
    linkPrevNextTriangle(h_2_1, h_1L, hL_2);
    v_2.edge = h_2L;   h_2L.origin = v_2;   hL_2.origin = vL;
    vL.edge  = hL_1;   hL_1.origin = vL;    h_1L.origin = v_1;
    v_1.edge = h_1_2;  h_1_2.origin = v_1;  h_2_1.origin = v_2;
    Face f_2_1L = new Face();
    Face f_2L_1 = new Face();
    linkEdgesAndFace(h_2_1, f_2_1L);
    linkEdgesAndFace(h_2L, f_2L_1);
  }
  /**
   * Split triangle tri in three triangles by adding a point.
   * @param tri triangle to be split; defines i0,i1,i2 to be its elements.
   * @param pointIndex; i3 is defined equal to pointIndex.
   * @return h30 halfedge from v3 to v0, part of triangle {i3,i0,i1}.
   */
  public HalfEdge splitTriangle(int[] tri, int pointIndex) {
    HalfEdge h01 = getHalfEdge(tri[0],tri[1]);
    HalfEdge h12 = h01.next;
    HalfEdge h20 = h12.next;
    Vertex v0 = h01.origin;
    Vertex v1 = h12.origin;
    Vertex v2 = h20.origin;
    Vertex v3 = new Vertex(pointIndex);
    HalfEdge h03 = new HalfEdge();
    HalfEdge h30 = new HalfEdge();
    HalfEdge h13 = new HalfEdge();
    HalfEdge h31 = new HalfEdge();
    HalfEdge h23 = new HalfEdge();
    HalfEdge h32 = new HalfEdge();
    linkTwins(h03,h30);
    linkTwins(h13,h31);
    linkTwins(h23,h32);
    linkPrevNextTriangle(h01,h13,h30);
    linkPrevNextTriangle(h12,h23,h31);
    linkPrevNextTriangle(h20,h03,h32);
    h03.origin = v0;
    h30.origin = v3;
    h13.origin = v1;
    h31.origin = v3;
    h23.origin = v2;
    h32.origin = v3;
    v3.edge = h30;
    Face f013 = new Face();  linkEdgesAndFace(h01,f013);
    Face f123 = new Face();  linkEdgesAndFace(h12,f123);
    Face f203 = new Face();  linkEdgesAndFace(h20,f203);
    return h30;
  }
  /** Returns the halfedge {i0, i1}, or null if it doesn't exist. */
  public HalfEdge getHalfEdge(int i0, int i1) {
    Vertex v0 = getVertex(i0);
    HalfEdge h0 = v0.edge;
    HalfEdge h = h0;
    while(h.twin.origin.index != i1) {
      h = h.twin.next;
      if(h == h0) {
        return null;
      }
    }
    return h;
  }
  /** Returns vertex i, or null if there is no suc vertex; handles i==-1
   *  and i==-2 correctly.*/
  public Vertex getVertex(int i) {
    Vertex v = null;
    if(i==-1) {
      v = v_1;
    }
    else if(i==-2) {
      v = v_2;
    }
    else {
      v = vertices[i];
    }
    return v;
  }
  /**
   * Splits triangles tri0, being {i0,i1,i2} or a rotation of this, and tri1,
   * being {i1,i0,i3} or a rotation of this, because edges {i0,i1} and {i1,i0}
   * are split by the new point i4.
   * Creates halfedges and faces corresponding to the new triangles
   * <ul>
   *   <li>{i0,i4,i2}</li>
   *   <li>{i4,i1,i2}</li>
   *   <li>{i1,i4,i3}</li>
   *   <li>{i4,i0,i3}</li>
   * </ul>
   * @see Fig 9.7 in Computational Geometry - Algorithms and Applications,
   *  3rd Ed. , page 200
   * @return h04
   */
  public HalfEdge splitTriangles(int[] tri0, int[] tri1, int i4) {
    int[] commons = Triangle.findCommonPoints(tri0, tri1);
    int i0 = tri0[commons[0]];
    int i1 = tri0[commons[1]];
    int i2 = tri0[Triangle.nonCommon(commons)];
    int i3 = tri1[Triangle.theOtherPoint(tri1, new int[] {i0,i1})];

    Vertex v0 = vertices[i0];
    Vertex v1 = vertices[i1];
    Vertex v2 = vertices[i2];
    Vertex v3 = vertices[i3];
    Vertex v4 = new Vertex(i4);
    HalfEdge h01 = getHalfEdge(i0,i1);
//  HalfEdge h12 = h01.next;
//  HalfEdge h20 = h12.next;
    HalfEdge h10 = h01.twin;
//  HalfEdge h03 = h10.next;
//  HalfEdge h31 = h03.next;
    HalfEdge h04 = new HalfEdge();
    HalfEdge h40 = new HalfEdge();
    HalfEdge h14 = new HalfEdge();
    HalfEdge h41 = new HalfEdge();
    HalfEdge h24 = new HalfEdge();
    HalfEdge h42 = new HalfEdge();
    HalfEdge h34 = new HalfEdge();
    HalfEdge h43 = new HalfEdge();
    linkTwins(h04,h40);
    linkTwins(h14,h41);
    linkTwins(h24,h42);
    linkTwins(h34,h43);
    h04.origin = v0;
    h40.origin = v4;
    h14.origin = v1;
    h41.origin = v4;
    h24.origin = v2;
    h42.origin = v4;
    h34.origin = v3;
    h43.origin = v4;
    v0.repair(h01,h04);
    v1.repair(h10,h14);
    v4.edge = h40;
    Face f042 = new Face();
    Face f412 = new Face();
    Face f143 = new Face();
    Face f403 = new Face();
    linkEdgesAndFace(h04, f042);
    linkEdgesAndFace(h41, f412);
    linkEdgesAndFace(h14, f143);
    linkEdgesAndFace(h40, f403);
    h01.inUse = false;
    h10.inUse = false;
    return h04;
  }
  /** For debugging: check that edges for which inUse==false, are no longer
   *  referenced by the other edges.*/ 
  public void checkEdges() {
    for(HalfEdge h : edges) {
      if(h.inUse) {
        Util.myAssert(h.twin.inUse && h.next.inUse && h.prev.inUse);
      }
    }
  }
  public void writeToFile() {
    Date now = new Date();
    String s = new SimpleDateFormat("yyyyMMdd-HHmmss").format(now);
    String dirName = "./data/" + s;
    File dir = new File(dirName);
    dir.mkdir();
    if(!dir.exists()) {
      throw new IllegalStateException(dir.getPath() + "does not exist");
    }
    String fileName = "pointsAndLines.txt";  
    
    try(FileWriter fw = new FileWriter(dirName + "/" + fileName);
        BufferedWriter bw = new BufferedWriter(fw)) {
      writePointsAndLines(bw);
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }
  public void writePointsAndLines(BufferedWriter writer) throws IOException {
    writer.write(""+points.length);
    writer.newLine();
    for(Point p : points) {
      writer.write(""+p.x+" "+p.y);
      writer.newLine();
    }
    int numHalfEdges = 0;
    for(HalfEdge h : edges) {
      if(h.inUse) {
        if(h.origin.index>=0 && h.twin.origin.index>=0) {
          ++numHalfEdges;
        }
      }
    }
    writer.write(""+numHalfEdges);
    writer.newLine();
    for(HalfEdge h : edges) {
      if(h.inUse) {
        if(h.origin.index>=0 && h.twin.origin.index>=0) {
          writer.write(""+h.origin.index + " " + h.twin.origin.index);
          writer.newLine();
        }
      }
    }
  }
  
  public void writePointsAndTriangles(BufferedWriter writer) throws IOException {
    writer.write(""+points.length);
    writer.newLine();
    for(Point p : points) {
      writer.write(""+p.x+" "+p.y);
      writer.newLine();
    }
    //
    TreeSet<Face> faces = getFaces();
    writer.write(""+faces.size());
    writer.newLine();
    for(Face f : faces) {
      int[] tri = f.calcTriangle();
      writer.write(""+tri[0]+" "+tri[1]+" "+tri[2]);
      writer.newLine();
    }
  }
  public TreeSet<Face> getFaces() {
    TreeSet<Face> faces = new TreeSet<>();
    for(Vertex v : vertices) {
      if(v==null) {
        continue;
      }
      Iterator<HalfEdge> hIter = v.edgeIterator();
      while(hIter.hasNext()) {
        HalfEdge h = hIter.next();
        Face f = h.face;
        faces.add(f);
      }
    }
    return faces;
  }
}
