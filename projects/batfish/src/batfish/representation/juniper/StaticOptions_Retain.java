package batfish.representation.juniper;

public class StaticOptions_Retain extends StaticOptions {
   
   private boolean _retain;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Retain (boolean b) {
      _retain = b;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.RETAIN_ORNO;
	}

}
