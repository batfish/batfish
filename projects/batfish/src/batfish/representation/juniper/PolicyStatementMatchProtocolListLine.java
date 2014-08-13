package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

import static batfish.representation.juniper.ProtocolOps.*;

public class PolicyStatementMatchProtocolListLine extends PolicyStatement_MatchLine {

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
