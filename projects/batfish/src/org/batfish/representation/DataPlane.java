package org.batfish.representation;

import java.io.Serializable;

import org.batfish.common.collections.EdgeSet;
import org.batfish.common.collections.FibMap;
import org.batfish.common.collections.InterfaceSet;
import org.batfish.common.collections.PolicyRouteFibNodeMap;

public class DataPlane implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final FibMap _fibs;

   private final InterfaceSet _flowSinks;

   private final PolicyRouteFibNodeMap _policyRouteFibNodeMap;

   private final EdgeSet _topologyEdges;

   public DataPlane(InterfaceSet flowSinks, EdgeSet topologyEdges, FibMap fibs,
         PolicyRouteFibNodeMap policyRouteFibNodeMap) {
      _flowSinks = flowSinks;
      _topologyEdges = topologyEdges;
      _fibs = fibs;
      _policyRouteFibNodeMap = policyRouteFibNodeMap;
   }

   public FibMap getFibs() {
      return _fibs;
   }

   public InterfaceSet getFlowSinks() {
      return _flowSinks;
   }

   public PolicyRouteFibNodeMap getPolicyRouteFibNodeMap() {
      return _policyRouteFibNodeMap;
   }

   public EdgeSet getTopologyEdges() {
      return _topologyEdges;
   }

}
