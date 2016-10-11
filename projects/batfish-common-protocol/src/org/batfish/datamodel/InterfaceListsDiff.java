package org.batfish.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InterfaceListsDiff extends ConfigDiffElement {
   private static final String DIFF = "diff";
   private Map<String, Map<String, String>> _diff;

   @JsonCreator()
   public InterfaceListsDiff() {

   }

   public InterfaceListsDiff(NavigableMap<String, Interface> a,
         NavigableMap<String, Interface> b) {
      super(a.keySet(), b.keySet());
      _diff = new HashMap<>();
      for (String name : _common) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
            Map<String, String> info = new HashMap<>();
            info.put("a", a.get(name).getName());
            info.put("b", b.get(name).getName());
            _diff.put(name, info);
         }
      }
   }

   @JsonProperty(DIFF)
   public Map<String, Map<String, String>> getDiff() {
      return _diff;
   }

   public void setDiff(Map<String, Map<String, String>> d) {
      this._diff = d;
   }
}
