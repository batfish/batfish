package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchIpAccessListLine extends PolicyMapMatchLine {

   private static final String LISTS_VAR = "lists";

   private static final long serialVersionUID = 1L;

   private Set<IpAccessList> _lists;

   @JsonCreator
   public PolicyMapMatchIpAccessListLine() {
   }

   public PolicyMapMatchIpAccessListLine(Set<IpAccessList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(LISTS_VAR)
   public Set<IpAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.IP_ACCESS_LIST;
   }

   @JsonProperty(LISTS_VAR)
   public void setLists(Set<IpAccessList> lists) {
      _lists = lists;
   }

}
