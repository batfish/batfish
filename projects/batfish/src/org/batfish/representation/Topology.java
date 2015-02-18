package org.batfish.representation;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

public class Topology implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<Edge> _edges;

   public Topology(List<Edge> edges) {
      _edges = edges;
   }

   /**
    * Dumps the topology as a dot-file for debugging.
    *
    * @param out
    *           The output stream to dump to.
    */
   public void dumpDot(OutputStream _out) {
      PrintStream out = new PrintStream(_out);
      out.println("digraph topology {");
      // TODO: complete
      // for (Edge edge : _edges) {
      // out.println("  ");
      // }
      out.println("}");
   }

   public List<Edge> getEdges() {
      return _edges;
   }

}
