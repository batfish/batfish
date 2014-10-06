package batfish.grammar.juniper.bgp;

import batfish.representation.juniper.BGPFamily;

public class BGGR_FamilyStanza extends BG_GRStanza {
   
   BGPFamily _fam;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_FamilyStanza (BGPFamily b) {
      _fam = b;
    //  set_stanzaStatus(b.get_stanzaStatus());
      set_postProcessTitle("BGP Group Family");
   }

   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
	@Override
	public BG_GRType getType() {
		return BG_GRType.FAMILY;
	}
}
