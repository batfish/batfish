package batfish.representation.juniper;

public class PolicyStatementSetAdditiveCommunityLine extends PolicyStatementSetLine {

   private String _communities;

   public PolicyStatementSetAdditiveCommunityLine(String communities) {
      _communities = communities;
   }

   @Override
   public SetType getSetType() {
      return SetType.ADDITIVE_COMMUNITY;
   }

   public String getCommunities() {
      return _communities;
   }
   
}
