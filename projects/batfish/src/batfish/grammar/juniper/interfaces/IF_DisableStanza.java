package batfish.grammar.juniper.interfaces;

public class IF_DisableStanza extends IFStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public IF_DisableStanza () {
      set_postProcessTitle("Disable Interface");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public IFType getType() {
		return IFType.DISABLE;
	}

}
