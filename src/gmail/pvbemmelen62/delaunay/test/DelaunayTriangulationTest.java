package gmail.pvbemmelen62.delaunay.test;

import java.util.*;

import gmail.pvbemmelen62.delaunay.*;
import gmail.pvbemmelen62.delaunay.dcel.*;

public class DelaunayTriangulationTest {

  public static void main(String[] args) {
    int numPoints = 2000;
    Long seed = 0L;
    Random random = new Random(seed);
    Point[] points = Points.randomPoints(numPoints, random);
    int iLargest = DelaunayTriangulation.findLargestPoint(points);
    Util.swap((Object[])points, 0, iLargest);
    
    // What happens when there are two equal points?
    //  points[20] = new Point(points[10]);
// Exception in thread "main" java.lang.IllegalStateException: Unexpected numIndices: 3
// at ...PointLocationStructure$Node.findContainingChildren(PointLocationStructure.java:60)
// at ...PointLocationStructure.findContainingLeafNodes(PointLocationStructure.java:198)
// at ...DelaunayTriangulation.triangulate(DelaunayTriangulation.java:82)
// at ...DelaunayTriangulation.<init>(DelaunayTriangulation.java:67)
// at ...test.DelaunayTriangulationTest.main(DelaunayTriangulationTest.java:23)
    
    
    DelaunayTriangulation dt = new DelaunayTriangulation(points);
    
    Dcel dcel = dt.getDcel();
    
//    String fileName = args[0];
//    try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
//      dcel.writePointsAndLines(bw);
//    }
//    catch(IOException ioe) {
//      ioe.printStackTrace();
//    }
    dcel.writeToFile();
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException e) {
    }
    gmail.pvbemmelen62.delaunay.plot.PlotDebug.main(new String[] { "" });
  }
}
