package org.batfish.datamodel;

import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CommunityListsDiff extends ConfigDiffElement {

   @JsonCreator()
   public CommunityListsDiff() {

   }

   public CommunityListsDiff(NavigableMap<String, CommunityList> a,
         NavigableMap<String, CommunityList> b) {
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
