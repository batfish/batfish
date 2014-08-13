package batfish.representation.juniper;

public class CommunityMemberListLine {

   private int _asNum;
   private int _commVal;
   
   /* ------------------------------ Constructor ----------------------------*/
   public CommunityMemberListLine(String commid) {
      String[] split_comm = commid.split("/");
      _asNum = Integer.parseInt(split_comm[0]);
      _commVal = Integer.parseInt(split_comm[1]);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_asNum() {
      return _asNum;
   }
   public int get_commVal() {
      return _commVal;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
}
