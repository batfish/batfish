package org.batfish.datamodel;

import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;

public interface DataPlane extends Serializable {

  /** Return routes in the BGP rib for each node/VRF */
  @Nonnull
  Table<String, String, Set<Bgpv4Route>> getBgpRoutes();

  /** Return backup routes in the BGP rib for each node/VRF */
  @Nonnull
  Table<String, String, Set<Bgpv4Route>> getBgpBackupRoutes();

  /** Return routes in the EVPN RIB on each node/VRF */
  @Nonnull
  Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes();

  /** Return backup routes in the EVPN RIB on each node/VRF */
  @Nonnull
  Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnBackupRoutes();

  /** Return a {@link Fib} for each node/VRF */
  @Nonnull
  Map<String, Map<String, Fib>> getFibs();

  @Nonnull
  ForwardingAnalysis getForwardingAnalysis();

  /**
   * Return the set of all (main) RIBs. Table structure: hostname -&gt; VRF name -&gt; FinalMainRib
   */
  @Nonnull
  Table<String, String, FinalMainRib> getRibs();

  /**
   * Return the summary of route prefix propagation. Map structure: Hostname -&gt; VRF name -&gt;
   * Prefix -&gt; action taken -&gt; set of hostnames (peers).
   */
  @Nonnull
  SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary();

  /**
   * Return {@link Layer2Vni} for each node/VRF. Returned settings are based on the vni settings in
   * a {@link Vrf}, but may include additional information obtained during dataplane computation,
   * such as updated flood lists due to EVPN route exchange.
   */
  @Nonnull
  Table<String, String, Set<Layer2Vni>> getLayer2Vnis();

  /**
   * Return {@link Layer3Vni} for each node/VRF. Returned settings are based on the vni settings in
   * a {@link Vrf}, but may include additional information obtained during dataplane computation,
   * such as updated VTEPs due to EVPN route exchange.
   */
  @Nonnull
  Table<String, String, Set<Layer3Vni>> getLayer3Vnis();
}
