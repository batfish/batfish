package batfish.representation.juniper;

import java.util.List;

public class PolicyStatementMatchInterfaceListLine extends PolicyStatement_MatchLine {

   private List<String> _ifNames;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchInterfaceListLine(List<String> s) {
      _ifNames = s;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<String> get_ifNames() {
      return _ifNames;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.INTERFACE;
   }   
}
