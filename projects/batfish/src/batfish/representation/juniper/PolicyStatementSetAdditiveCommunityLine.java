package batfish.representation.juniper;

public class PolicyStatementSetAdditiveCommunityLine extends PolicyStatementSetLine {

   private static final long serialVersionUID = 1L;
   
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
