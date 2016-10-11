package org.batfish.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAccessListDiff extends ConfigDiffElement {

   private static final String DIFF = "diff";
   private Map<String, Map<String, IpAccessList>> _diff;
   
   @JsonCreator()
   public IpAccessListDiff() {

   }

   public IpAccessListDiff(NavigableMap<String, IpAccessList> a,
         NavigableMap<String, IpAccessList> b) {
      super(a.keySet(), b.keySet());
      _diff = new HashMap<String, Map<String, IpAccessList>>();
      for (String name : _common) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
            Map<String, IpAccessList> info = new HashMap<String, IpAccessList>();
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
   public Map<String, Map<String, IpAccessList>> get_diff() {
      return _diff;
   }

   public void set_diff(Map<String, Map<String, IpAccessList>> d) {
      this._diff = d;
   }
   

}
