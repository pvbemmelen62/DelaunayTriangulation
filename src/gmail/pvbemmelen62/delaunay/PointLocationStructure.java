package gmail.pvbemmelen62.delaunay;

import java.util.*;

public class PointLocationStructure {

  private Point[] points;
  private int iLargest;
  public Node top;
  
  public class Node {
    Node[] children;
    /** Indices into points[] ; triangle orientation: rotation when going from
     *  points[triangle[j]] for j=0 to j=1 to j=2 to j=0, is in -z direction.
     *  Smallest value in triangle is at beginning of array.
     */
    int[] triangle;
    public Node() {
      this.children = null;
    }
    public Node(int p0, int p1, int p2) {
      this();
      triangle = new int[] {p0, p1, p2};
    }
    public Node(int[] triangle) {
      this();
      this.triangle = triangle;
    }
    @Override
    public String toString() {
      return "Node [children=" + Arrays.toString(children) + ", triangle="
          + Arrays.toString(triangle) + "]";
    }
    public Node[] findContainingChildren(Point p) {
      if(children==null) {
        return null;
      }
      if(children.length==2) {
        return findContainingChildren(children[0], children[1], p);
      }
      else if(children.length==3) {
        Node[] ns01 = findContainingChildren(children[0], children[1], p);
        Node[] ns12 = findContainingChildren(children[1], children[2], p);
        Node[] ns20 = findContainingChildren(children[2], children[0], p);
        int[] childCounts = {0,0,0};
        for(Node[] nds : new Node[][] { ns01, ns12, ns20 }) {
          for(Node nd : nds) {
            ++childCounts[childIndex(nd)];
          }
        }
        int[] indices = new int[3];
        int numIndices = childIndices(childCounts, 2, indices);
        if(numIndices==1) {
          return new Node[] { children[indices[0]] };
        }
        else if(numIndices==2) {
          return new Node[] { children[indices[0]], children[indices[1]] };
        }
        else { // probably due to two equal points in input.
          throw new IllegalStateException("Unexpected numIndices: "
            + numIndices);
        }
      }
      throw new IllegalStateException("Unexpected children.length: "
          + children.length);
    }
    /**
     * Returns the number of entries in <code>childCounts</code> that are
     * equal to <code>count</code>.
     * The array <code>indices</code> upon return will contain the indices of
     * childCounts for which this is true. The client must pass in this array
     * of sufficient length.
     */
    protected int childIndices(int[] childCounts, int count, int[] indices) {
      int numIndices = 0;
      for(int i=0; i<childCounts.length; ++i) {
        if(childCounts[i]==count) {
          indices[numIndices++] = i;
        }
      }
      return numIndices;
    }
    protected int childIndex(Node child) {
      if(children[0]==child) {
        return 0;
      }
      else if(children[1]==child) {
        return 1;
      }
      else if(children[2]==child) {
        return 2;
      }
      return -1;
    }
    /** Boundary of node0.triangle and node1.triangle decides which one/two
     *  contain points[pointIndex] .*/
    protected Node[] findContainingChildren(Node node0, Node node1,
        Point p) {
      int[] tri0 = node0.triangle;
      int[] tri1 = node1.triangle;
      int[] commons = Triangle.findCommonPoints(tri0, tri1);
      Comparator<Point> comparator = DelaunayTriangulation.yThenXComparator;
      Util.myAssert(!(commons[0]<0 && commons[1]<0));
      if(tri0[commons[0]]==-2) {
        Point q = points[tri0[commons[1]]];
        // NB: line from -2 to q is very near horizontal because -2 has very
        //   neg x coord.
        int cmp = comparator.compare(p,q);
        Util.myAssert(cmp!=0);
        // p lies in tri0 <==> -2 to q to p is rotation in -z-direction
        if(cmp<0) {
          // -2 to q to p is in -z-direction
          return new Node[] { node0 };
        }
        else {
          return new Node[] { node1 };
        }
      }
      else if(tri0[commons[1]]==-2) {
        Point q = points[tri0[commons[0]]];
        int cmp = comparator.compare(p,q);
        Util.myAssert(cmp!=0);
        // p lies in tri0 <==> q to -2 to p is rotation in -z-direction
        if(cmp>0) {
          // q to -2 to p is in -z-direction
          return new Node[] { node0 };
        }
        else {
          return new Node[] { node1 };
        }
      }
      if(tri0[commons[0]]==-1) {
        Point q = points[tri0[commons[1]]];
        // NB: line from -1 to q is very near horizontal because -1 has very
        //   large x coord.
        int cmp = comparator.compare(p,q);
        Util.myAssert(cmp!=0);
        // p lies in tri0 <==> -1 to q to p is rotation in -z-direction
        if(cmp>0) {
          // -1 to q to p is in -z-direction
          return new Node[] { node0 };
        }
        else {
          return new Node[] { node1 };
        }
      }
      else if(tri0[commons[1]]==-1) {
        Point q = points[tri0[commons[0]]];
        // NB: line from q to -1 is very near horizontal because -1 has very
        //   large x coord.
        int cmp = comparator.compare(p,q);
        Util.myAssert(cmp!=0);
        // p lies in tri0 <==> q to -1 to p is rotation in -z-direction
        if(cmp<0) {
          // q to -1 to p is in -z-direction
          return new Node[] { node0 };
        }
        else {
          return new Node[] { node1 };
        }
      }
      int hpp = Triangle.getOrientation(
          points[tri0[commons[0]]], points[tri0[commons[1]]], p);
      if(hpp==1) {
        return new Node[] {node1};
      }
      else if(hpp==-1) {
        return new Node[] {node0};
      }
      else {
        return new Node[] {node0, node1};
      }
    }
  }
  /** Creates a PointLocationStructure containing only the top node consisting
   * of triangle {-2,0,-1}.
   * @param points points of triangulation; points[0] must be largest according
   *   to DelaunayTriangulation.yThenXComparator.
   */
  public PointLocationStructure(Point[] points, int iLargest) {
    this.points = points;
    this.iLargest = iLargest;
    top = new Node();
    top.triangle = new int[] {-2, iLargest, -1};
  }
  /** Returns array of nodes that contain point p.
   * If p lies internal within the boundary of a node, then the array contains
   * that one node; if p lies on the boundary of two nodes, then the array
   * contains those two nodes.
   */
  public Node[] findContainingLeafNodes(Point p) {
    Deque<Node> deque = new LinkedList<>();
    deque.addLast(top);
    Node[] leafNodes = new Node[2];
    int numLeafNodes = 0;
    while(!deque.isEmpty()) {
      Node node = deque.removeFirst();
      Node[] children = node.findContainingChildren(p);
      if(children==null) {
        if(numLeafNodes > 1) {
          // Question: may a leaf node show up more than once ?
          throw new IllegalStateException("More than 2 containing leaf nodes.");
        }
        leafNodes[numLeafNodes++] = node;
      }
      else {
        for(Node child : children) {
          deque.addLast(child);
        }
      }
    }
    if(numLeafNodes==0) {
      throw new IllegalStateException("No containing leaf nodes found.");
    }
    else if(numLeafNodes==1) {
      return new Node[] { leafNodes[0] };
    }
    else /* if(numLeafNodes==2) */ {
      return leafNodes;
    }
  }
//  private boolean equalEdges(int[] e0, int[] e1) {
//    return (e0[0]==e1[0] && e0[1]==e1[1])
//        || (e0[0]==e1[1] && e0[1]==e1[0]);
//  }
  /**
   * Splits <code>node</code> because it contains the new point
   * <code>pointIndex</code>, and creates new nodes as children of node.
   * If the node specified has triangle {i0,i1,i2}, then the order of the
   * nodes created as children of node, is
   *   {i0,i1,i3}, {i1,i2,i3}, {i2,i0,i3}
   * where
   *   i3 : pointIndex
   * @param pointIndex
   */
  public void splitContainingLeafNode(Node node, int pointIndex) {
    int[] tri = node.triangle;
    node.children = new Node[3];
    for(int i=0; i<3; ++i) {
      Node child = new Node();
      int[] ctri = { tri[i], tri[(i+1)%3], pointIndex };
      Triangle.toCanonical(ctri);
      child.triangle = ctri;
      node.children[i] = child;
    }
  }
 /**
  * Splits node0 with triangle {i0,i1,i2} or a rotation of this, and node1 with
  * triangle {i1,i0,i3} or a rotation of this, because edges {i0,i1} and {i1,i0}
  * are split by the new point i4.
  * Creates children of node0 and node1, respectively cdn0[] and cdn1[], such
  * that<ul>
  *   <li>cdn0[0] corresponds to triangle {i0,i4,i2}</li>
  *   <li>cdn0[1] corresponds to triangle {i4,i1,i2}</li>
  *   <li>cdn1[0] corresponds to triangle {i1,i4,i3}</li>
  *   <li>cdn1[1] corresponds to triangle {i4,i0,i3}</li>
  * </ul>
  * @see Fig 9.7 in Computational Geometry - Algorithms and Applications,
  *  3rd Ed. , page 200
  */
  public void splitContainingLeafNodes(Node node0, Node node1, int i4) {
    int[][] tris = { node0.triangle, node1.triangle} ;
    int[] commons = Triangle.findCommonPoints(tris[0], tris[1]);
    int i0 = tris[0][commons[0]];
    int i1 = tris[0][commons[1]];
    int i2 = tris[0][Triangle.nonCommon(commons)];
    int i3 = tris[1][Triangle.theOtherPoint(tris[1], new int[] {i0,i1})];

    Node[] children;
    //
    children = new Node[] { new Node(), new Node() };
    children[0].triangle = Triangle.toCanonical(new int[] { i0,i4,i2 });
    children[1].triangle = Triangle.toCanonical(new int[] { i4,i1,i2 });
    node0.children = children;
    //
    children = new Node[] { new Node(), new Node() };
    children[0].triangle = Triangle.toCanonical(new int[] { i1,i4,i3 });
    children[1].triangle = Triangle.toCanonical(new int[] { i4,i0,i3 });
    node1.children = children;
  }
  public Node[] flipEdge(Node node0, Node node1, int[] tri0, int[] tri1) {
    Node nodeNew0 = new Node(tri0);
    Node nodeNew1 = new Node(tri1);
    Node[] children = new Node[]{nodeNew0, nodeNew1};
    node0.children = children;
    node1.children = children;
    return children;
  }
  
}
