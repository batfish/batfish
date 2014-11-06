package batfish.grammar.juniper.interfaces;

public class IF_EnableStanza extends IFStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public IF_EnableStanza () {
      set_postProcessTitle("Enable Interface");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public IFType getType() {
		return IFType.ENABLE;
	}

}
