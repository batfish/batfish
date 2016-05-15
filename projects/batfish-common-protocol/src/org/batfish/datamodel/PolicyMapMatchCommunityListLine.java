package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class PolicyMapMatchCommunityListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<CommunityList> _lists;

   public PolicyMapMatchCommunityListLine(Set<CommunityList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<CommunityList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.COMMUNITY_LIST;
   }

}
