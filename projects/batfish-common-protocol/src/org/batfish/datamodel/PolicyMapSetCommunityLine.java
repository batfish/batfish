package org.batfish.datamodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetCommunityLine extends PolicyMapSetLine {

   private static final String COMMUNITIES_VAR = "communities";

   private static final long serialVersionUID = 1L;

   private final List<Long> _communities;

   @JsonCreator
   public PolicyMapSetCommunityLine(
         @JsonProperty(COMMUNITIES_VAR) List<Long> communities) {
      _communities = communities;
   }

   @JsonProperty(COMMUNITIES_VAR)
   public List<Long> getCommunities() {
      return _communities;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.COMMUNITY;
   }

}
