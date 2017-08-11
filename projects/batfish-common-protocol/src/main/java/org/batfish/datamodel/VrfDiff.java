package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VrfDiff extends ConfigDiffElement {

  private static final String PROP_AS_PATH_ACCESS_LISTS_DIFF = "asPathAccessListsDiff";

  private static final String PROP_COMMUNITY_LISTS_DIFF = "comunityListsDiff";

  private static final String PROP_INTERFACES_DIFF = "interfacesDiff";

  private static final String PROP_IP_ACCESS_LISTS_DIFF = "ipAccessListsDiff";

  private static final String PROP_ROUTE_FILTER_LISTS_DIFF = "routeFilterListsDiff";

  private static final String PROP_ROUTING_POLICIES_DIFF = "routingPoliciesDiff";

  private static final String PROP_VRFS_DIFF = "vrfsDiff";

  private AsPathAccessListsDiff _asPathAccessListsDiff;

  private BgpProcessDiff _bgpProcessDiff;

  private CommunityListsDiff _communityListsDiff;

  private InterfacesDiff _interfacesDiff;

  private IpAccessListsDiff _ipAccessListsDiff;

  private RouteFilterListsDiff _routeFilterListsDiff;

  private RoutingPoliciesDiff _routingPoliciesDiff;

  private VrfsDiff _vrfsDiff;

  @JsonCreator()
  private VrfDiff() {}

  public VrfDiff(Vrf before, Vrf after) {
    _bgpProcessDiff = new BgpProcessDiff(before.getBgpProcess(), after.getBgpProcess());
    if (_bgpProcessDiff.isEmpty()) {
      _bgpProcessDiff = null;
    }
  }

  @JsonProperty(PROP_AS_PATH_ACCESS_LISTS_DIFF)
  public AsPathAccessListsDiff getAsPathAccessListsDiff() {
    return _asPathAccessListsDiff;
  }

  @JsonProperty(PROP_COMMUNITY_LISTS_DIFF)
  public CommunityListsDiff getCommunityListsDiff() {
    return _communityListsDiff;
  }

  @JsonProperty(PROP_INTERFACES_DIFF)
  public InterfacesDiff getInterfacesDiff() {
    return _interfacesDiff;
  }

  @JsonProperty(PROP_IP_ACCESS_LISTS_DIFF)
  public IpAccessListsDiff getIpAccessListsDiff() {
    return _ipAccessListsDiff;
  }

  @JsonProperty(PROP_ROUTE_FILTER_LISTS_DIFF)
  public RouteFilterListsDiff getRouteFilterListsDiff() {
    return _routeFilterListsDiff;
  }

  @JsonProperty(PROP_ROUTING_POLICIES_DIFF)
  public RoutingPoliciesDiff getRoutingPoliciesDiff() {
    return _routingPoliciesDiff;
  }

  @JsonProperty(PROP_VRFS_DIFF)
  public VrfsDiff getVrfsDiff() {
    return _vrfsDiff;
  }

  @Override
  @JsonIgnore
  public boolean isEmpty() {
    return _asPathAccessListsDiff == null
        && _communityListsDiff == null
        && _interfacesDiff == null
        && _ipAccessListsDiff == null
        && _vrfsDiff == null
        && _routeFilterListsDiff == null
        && _routingPoliciesDiff == null;
  }

  @JsonProperty(PROP_AS_PATH_ACCESS_LISTS_DIFF)
  public void setAsPathAccessListsDiff(AsPathAccessListsDiff asPathAccessListsDiff) {
    _asPathAccessListsDiff = asPathAccessListsDiff;
  }

  @JsonProperty(PROP_COMMUNITY_LISTS_DIFF)
  public void setCommunityListsDiff(CommunityListsDiff communityListsDiff) {
    _communityListsDiff = communityListsDiff;
  }

  @JsonProperty(PROP_INTERFACES_DIFF)
  public void setInterfacesDiff(InterfacesDiff interfacesDiff) {
    _interfacesDiff = interfacesDiff;
  }

  @JsonProperty(PROP_IP_ACCESS_LISTS_DIFF)
  public void setIpAccessListsDiff(IpAccessListsDiff ipAccessListsDiff) {
    _ipAccessListsDiff = ipAccessListsDiff;
  }

  @JsonProperty(PROP_ROUTE_FILTER_LISTS_DIFF)
  public void setRouteFilterListsDiff(RouteFilterListsDiff routeFilterListsDiff) {
    _routeFilterListsDiff = routeFilterListsDiff;
  }

  @JsonProperty(PROP_ROUTING_POLICIES_DIFF)
  public void setRoutingPoliciesDiff(RoutingPoliciesDiff routingPoliciesDiff) {
    _routingPoliciesDiff = routingPoliciesDiff;
  }

  @JsonProperty(PROP_VRFS_DIFF)
  public void setVrfsDiff(VrfsDiff vrfsDiff) {
    _vrfsDiff = vrfsDiff;
  }
}
