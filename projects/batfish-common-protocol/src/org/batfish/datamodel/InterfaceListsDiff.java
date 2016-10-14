package org.batfish.datamodel;

import java.util.HashSet;
import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;

public class InterfaceListsDiff extends ConfigDiffElement {

   @JsonCreator()
   public InterfaceListsDiff() {

   }

   public InterfaceListsDiff(NavigableMap<String, Interface> a,
         NavigableMap<String, Interface> b) {
      super(a.keySet(), b.keySet());
      _diff = new HashSet<>();
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
