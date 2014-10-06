package batfish.representation.juniper;

import java.util.List;

public class PolicyStatementMatchProtocolListLine extends PolicyStatement_MatchLine {

   private static final long serialVersionUID = 1L;
   

   private List<ProtocolType> _protocols;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PolicyStatementMatchProtocolListLine(List<ProtocolType> ps) {
      _protocols = ps;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<ProtocolType> get_protocls() {
      return _protocols;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public PolicyStatement_MatchType getType() {
      return PolicyStatement_MatchType.PROTOCOL;
   }


   
}
