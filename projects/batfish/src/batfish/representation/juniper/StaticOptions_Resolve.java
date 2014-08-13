package batfish.representation.juniper;

public class StaticOptions_Resolve extends StaticOptions {
   
   private boolean _resolve;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Resolve (boolean b) {
      _resolve = b;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.RESOLVE_ORNO;
	}

}
