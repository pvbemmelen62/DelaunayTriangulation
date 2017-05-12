package gmail.pvbemmelen62.delaunay.plot;

import java.io.*;

public class PlotDebug {

  public static void main(String[] args) {
    File dir = new File("./data");
    File[] dirs = dir.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });
    String lastName = "000";
    File lastDir = null;
    for(File d : dirs) {
      String name = d.getName();
      if(name.compareTo(lastName) > 0) {
        lastDir = d;
      }
    }
    plp.plot.Plotter.main(
        new String[] { "-file=" + "./data/" + lastDir.getName()
          + "/pointsAndLines.txt" }
    );
  }

}
