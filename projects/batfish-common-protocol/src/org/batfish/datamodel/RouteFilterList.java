package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteFilterList extends ComparableStructure<String> {

   private static final String LINES_VAR = "lines";

   private static final long serialVersionUID = 1L;

   private final List<RouteFilterLine> _lines;

   @JsonCreator
   public RouteFilterList(@JsonProperty(LINES_VAR)List<RouteFilterLine> lines, @JsonProperty(NAME_VAR)String name) {
      super(name);
      _lines = lines;
   }

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

   @JsonProperty(LINES_VAR)
   public List<RouteFilterLine> getLines() {
      return _lines;
   }

}
