package batfish.representation.juniper;

public class StaticOptions_Preference extends StaticOptions {

   private int _pref;

   public StaticOptions_Preference(int i) {
      _pref = i;
   }

   public int getPref() {
      return _pref;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.PREFERENCE;
   }

}
