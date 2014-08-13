package batfish.representation.juniper;

public class StaticOptions_Preference extends StaticOptions {
   
   private int _pref;
   
   /* ------------------------------ Constructor ----------------------------*/
   public StaticOptions_Preference (int i) {
      _pref = i;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.PREFERENCE;
   }

}
