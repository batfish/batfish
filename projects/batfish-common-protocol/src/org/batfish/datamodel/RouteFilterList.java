package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteFilterList extends ComparableStructure<String> {

   private static final String LINES_VAR = "lines";

   private static final long serialVersionUID = 1L;

   private transient Set<Prefix> _deniedCache;

   private List<RouteFilterLine> _lines;

   private transient Set<Prefix> _permittedCache;

   @JsonCreator
   public RouteFilterList(@JsonProperty(NAME_VAR) String name) {
      super(name);
      _lines = new ArrayList<>();
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

   private boolean newPermits(Prefix prefix) {
      boolean accept = false;
      for (RouteFilterLine line : _lines) {
         Prefix linePrefix = line.getPrefix();
         int lineBits = linePrefix.getPrefixLength();
         Prefix truncatedLinePrefix = new Prefix(linePrefix.getAddress(),
               lineBits);
         Prefix relevantPortion = new Prefix(prefix.getAddress(), lineBits)
               .getNetworkPrefix();
         if (relevantPortion.equals(truncatedLinePrefix)) {
            int prefixLength = prefix.getPrefixLength();
            SubRange range = line.getLengthRange();
            int min = range.getStart();
            int max = range.getEnd();
            if (prefixLength >= min && prefixLength <= max) {
               accept = line.getAction() == LineAction.ACCEPT;
               break;
            }
         }

      }
      if (accept) {
         _permittedCache.add(prefix);
      }
      else {
         _deniedCache.add(prefix);
      }
      return accept;
   }

   public boolean permits(Prefix prefix) {
      if (_permittedCache == null) {
         _deniedCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
         _permittedCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
      }
      else if (_deniedCache.contains(prefix)) {
         return false;
      }
      else if (_permittedCache.contains(prefix)) {
         return true;
      }
      return newPermits(prefix);
   }

   @JsonProperty(LINES_VAR)
   public void setLines(List<RouteFilterLine> lines) {
      _lines = lines;
   }

}
