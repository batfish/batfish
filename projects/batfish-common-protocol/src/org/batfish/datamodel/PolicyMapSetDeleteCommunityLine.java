package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetDeleteCommunityLine extends PolicyMapSetLine {

   private static final String LIST_VAR = "list";

   private static final long serialVersionUID = 1L;

   private CommunityList _list;

   @JsonCreator
   public PolicyMapSetDeleteCommunityLine() {
   }

   public PolicyMapSetDeleteCommunityLine(CommunityList list) {
      _list = list;
   }

   @JsonProperty(LIST_VAR)
   public CommunityList getList() {
      return _list;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.DELETE_COMMUNITY;
   }

   @JsonProperty(LIST_VAR)
   public void setList(CommunityList list) {
      _list = list;
   }

}
