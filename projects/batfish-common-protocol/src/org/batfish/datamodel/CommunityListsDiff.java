package org.batfish.datamodel;

import java.util.NavigableMap;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CommunityListsDiff extends ConfigDiffElement {

   @JsonCreator()
   public CommunityListsDiff() {
   }

   public CommunityListsDiff(NavigableMap<String, CommunityList> before,
         NavigableMap<String, CommunityList> after) {
      super(before, after, false);
   }

}
