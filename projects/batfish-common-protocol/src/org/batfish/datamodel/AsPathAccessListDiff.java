package org.batfish.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AsPathAccessListDiff extends ConfigDiffElement {

   private static final String DIFF = "diff";
   private Map<String, Map<String, AsPathAccessList>> _diff;

   @JsonCreator()
   public AsPathAccessListDiff() {

   }

   public AsPathAccessListDiff(NavigableMap<String, AsPathAccessList> a,
         NavigableMap<String, AsPathAccessList> b) {
      super(a.keySet(), b.keySet());
      _diff = new HashMap<String, Map<String, AsPathAccessList>>();
      for (String name : _common) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
            Map<String, AsPathAccessList> info = new HashMap<String, AsPathAccessList>();
            info.put("a", a.get(name));
            info.put("b", b.get(name));
            _diff.put(name, info);
         }
      }
   }

   /**
    * @return the _diff
    */
   @JsonProperty(DIFF)
   public Map<String, Map<String, AsPathAccessList>> get_diff() {
      return _diff;
   }

   public void set_diff(Map<String, Map<String, AsPathAccessList>> d) {
      this._diff = d;
   }

}
