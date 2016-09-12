package org.batfish.nls;

import java.io.Serializable;

import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.FibMap;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.PolicyRouteFibNodeMap;

public class NlsDataPlane implements DataPlane, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final FibMap _fibs;

   private final InterfaceSet _flowSinks;

   private final PolicyRouteFibNodeMap _policyRouteFibNodeMap;

   private final EdgeSet _topologyEdges;

   public NlsDataPlane(InterfaceSet flowSinks, EdgeSet topologyEdges,
         FibMap fibs, PolicyRouteFibNodeMap policyRouteFibNodeMap) {
      _flowSinks = flowSinks;
      _topologyEdges = topologyEdges;
      _fibs = fibs;
      _policyRouteFibNodeMap = policyRouteFibNodeMap;
   }

   @Override
   public FibMap getFibs() {
      return _fibs;
   }

   @Override
   public InterfaceSet getFlowSinks() {
      return _flowSinks;
   }

   @Override
   public PolicyRouteFibNodeMap getPolicyRouteFibNodeMap() {
      return _policyRouteFibNodeMap;
   }

   @Override
   public EdgeSet getTopologyEdges() {
      return _topologyEdges;
   }

}
