package batfish.grammar.juniper.bgp;

public class BGGR_LocalAsStanza extends BG_GRStanza {
   
	private int _localASNum;
	
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_LocalAsStanza(int a) {
      _localASNum = a;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/

   /* ---------------------------- Getters/Setters --------------------------*/
   public int get_localASNum () {
      return _localASNum;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/ 
	@Override
	public BG_GRType getType() {
		return BG_GRType.LOCAL_AS;
	}

}
