package batfish.representation.juniper;

public class StaticOptions_NextTable extends StaticOptions {
   
   private String _nextTable;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_NextTable (String s) {
      _nextTable = s;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public StaticOptionsType getType() {
		return StaticOptionsType.NEXT_TABLE;
	}

}
