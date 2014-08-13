package batfish.representation.juniper;

public class StaticOptions_Tag extends StaticOptions {
   
   private String _tag;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Tag (String i) {
      _tag = i;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.TAG;
	}

}
