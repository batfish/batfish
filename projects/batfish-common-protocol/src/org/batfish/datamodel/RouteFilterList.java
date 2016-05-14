package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;

public class RouteFilterList extends ComparableStructure<String> {

   private static final long serialVersionUID = 1L;

   private List<RouteFilterLine> _lines;

   public RouteFilterList(String name) {
      super(name);
      _lines = new ArrayList<RouteFilterLine>();
   }

   public void addLine(RouteFilterLine r) {
      _lines.add(r);
   }

   public List<RouteFilterLine> getLines() {
      return _lines;
   }

}
