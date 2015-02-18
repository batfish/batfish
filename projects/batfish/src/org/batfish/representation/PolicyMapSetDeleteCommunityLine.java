package org.batfish.representation;

public class PolicyMapSetDeleteCommunityLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private CommunityList _list;

   public PolicyMapSetDeleteCommunityLine(CommunityList list) {
      _list = list;
   }

   public CommunityList getList() {
      return _list;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.DELETE_COMMUNITY;
   }

}
