package batfish.grammar.juniper.interfaces;

public class IFU_DisableStanza extends IF_UStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFU_DisableStanza () {
      set_postProcessTitle("Disable Unit");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public IF_UType getType() {
		return IF_UType.DISABLE;
	}

}
