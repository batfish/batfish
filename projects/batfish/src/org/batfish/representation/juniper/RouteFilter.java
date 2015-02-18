package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.batfish.util.NamedStructure;

public final class RouteFilter extends NamedStructure implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Map<RouteFilterLine, RouteFilterLine> _lines;

   public RouteFilter(String name) {
      super(name);
      _lines = new LinkedHashMap<RouteFilterLine, RouteFilterLine>();
   }

   public Set<RouteFilterLine> getLines() {
      return _lines.keySet();
   }

   public RouteFilterLine insertLine(RouteFilterLine line) {
      RouteFilterLine existingLine = _lines.get(line);
      if (existingLine == null) {
         _lines.put(line, line);
         return line;
      }
      else {
         return existingLine;
      }
   }

}
