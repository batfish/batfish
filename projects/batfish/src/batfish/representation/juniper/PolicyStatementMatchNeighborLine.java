package batfish.representation.juniper;

import batfish.representation.juniper.PolicyStatement_MatchLine;

public class PolicyStatementMatchNeighborLine extends PolicyStatement_MatchLine {

   private String _neighborIp;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchNeighborLine(String neighborIP) {
      _neighborIp = neighborIP;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_neighborIp() {
      return _neighborIp;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.NEIGHBOR;
   }

}
