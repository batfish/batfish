package org.batfish.datamodel;

import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public interface DataPlane extends Serializable {

  Table<String, String, Set<Bgpv4Route>> getBgpRoutes(boolean multipath);

  Map<String, Configuration> getConfigurations();

  Map<String, Map<String, Fib>> getFibs();

  ForwardingAnalysis getForwardingAnalysis();

  /**
   * Return the map of Vrfs that own each Ip (as computed during dataplane computation). Map
   * structure: Ip -&gt; hostname -&gt; set of Vrfs
   */
  Map<Ip, Map<String, Set<String>>> getIpVrfOwners();

  /** Return the set of all (main) RIBs. Map structure: hostname -&gt; VRF name -&gt; GenericRib */
  SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> getRibs();

  /**
   * Return the summary of route prefix propagation. Map structure: Hostname -&gt; VRF name -&gt;
   * Prefix -&gt; action taken -&gt; set of hostnames (peers).
   */
  SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary();
}
