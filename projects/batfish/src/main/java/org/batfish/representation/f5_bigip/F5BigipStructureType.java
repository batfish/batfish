package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.vendor.StructureType;

/** Named structure-types for F5 BIG-IP device */
@ParametersAreNonnullByDefault
public enum F5BigipStructureType implements StructureType {
  ACCESS_LIST("access-list"),
  BGP_NEIGHBOR("bgp neighbor"),
  BGP_PROCESS("bgp process"),
  INTERFACE("interface"),
  MONITOR("monitor"),
  MONITOR_HTTP("monitor http"),
  MONITOR_HTTPS("monitor https"),
  NODE("node"),
  PEER_GROUP("peer-group"),
  PERSISTENCE("persistence"),
  PERSISTENCE_SOURCE_ADDR("persistence source-addr"),
  PERSISTENCE_SSL("persistence ssl"),
  POOL("pool"),
  PREFIX_LIST("prefix-list"),
  PROFILE("profile"),
  PROFILE_CLIENT_SSL("profile client-ssl"),
  PROFILE_HTTP("profile http"),
  PROFILE_OCSP_STAPLING_PARAMS("profile ocsp-stapling-params"),
  PROFILE_ONE_CONNECT("profile one-connect"),
  PROFILE_SERVER_SSL("profile server-ssl"),
  PROFILE_TCP("profile tcp"),
  ROUTE("route"),
  ROUTE_MAP("route-map"),
  RULE("rule"),
  SELF("self"),
  SNAT("snat"),
  SNAT_TRANSLATION("snat-translation"),
  SNATPOOL("snatpool"),
  TRUNK("trunk"),
  VIRTUAL("virtual"),
  VIRTUAL_ADDRESS("virtual-address"),
  VLAN("vlan"),
  VLAN_MEMBER_INTERFACE("vlan member interface");

  private final String _description;

  F5BigipStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
