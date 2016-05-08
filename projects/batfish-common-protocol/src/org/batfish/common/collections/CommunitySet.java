package org.batfish.common.collections;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.util.CommonUtil;

public class CommunitySet extends HashSet<Long> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public Set<String> asStringSet() {
      Set<Long> sortedCommunities = new TreeSet<Long>();
      sortedCommunities.addAll(this);
      Set<String> strings = new LinkedHashSet<String>();
      for (long communityLong : sortedCommunities) {
         String commmunityStr = CommonUtil.longToCommunity(communityLong);
         strings.add(commmunityStr);
      }
      return strings;
   }

}
