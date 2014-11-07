package batfish.grammar.juniper.interfaces;

public class IFU_EnableStanza extends IF_UStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFU_EnableStanza () {
      set_postProcessTitle("Enable Interface");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public IF_UType getType() {
		return IF_UType.ENABLE;
	}

}
