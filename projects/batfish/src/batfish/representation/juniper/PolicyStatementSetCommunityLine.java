package batfish.representation.juniper;

public class PolicyStatementSetCommunityLine extends PolicyStatementSetLine {

   private static final long serialVersionUID = 1L;
   
   private String _community;

   public PolicyStatementSetCommunityLine(String c) {
      _community = c;
   }

   @Override
   public SetType getSetType() {
      return SetType.COMMUNITY;
   }

   public String getCommunities() {
      return _community;
   }
   
}
