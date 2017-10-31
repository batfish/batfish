package org.batfish.datamodel;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.collections.FibMap;
import org.batfish.datamodel.collections.InterfaceSet;
import org.batfish.datamodel.collections.PolicyRouteFibNodeMap;

public interface DataPlane extends Serializable {

  FibMap getFibs();

  InterfaceSet getFlowSinks();

  PolicyRouteFibNodeMap getPolicyRouteFibNodeMap();

  SortedMap<String, SortedMap<String, IRib<AbstractRoute>>> getRibs();

  SortedSet<Edge> getTopologyEdges();
}
