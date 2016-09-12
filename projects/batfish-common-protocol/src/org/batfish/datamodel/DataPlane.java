package org.batfish.datamodel;

import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.FibMap;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.PolicyRouteFibNodeMap;

public interface DataPlane {

   FibMap getFibs();

   InterfaceSet getFlowSinks();

   PolicyRouteFibNodeMap getPolicyRouteFibNodeMap();

   EdgeSet getTopologyEdges();

}
