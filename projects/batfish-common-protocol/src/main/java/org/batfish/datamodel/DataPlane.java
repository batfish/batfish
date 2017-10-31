package org.batfish.datamodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.collections.FibMap;
import org.batfish.datamodel.collections.InterfaceSet;

public interface DataPlane extends Serializable {

  FibMap getFibs();

  InterfaceSet getFlowSinks();

  SortedMap<String, HashMap<Ip, SortedSet<Edge>>> getPolicyRouteFibNodeMap();

  SortedMap<String, SortedMap<String, IRib<AbstractRoute>>> getRibs();

  SortedSet<Edge> getTopologyEdges();
}
