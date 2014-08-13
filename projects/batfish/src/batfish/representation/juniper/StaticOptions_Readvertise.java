package batfish.representation.juniper;

public class StaticOptions_Readvertise extends StaticOptions {
   
   private boolean _read;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Readvertise (boolean b) {
      _read = b;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.READVERTISE_ORNO;
	}

}
