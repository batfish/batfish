package batfish.grammar.juniper.bgp;

public class BGGR_TypeStanza extends BG_GRStanza {
   
	private boolean _isExternal;
	
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_TypeStanza(boolean e) {
      _isExternal = e;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public boolean get_isExternal () {
      return _isExternal;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
	@Override
	public BG_GRType getType() {
		return BG_GRType.TYPE;
	}

}
