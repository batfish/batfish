package batfish.representation;

import batfish.util.Util;

public class PolicyMapSetLocalPreferenceLine extends PolicyMapSetLine {

   private int _localPreference;

   public PolicyMapSetLocalPreferenceLine(int localPreference) {
      _localPreference = localPreference;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.LOCAL_PREFERENCE;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   @Override
   public boolean sameParseTree(PolicyMapSetLine line, String prefix) {
      boolean res = (line.getType() == PolicyMapSetType.LOCAL_PREFERENCE);
      if(res == false){
         System.out.println("PoliMapSetLocPrefLine:Type "+prefix);
         return res;
      }
      
      PolicyMapSetLocalPreferenceLine locLine = (PolicyMapSetLocalPreferenceLine) line;
         
      res = (_localPreference == locLine._localPreference);
      
      if(res == false){
         System.out.println("PoliMapSetLocPrefLine "+prefix);
        
      }
      return res;
      
   }
   
   @Override
   public String getIFString(int indentLevel) {
	   return Util.getIndentString(indentLevel) + "LocalPreference " + _localPreference;
   }
   
}
