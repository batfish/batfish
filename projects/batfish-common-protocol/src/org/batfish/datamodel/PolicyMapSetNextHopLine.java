package org.batfish.datamodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetNextHopLine extends PolicyMapSetLine {

   private static final String NEXT_HOPS_VAR = "nextHops";

   private static final long serialVersionUID = 1L;

   private final List<Ip> _nextHops;

   @JsonCreator
   public PolicyMapSetNextHopLine(
         @JsonProperty(NEXT_HOPS_VAR) List<Ip> nextHops) {
      _nextHops = nextHops;
   }

   @JsonProperty(NEXT_HOPS_VAR)
   public List<Ip> getNextHops() {
      return _nextHops;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.NEXT_HOP;
   }

}
