package batfish.representation;

import java.util.List;

import batfish.representation.PolicyMapMatchType;
import batfish.representation.PolicyMapMatchLine;

public class PolicyMapMatchProtocolLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private List<RoutingProtocol> _protocol;

   public PolicyMapMatchProtocolLine(List<RoutingProtocol> protocols) {
      _protocol = protocols;
   }

   public List<RoutingProtocol> getProtocols() {
      return _protocol;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.PROTOCOL;
   }

}
