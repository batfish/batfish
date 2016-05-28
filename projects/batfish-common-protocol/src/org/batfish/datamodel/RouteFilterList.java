package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;
import org.batfish.common.util.UseForEqualityCheck;

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

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      RouteFilterList other = (RouteFilterList) obj;
      return other._lines.equals(_lines);
   }
   
   public List<RouteFilterLine> getLines() {
      return _lines;
   }

}
