package batfish.representation.juniper;

public class StaticOptions_AsPath extends StaticOptions {
   
   private String _asPath;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_AsPath (String a) {
      _asPath = a;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.ASPATH;
	}

}
