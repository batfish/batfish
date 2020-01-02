package org.batfish.representation.cumulus;

import java.util.Map;

/** A shared interfaces for the two Cumulus configuration types -- concatenated, frr */
public interface CumulusNodeConfiguration {

  public static final String LOOPBACK_INTERFACE_NAME = "lo";

  Map<String, IpCommunityList> getIpCommunityLists();

  Map<String, IpPrefixList> getIpPrefixLists();

  Map<String, RouteMap> getRouteMaps();

  BgpProcess getBgpProcess();

  Map<String, Interface> getInterfaces();

  Loopback getLoopback();

  Map<String, Vrf> getVrfs();

  Map<String, Vxlan> getVxlans();

  OspfProcess getOspfProcess();
}
