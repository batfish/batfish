package org.batfish.datamodel;

import org.batfish.datamodel.PolicyMapMatchLine;
import org.batfish.datamodel.PolicyMapMatchType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchNeighborLine extends PolicyMapMatchLine {

   private static final String NEIGHBOR_IP_VAR = "neighborIp";

   private static final long serialVersionUID = 1L;

   private final Ip _neighborIp;

   @JsonCreator
   public PolicyMapMatchNeighborLine(
         @JsonProperty(NEIGHBOR_IP_VAR) Ip neighborIP) {
      _neighborIp = neighborIP;
   }

   @JsonProperty(NEIGHBOR_IP_VAR)
   public Ip getNeighborIp() {
      return _neighborIp;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.NEIGHBOR;
   }

}
