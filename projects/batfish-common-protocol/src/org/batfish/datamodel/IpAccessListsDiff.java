package org.batfish.datamodel;

import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAccessListsDiff extends ConfigDiffElement {

   private static final String DIFF = "diff";
   private Set<String> _diff;

   @JsonCreator()
   public IpAccessListsDiff() {

   }

   public IpAccessListsDiff(NavigableMap<String, IpAccessList> a,
         NavigableMap<String, IpAccessList> b) {

      super(a.keySet(), b.keySet());
      _diff = new HashSet<>();
      for (String name : _common) {
         if (a.get(name).unorderedEqual(b.get(name))) {
            _identical.add(name);
         }
         else {
            _diff.add(name);
         }
      }
   }

   @JsonProperty(DIFF)
   public Set<String> getDiff() {
      return _diff;
   }

   public void setDiff(Set<String> _diff) {
      this._diff = _diff;
   }
}
