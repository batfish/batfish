package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class PolicyMapMatchAsPathAccessListLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<AsPathAccessList> _lists;

   public PolicyMapMatchAsPathAccessListLine(Set<AsPathAccessList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<AsPathAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.AS_PATH_ACCESS_LIST;
   }

}
