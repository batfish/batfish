package org.batfish.datamodel.collections;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;

public class CommunitySet extends LinkedHashSet<Long> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public CommunitySet() {
   }

   public CommunitySet(CommunitySet communitySet) {
      super(communitySet);
   }

   public Set<String> asStringSet() {
      Set<Long> sortedCommunities = new TreeSet<>();
      sortedCommunities.addAll(this);
      Set<String> strings = new LinkedHashSet<>();
      for (long communityLong : sortedCommunities) {
         String commmunityStr = CommonUtil.longToCommunity(communityLong);
         strings.add(commmunityStr);
      }
      return strings;
   }

}
