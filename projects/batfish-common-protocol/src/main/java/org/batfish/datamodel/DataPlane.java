package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

public interface DataPlane extends Serializable {

  Map<String, Map<String, Fib>> getFibs();

  SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> getRibs();

  SortedSet<Edge> getTopologyEdges();
}
