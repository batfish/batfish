package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchCommunityListLine extends PolicyMapMatchLine {

   private static final String LISTS_VAR = "lists";

   private static final long serialVersionUID = 1L;

   private Set<CommunityList> _lists;

   @JsonCreator
   public PolicyMapMatchCommunityListLine() {
   }

   public PolicyMapMatchCommunityListLine(Set<CommunityList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(LISTS_VAR)
   public Set<CommunityList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.COMMUNITY_LIST;
   }

   @JsonProperty(LISTS_VAR)
   public void setLists(Set<CommunityList> lists) {
      _lists = lists;
   }

}
