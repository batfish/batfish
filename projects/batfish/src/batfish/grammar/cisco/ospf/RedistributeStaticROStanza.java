package batfish.grammar.cisco.ospf;

import batfish.representation.cisco.OspfProcess;

public class RedistributeStaticROStanza implements ROStanza {

//   private int _cost;
//   private String _routeMapName;
//   private boolean _subnets;

   public RedistributeStaticROStanza(int cost, String routeMapName,
         boolean subnets) {
//      _cost = cost;
//      _routeMapName = routeMapName;
//      _subnets = subnets;
   }

   @Override
   public void process(OspfProcess p) {
//      p.setRedistributeStaticMap(_routeMapName);
//      p.setRedistributeStaticSubnets(_subnets);
//      p.setRedistributeStatic(true);
//      p.setRedistributeStaticMetric(_cost);
   }

}
