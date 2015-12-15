package org.batfish.graphviz;

import org.batfish.util.NamedStructure;

public class GraphvizNode extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _label;

   public GraphvizNode(String name) {
      super(name);
   }

   public String getLabel() {
      return _label;
   }

   public void setLabel(String label) {
      _label = label;
   }

   @Override
   public String toString() {
      return _name + "[label=\"" + _label + "\"];";
   }

}
