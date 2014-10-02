package batfish.grammar.juniper.bgp;

import java.util.List;

import batfish.representation.juniper.BGPImportList;

public class BGGRN_ImportStanza extends BGGR_NStanza {
   
   BGPImportList _iList;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGRN_ImportStanza(BGPImportList i) {
      _iList = i;
      //set_stanzaStatus(i.get_stanzaStatus());
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public List<String> GetImportNames () {
      return _iList.get_policyNames();
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/   
   @Override
   public BGGR_NType getType() {
      return BGGR_NType.IMPORT;
   }

}