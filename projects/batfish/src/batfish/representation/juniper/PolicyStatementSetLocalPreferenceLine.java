package batfish.representation.juniper;

public class PolicyStatementSetLocalPreferenceLine extends PolicyStatementSetLine {

   private static final long serialVersionUID = 1L;
   
   private int _localPreference;

   public PolicyStatementSetLocalPreferenceLine(int localPreference) {
      _localPreference = localPreference;
   }

   @Override
   public SetType getSetType() {
      return SetType.LOCAL_PREFERENCE;
   }

   public int getLocalPreference() {
      return _localPreference;
   }
   
}
