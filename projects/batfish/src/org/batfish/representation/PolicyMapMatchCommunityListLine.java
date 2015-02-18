package org.batfish.representation;

import java.util.Set;

public class PolicyMapMatchCommunityListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<CommunityList> _lists;

   public PolicyMapMatchCommunityListLine(Set<CommunityList> lists) {
      _lists = lists;
   }

   public Set<CommunityList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.COMMUNITY_LIST;
   }

}
