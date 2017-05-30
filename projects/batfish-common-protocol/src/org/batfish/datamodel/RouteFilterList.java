package org.batfish.datamodel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("An access-list used to filter IPV4 routes")
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
   @JsonPropertyDescription("The lines against which to check an IPV4 route")
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
      if (_deniedCache.contains(prefix)) {
         return false;
      }
      else if (_permittedCache.contains(prefix)) {
         return true;
      }
      return newPermits(prefix);
   }

   private void readObject(ObjectInputStream in)
         throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      _deniedCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
      _permittedCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
   }

   @JsonProperty(LINES_VAR)
   public void setLines(List<RouteFilterLine> lines) {
      _lines = lines;
   }

}
