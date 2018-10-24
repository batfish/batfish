package org.batfish.datamodel;

import com.google.common.collect.Table;
import com.google.common.graph.ValueGraph;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public interface DataPlane extends Serializable {

  Table<String, String, Set<BgpRoute>> getBgpRoutes(boolean multipath);

  ValueGraph<BgpPeerConfigId, BgpSessionProperties> getBgpTopology();

  Map<String, Configuration> getConfigurations();

  Map<String, Map<String, Fib>> getFibs();

  ForwardingAnalysis getForwardingAnalysis();

  /**
   * Return the map of Vrfs that own each Ip (as computed during dataplane computation). Map
   * structure: Ip -&gt; hostname -&gt; set of Vrfs
   */
  Map<Ip, Map<String, Set<String>>> getIpVrfOwners();

  /** Return the set of all (main) RIBs. Map structure: hostname -&gt; VRF name -&gt; GenericRib */
  SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> getRibs();

  /** Return the network (i.e., layer 3) topology */
  Topology getTopology();

  /** Get a set of all layer 3 edges in the network */
  SortedSet<Edge> getTopologyEdges();

  /**
   * Return the summary of route prefix propagation. Map structure: Hostname -&gt; VRF name -&gt;
   * Prefix -&gt; action taken -&gt; set of hostnames (peers).
   */
  SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary();
}
