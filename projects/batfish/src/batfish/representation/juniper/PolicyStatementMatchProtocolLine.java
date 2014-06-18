package batfish.representation.juniper;

import java.util.List;

import batfish.representation.juniper.MatchType;
import batfish.representation.juniper.PolicyStatementMatchLine;

public class PolicyStatementMatchProtocolLine extends PolicyStatementMatchLine {

   private List<String> _protocol;

   public PolicyStatementMatchProtocolLine(List<String> protocol) {
      _protocol = protocol;
   }

   @Override
   public MatchType getType() {
      return MatchType.PROTOCOL;
   }

   public List<String> getProtocl() {
      return _protocol;
   }
   
}
