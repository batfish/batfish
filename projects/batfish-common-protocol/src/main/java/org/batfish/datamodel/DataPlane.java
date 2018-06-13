package org.batfish.datamodel;

import com.google.common.graph.Network;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public interface DataPlane extends Serializable {

  Network<BgpNeighbor, BgpSession> getBgpTopology();

  Map<String, Configuration> getConfigurations();

  Map<String, Map<String, Fib>> getFibs();

  ForwardingAnalysis getForwardingAnalysis();

  /**
   * Return the map of Ip owners (as computed during dataplane computation). Map structure: Ip ->
   * Set of hostnames
   */
  Map<Ip, Set<String>> getIpOwners();

  /**
   * Return the map of Vrfs that own each Ip (as computed during dataplane computation). Map
   * structure: Ip -> hostname -> set of Vrfs
   */
  Map<Ip, Map<String, Set<String>>> getIpVrfOwners();

  /** Return the set of all (main) RIBs. Map structure: hostname -> VRF name -> GenericRib */
  SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> getRibs();

  Topology getTopology();

  SortedSet<Edge> getTopologyEdges();

  SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary();
}
