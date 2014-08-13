package batfish.representation.juniper;

public class StaticOptions_Active extends StaticOptions {
   
   private boolean _active;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Active (boolean b) {
      _active = b;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.ACTIVE_PASSIVE;
	}

}
