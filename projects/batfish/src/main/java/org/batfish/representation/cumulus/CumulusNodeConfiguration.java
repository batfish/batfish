package org.batfish.representation.cumulus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A shared interfaces for the two Cumulus configuration types -- concatenated, nclu */
public interface CumulusNodeConfiguration {

  String LOOPBACK_INTERFACE_NAME = "lo";

  Map<String, IpCommunityList> getIpCommunityLists();

  Map<String, IpPrefixList> getIpPrefixLists();

  Map<String, RouteMap> getRouteMaps();

  BgpProcess getBgpProcess();

  /**
   * Returns the {@link OspfInterface} of the specified interface.
   *
   * <p>Returns Optional.empty if the interface does not exist or its OspfInterface is null.
   */
  Optional<OspfInterface> getOspfInterface(String ifaceName);

  /** Returns the vrf with the asked name. Returns null if the vrf does not exist */
  @Nullable
  Vrf getVrf(String vrfName);

  /** Returns all the vxlan Ids for this node */
  @Nonnull
  List<Integer> getVxlanIds();

  /** Returns a map from interface names to clag settings, for interfaces with Clag settings */
  Map<String, InterfaceClagSettings> getClagSettings();

  OspfProcess getOspfProcess();
}
