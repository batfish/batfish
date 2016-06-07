package org.batfish.datamodel;

import org.batfish.datamodel.PolicyMapMatchLine;
import org.batfish.datamodel.PolicyMapMatchType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchProtocolLine extends PolicyMapMatchLine {

   private static final String PROTOCOL_VAR = "protocol";

   private static final long serialVersionUID = 1L;

   private final RoutingProtocol _protocol;

   @JsonCreator
   public PolicyMapMatchProtocolLine(
         @JsonProperty(PROTOCOL_VAR) RoutingProtocol protocol) {
      _protocol = protocol;
   }

   @JsonProperty(PROTOCOL_VAR)
   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.PROTOCOL;
   }

}
