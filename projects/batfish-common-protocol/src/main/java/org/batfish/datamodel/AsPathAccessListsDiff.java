package org.batfish.datamodel;

import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AsPathAccessListsDiff extends ConfigDiffElement {

   @JsonCreator()
   public AsPathAccessListsDiff() {
   }

   public AsPathAccessListsDiff(NavigableMap<String, AsPathAccessList> before,
         NavigableMap<String, AsPathAccessList> after) {
      super(before, after, false);
   }

}
