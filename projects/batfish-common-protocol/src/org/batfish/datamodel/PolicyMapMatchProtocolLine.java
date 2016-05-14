package org.batfish.datamodel;

import org.batfish.datamodel.PolicyMapMatchLine;
import org.batfish.datamodel.PolicyMapMatchType;

public class PolicyMapMatchProtocolLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private RoutingProtocol _protocol;

   public PolicyMapMatchProtocolLine(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.PROTOCOL;
   }

}
