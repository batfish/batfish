package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.ASPathAccessList;
import batfish.representation.juniper.CommunityMemberList;
import batfish.representation.juniper.PolicyStatement;
import batfish.representation.juniper.PrefixList;

public class PolicyOptionsStanza extends JStanza {
   
   private List <POStanza> _poStanzas;
   
   private List<CommunityMemberList> _communities;
   private List<ASPathAccessList> _asPathLists;
   private List<PolicyStatement> _policyStatements;
   private List<PrefixList> _prefixLists;

   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyOptionsStanza() {
      _poStanzas = new ArrayList<POStanza> ();
   }
   /* ----------------------------- Other Methods ---------------------------*/
   public void addPOStanza (POStanza p) {
      _poStanzas.add(p);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<CommunityMemberList> get_communities () {
      return _communities;
   }
   public List<ASPathAccessList> get_asPathLists () {
      return _asPathLists;
   }
   public List<PolicyStatement> get_policyStatements () {
      return _policyStatements;
   }
   public List<PrefixList> get_prefixLists () {
      return _prefixLists;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   public void postProcessStanza () {
      _communities = new ArrayList<CommunityMemberList>();
      _asPathLists = new ArrayList<ASPathAccessList>();
      _policyStatements = new ArrayList<PolicyStatement>();
      _prefixLists = new ArrayList<PrefixList>();
      
      for (POStanza pos: _poStanzas) {
         
         pos.postProcessStanza();
      
         if (pos.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
            switch (pos.getType()) {
               case AS_PATH:
                  PO_AsPathStanza aps = (PO_AsPathStanza) pos;
                 _asPathLists.add(aps.get_ASPathAccessList());
                 break;
               case COMMUNITY:
                  PO_CommunityStanza cps = (PO_CommunityStanza) pos;
                 _communities.add(cps.get_community());
                 break;
               case PREFIX_LIST:
                  PO_PrefixListStanza prps = (PO_PrefixListStanza) pos;
                 _prefixLists.add(prps.get_prefixList());
                 break;
               case POLICY_STATEMENT:
                  PO_PolicyStatementStanza pops = (PO_PolicyStatementStanza) pos;
                 _policyStatements.add(pops.get_policyStatement());
                 break;
                 
               case NULL:
                  break;
                 
               default:
                  throw new Error ("bad policy options stanza type");
            }
         }
         this.addIgnoredStatements(pos.get_ignoredStatements());
      }
      this.set_postProcessTitle("Policy Options");     
      
   }

   @Override
   public JStanzaType getType() {
      return JStanzaType.POLICY_OPTIONS;
   }

}
