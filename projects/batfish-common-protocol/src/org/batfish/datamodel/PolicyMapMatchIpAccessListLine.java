package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class PolicyMapMatchIpAccessListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<IpAccessList> _lists;

   public PolicyMapMatchIpAccessListLine(Set<IpAccessList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<IpAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.IP_ACCESS_LIST;
   }

}
