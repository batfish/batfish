package batfish.representation.juniper;

public class StaticOptions_Discard extends StaticOptions {
   
   private boolean _discard;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Discard (boolean b) {
      _discard = b;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.DISCARD;
	}

}
