package org.batfish.representation;

import java.util.ArrayList;
import java.util.List;

import org.batfish.util.NamedStructure;

public class RouteFilterList extends NamedStructure {

   private static final long serialVersionUID = 1L;

   private List<RouteFilterLine> _lines;

   public RouteFilterList(String name) {
      super(name);
      _lines = new ArrayList<RouteFilterLine>();
   }

   public void addLine(RouteFilterLine r) {
      _lines.add(r);
   }

   public void addLines(List<RouteFilterLine> r) {
      _lines.addAll(r);
   }

   public List<RouteFilterLine> getLines() {
      return _lines;
   }

}
