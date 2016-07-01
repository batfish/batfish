package org.batfish.datamodel.routing_policy.statement;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.Ip;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetNextHopIp extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private List<Ip> _nextHopIps;

   @JsonCreator
   public SetNextHopIp() {
      _nextHopIps = new ArrayList<Ip>();
   }

   public SetNextHopIp(List<Ip> nextHopIps) {
      _nextHopIps = nextHopIps;
   }

   public List<Ip> getNextHopIps() {
      return _nextHopIps;
   }

   public void setNextHopIps(List<Ip> nextHopIps) {
      _nextHopIps = nextHopIps;
   }

}
