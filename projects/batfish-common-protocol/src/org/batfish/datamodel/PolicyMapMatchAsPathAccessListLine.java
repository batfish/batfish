package org.batfish.datamodel;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchAsPathAccessListLine extends PolicyMapMatchLine {

   private static final String LISTS_VAR = "lists";

   private static final long serialVersionUID = 1L;

   private Set<AsPathAccessList> _lists;

   @JsonCreator
   public PolicyMapMatchAsPathAccessListLine() {
   }

   public PolicyMapMatchAsPathAccessListLine(Set<AsPathAccessList> lists) {
      _lists = lists;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(LISTS_VAR)
   public Set<AsPathAccessList> getLists() {
      return _lists;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.AS_PATH_ACCESS_LIST;
   }

   @JsonProperty(LISTS_VAR)
   public void setLists(Set<AsPathAccessList> lists) {
      _lists = lists;
   }

}
