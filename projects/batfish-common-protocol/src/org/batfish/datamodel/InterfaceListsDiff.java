package org.batfish.datamodel;

import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InterfaceListsDiff extends ConfigDiffElement {
   private static final String DIFF = "diff";
   private Set<String> _diff;

   @JsonCreator()
   public InterfaceListsDiff() {

   }

   public InterfaceListsDiff(NavigableMap<String, Interface> a,
         NavigableMap<String, Interface> b) {
      super(a.keySet(), b.keySet());
      _diff = new HashSet<>();
      for (String name : _common) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
           _diff.add(name);
         }
      }
   }

   @JsonProperty(DIFF)
   public Set<String>  getDiff() {
      return _diff;
   }

   public void setDiff(Set<String> d) {
      this._diff = d;
   }
}
