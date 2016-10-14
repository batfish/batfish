package org.batfish.datamodel;

import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AsPathAccessListsDiff extends ConfigDiffElement {

   @JsonCreator()
   public AsPathAccessListsDiff() {

   }

   public AsPathAccessListsDiff(NavigableMap<String, AsPathAccessList> a,
         NavigableMap<String, AsPathAccessList> b) {
      super(a.keySet(), b.keySet());
      for (String name : super.common()) {
         if (a.get(name).equals(b.get(name))) {
            _identical.add(name);
         }
         else {
            _diff.add(name);
         }
      }
   }
}
