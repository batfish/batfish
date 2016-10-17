package org.batfish.datamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteFilterListsDiff extends ConfigDiffElement {

   private static final String DIFF_INFO_VAR = "diffInfo";
   protected static final String DIFF_VAR = "diff";
   protected Set<String> _diff;
   private Map<String, ConfigDiffElement> _diffInfo;

   @JsonCreator()
   public RouteFilterListsDiff() {

   }

   public RouteFilterListsDiff(NavigableMap<String, RouteFilterList> a,
         NavigableMap<String, RouteFilterList> b) {

      super(a.keySet(), b.keySet());
      _diff = new TreeSet<>();
      _diffInfo = new HashMap<>();
      for (String name : super.common()) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
            _diff.add(name);
            genDiffInfo(a, b, name);
         }
      }
   }

   private void genDiffInfo(NavigableMap<String, RouteFilterList> a,
         NavigableMap<String, RouteFilterList> b, String name) {

      Set<String> aNames = new TreeSet<>();
      Set<String> bNames = new TreeSet<>();
      ConfigDiffElement di = new ConfigDiffElement(aNames, bNames);
      List<RouteFilterLine> aLines = a.get(name).getLines();
      List<RouteFilterLine> bLines = b.get(name).getLines();
      for (RouteFilterLine line : aLines) {
         if (bLines.contains(line)) {
            di._identical.add(line.toCompactString());
         }
         else {
            di._inAOnly.add(line.toCompactString());
         }
      }

      for (RouteFilterLine line : bLines) {
         if (!aLines.contains(line)) {
            di._inBOnly.add(line.toCompactString());
         }
      }
      _diffInfo.put(name, di);
   }

   @JsonProperty(DIFF_VAR)
   public Set<String> getDiff() {
      return _diff;
   }

   @JsonProperty(DIFF_INFO_VAR)
   public Map<String, ConfigDiffElement> getDiffInfo() {
      return _diffInfo;
   }

   public void setDiff(Set<String> diff) {
      _diff = diff;
   }

   public void setDiffInfo(Map<String, ConfigDiffElement> diffInfo) {
      _diffInfo = diffInfo;
   }

}
