package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.CommunityMemberList;
import batfish.representation.juniper.CommunityMemberListLine;

public class PO_CommunityStanza extends POStanza {
   
   private String _name;
   private List<String> _communityIds;
   private CommunityMemberList _community;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PO_CommunityStanza(String n) {
      _name = n;
      _communityIds = new ArrayList<String> ();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addCommunityId(String c) {
      _communityIds.add(c);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public CommunityMemberList get_community () {
      return _community;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   public void postProcessStanza () {
      List<CommunityMemberListLine> cls = new ArrayList<CommunityMemberListLine>();
      for (String c : _communityIds) {
         cls.add(new CommunityMemberListLine(c));
      }
      _community = new CommunityMemberList(_name, cls);
   }
   
   @Override
   public POType getType() {
      return POType.COMMUNITY;
   }

} 