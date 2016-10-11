package org.batfish.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AsPathAccessListsDiff extends ConfigDiffElement {

   private static final String DIFF = "diff";
   private Map<String, Map<String, AsPathAccessList>> _diff;

   @JsonCreator()
   public AsPathAccessListsDiff() {

   }

   public AsPathAccessListsDiff(NavigableMap<String, AsPathAccessList> a,
         NavigableMap<String, AsPathAccessList> b) {
      super(a.keySet(), b.keySet());
      _diff = new HashMap<>();
      for (String name : _common) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
            Map<String, AsPathAccessList> info = new HashMap<>();
            info.put("a", a.get(name));
            info.put("b", b.get(name));
            _diff.put(name, info);
         }
      }
   }

   @JsonProperty(DIFF)
   public Map<String, Map<String, AsPathAccessList>> getDiff() {
      return _diff;
   }

   public void setDiff(Map<String, Map<String, AsPathAccessList>> d) {
      this._diff = d;
   }

}
