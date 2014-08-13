package batfish.grammar.juniper.bgp;

import batfish.representation.juniper.BGPFamily;

public class BG_FamilyStanza extends BGStanza {
   
   private BGPFamily _fam;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BG_FamilyStanza (BGPFamily b) {
      _fam = b;
   }

   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/  
	@Override
	public BGType getType() {
		return BGType.FAMILY;
	}
}
