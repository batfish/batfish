package org.batfish.representation;

import java.util.List;

public class PolicyMapSetAddCommunityLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Long> _communities;

   public PolicyMapSetAddCommunityLine(List<Long> communities) {
      _communities = communities;
   }

   public List<Long> getCommunities() {
      return _communities;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.ADDITIVE_COMMUNITY;
   }

}
