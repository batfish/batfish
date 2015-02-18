package org.batfish.representation;

import java.util.Set;

public class PolicyMapMatchIpAccessListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<IpAccessList> _lists;

   public PolicyMapMatchIpAccessListLine(Set<IpAccessList> lists) {
      _lists = lists;
   }

   public Set<IpAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.IP_ACCESS_LIST;
   }

}
