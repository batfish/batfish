package batfish.grammar.juniper.bgp;

import batfish.representation.juniper.BGPFamily;

public class BGGRN_FamilyStanza extends BGGR_NStanza {
   
   BGPFamily _fam;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGRN_FamilyStanza (BGPFamily b) {
      _fam = b;
     // set_stanzaStatus(b.get_stanzaStatus());
   }

   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/   
	@Override
	public BGGR_NType getType() {
		return BGGR_NType.FAMILY;
	}
}
