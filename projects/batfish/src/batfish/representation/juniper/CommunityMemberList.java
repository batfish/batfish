package batfish.representation.juniper;

import java.util.List;

public class CommunityMemberList {

   private String _name;
   private List<CommunityMemberListLine> _communityIds;
   
   /* ------------------------------ Constructor ----------------------------*/
   public CommunityMemberList(String n, List<CommunityMemberListLine> cs) {
      _name = n;
      _communityIds = cs;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<CommunityMemberListLine> get_communityIds() {
      return _communityIds;
   }
   public String get_name() {
      return _name;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
}
