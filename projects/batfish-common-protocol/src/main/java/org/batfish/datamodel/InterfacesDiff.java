package org.batfish.datamodel;

import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;

public class InterfacesDiff extends ConfigDiffElement {

   @JsonCreator()
   public InterfacesDiff() {
   }

   public InterfacesDiff(NavigableMap<String, Interface> before,
         NavigableMap<String, Interface> after) {
      super(before, after, true);
   }

}
