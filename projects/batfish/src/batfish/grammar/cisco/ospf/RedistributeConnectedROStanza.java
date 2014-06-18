package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public class RedistributeConnectedROStanza implements ROStanza {

   private boolean _subnets;
   private Integer _metric;

   public RedistributeConnectedROStanza(Integer metric, boolean subnets) {
      _metric = metric;
      _subnets = true;
   }

   @Override
   public void process(OspfProcess p) {
      p.setRedistributeConnected(true);
      p.setRedistributeConnectedSubnets(_subnets);
      if (_metric != null) {
         p.setRedistributeConnectedMetric(_metric);
      }
   }

}
