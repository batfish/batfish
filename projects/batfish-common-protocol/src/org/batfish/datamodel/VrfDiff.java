package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VrfDiff extends ConfigDiffElement {

   private static final String AS_PATH_ACCESS_LISTS_DIFF_VAR = "asPathAccessListsDiff";

   private static final String COMMUNITY_LISTS_DIFF_VAR = "comunityListsDiff";

   private static final String INTERFACES_DIFF_VAR = "interfacesDiff";

   private static final String IP_ACCESS_LISTS_DIFF_VAR = "ipAccessListsDiff";

   private static final String ROUTE_FILTER_LISTS_DIFF_VAR = "routeFilterListsDiff";

   private static final String ROUTING_POLICIES_DIFF_VAR = "routingPoliciesDiff";

   private static final String VRFS_DIFF_VAR = "vrfsDiff";

   private AsPathAccessListsDiff _asPathAccessListsDiff;

   private BgpProcessDiff _bgpProcessDiff;

   private CommunityListsDiff _communityListsDiff;

   private InterfacesDiff _interfacesDiff;

   private IpAccessListsDiff _ipAccessListsDiff;

   private RouteFilterListsDiff _routeFilterListsDiff;

   private RoutingPoliciesDiff _routingPoliciesDiff;

   private VrfsDiff _vrfsDiff;

   @JsonCreator()
   private VrfDiff() {
   }

   public VrfDiff(Vrf before, Vrf after) {
      _bgpProcessDiff = new BgpProcessDiff(before.getBgpProcess(),
            after.getBgpProcess());
      if (_bgpProcessDiff.isEmpty()) {
         _bgpProcessDiff = null;
      }
   }

   @JsonProperty(AS_PATH_ACCESS_LISTS_DIFF_VAR)
   public AsPathAccessListsDiff getAsPathAccessListsDiff() {
      return _asPathAccessListsDiff;
   }

   @JsonProperty(COMMUNITY_LISTS_DIFF_VAR)
   public CommunityListsDiff getCommunityListsDiff() {
      return _communityListsDiff;
   }

   @JsonProperty(INTERFACES_DIFF_VAR)
   public InterfacesDiff getInterfacesDiff() {
      return _interfacesDiff;
   }

   @JsonProperty(IP_ACCESS_LISTS_DIFF_VAR)
   public IpAccessListsDiff getIpAccessListsDiff() {
      return _ipAccessListsDiff;
   }

   @JsonProperty(ROUTE_FILTER_LISTS_DIFF_VAR)
   public RouteFilterListsDiff getRouteFilterListsDiff() {
      return _routeFilterListsDiff;
   }

   @JsonProperty(ROUTING_POLICIES_DIFF_VAR)
   public RoutingPoliciesDiff getRoutingPoliciesDiff() {
      return _routingPoliciesDiff;
   }

   @JsonProperty(VRFS_DIFF_VAR)
   public VrfsDiff getVrfsDiff() {
      return _vrfsDiff;
   }

   @Override
   @JsonIgnore
   public boolean isEmpty() {
      return _asPathAccessListsDiff == null && _communityListsDiff == null
            && _interfacesDiff == null && _ipAccessListsDiff == null
            && _vrfsDiff == null && _routeFilterListsDiff == null
            && _routingPoliciesDiff == null;
   }

   @JsonProperty(AS_PATH_ACCESS_LISTS_DIFF_VAR)
   public void setAsPathAccessListsDiff(
         AsPathAccessListsDiff asPathAccessListsDiff) {
      _asPathAccessListsDiff = asPathAccessListsDiff;
   }

   @JsonProperty(COMMUNITY_LISTS_DIFF_VAR)
   public void setCommunityListsDiff(CommunityListsDiff communityListsDiff) {
      _communityListsDiff = communityListsDiff;
   }

   @JsonProperty(INTERFACES_DIFF_VAR)
   public void setInterfacesDiff(InterfacesDiff interfacesDiff) {
      _interfacesDiff = interfacesDiff;
   }

   @JsonProperty(IP_ACCESS_LISTS_DIFF_VAR)
   public void setIpAccessListsDiff(IpAccessListsDiff ipAccessListsDiff) {
      _ipAccessListsDiff = ipAccessListsDiff;
   }

   @JsonProperty(ROUTE_FILTER_LISTS_DIFF_VAR)
   public void setRouteFilterListsDiff(
         RouteFilterListsDiff routeFilterListsDiff) {
      _routeFilterListsDiff = routeFilterListsDiff;
   }

   @JsonProperty(ROUTING_POLICIES_DIFF_VAR)
   public void setRoutingPoliciesDiff(RoutingPoliciesDiff routingPoliciesDiff) {
      _routingPoliciesDiff = routingPoliciesDiff;
   }

   @JsonProperty(VRFS_DIFF_VAR)
   public void setVrfsDiff(VrfsDiff vrfsDiff) {
      _vrfsDiff = vrfsDiff;
   }

}
