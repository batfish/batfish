package org.batfish.representation;

import java.util.Set;

public class PolicyMapMatchAsPathAccessListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<AsPathAccessList> _lists;

   public PolicyMapMatchAsPathAccessListLine(Set<AsPathAccessList> lists) {
      _lists = lists;
   }

   public Set<AsPathAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.AS_PATH_ACCESS_LIST;
   }

}
