package org.batfish.datamodel;

import java.io.Serializable;

import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.FibMap;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.PolicyRouteFibNodeMap;

public interface DataPlane extends Serializable {

   FibMap getFibs();

   InterfaceSet getFlowSinks();

   PolicyRouteFibNodeMap getPolicyRouteFibNodeMap();

   EdgeSet getTopologyEdges();

}
