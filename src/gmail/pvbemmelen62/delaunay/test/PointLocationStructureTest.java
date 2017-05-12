package gmail.pvbemmelen62.delaunay.test;

import java.util.*;

import gmail.pvbemmelen62.delaunay.*;
import gmail.pvbemmelen62.delaunay.PointLocationStructure.*;

public class PointLocationStructureTest {

  public static void main(String[] args) {
    int numPoints = 20;
    Long seed = 0L;
    Random random = new Random(seed);
    Point[] points = Points.randomPoints(numPoints, random);
    int iLargest = DelaunayTriangulation.findLargestPoint(points);
    DelaunayTriangulation.swap(points, 0, iLargest);
    PointLocationStructure pls = new PointLocationStructure(points);
    int[] permutation = Util.permutation(random, 1, numPoints-1);
    for(int i=0; i<numPoints-1; ++i) {
      int pointIndex = permutation[i];
      Node[] nodes = pls.findContainingLeafNodes(pointIndex);
      if(nodes.length==1) {
        pls.splitContainingLeafNode(nodes[0], pointIndex);
      }
      else {
        pls.splitContainingLeafNodes(nodes[0], nodes[1], pointIndex);
      }
    }
  }
}
