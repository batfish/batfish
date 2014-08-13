package batfish.representation.juniper;

public class StaticOptions_Metric extends StaticOptions {
   
   private int _metric;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Metric (int i) {
      _metric = i;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.METRIC;
	}

}
