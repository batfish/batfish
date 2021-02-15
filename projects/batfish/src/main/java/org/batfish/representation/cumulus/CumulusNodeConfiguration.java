package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.BgpProcess.BGP_UNNUMBERED_IP;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LinkLocalAddress;

/** A shared interfaces for the two Cumulus configuration types -- concatenated, nclu */
public interface CumulusNodeConfiguration {

  String LOOPBACK_INTERFACE_NAME = "lo";
  @VisibleForTesting public static final String CUMULUS_CLAG_DOMAIN_ID = "~CUMULUS_CLAG_DOMAIN~";
  public static final @Nonnull LinkLocalAddress LINK_LOCAL_ADDRESS =
      LinkLocalAddress.of(BGP_UNNUMBERED_IP);

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
  Map<String, Vxlan> getVxlans();

  /** Returns a map from interface names to clag settings, for interfaces with Clag settings */
  Map<String, InterfaceClagSettings> getClagSettings();

  OspfProcess getOspfProcess();

  /**
   * Returns the VRF for the specified access VLAN or null if the input was null or a VRF wasn't
   * configured
   */
  @Nullable
  String getVrfForVlan(@Nullable Integer bridgeAccessVlan);
}
