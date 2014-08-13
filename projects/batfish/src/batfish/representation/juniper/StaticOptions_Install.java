package batfish.representation.juniper;

public class StaticOptions_Install extends StaticOptions {
   
   private boolean _install;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Install (boolean b) {
      _install = b;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.INSTALL_ORNO;
	}

}
