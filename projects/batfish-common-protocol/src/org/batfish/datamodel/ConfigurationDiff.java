package org.batfish.datamodel;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationDiff implements AnswerElement {

   private static final String AS_PATH_ACCESS_LISTS_DIFF_VAR = "asPathAccessListsDiff";
   private static final String COMMUNITY_LISTS_DIFF_VAR = "comunityListsDiff";
   private static final String INTERFACE_LISTS_DIFF_VAR = "interfaceListstDiff";
   private static final String IP_ACCESS_LISTS_DSIFF_VAR = "ipAccessListstDiff";
   private static final String NEIGHBOR_LISTS_DIFF_VAR = "neighborListstDiff";
   private static final String ROUTE_FILTER_LISTS_DIFF_VAR = "routeFilterListsDiff";
   private static final String ROUTING_POLICY_LISTS_DIFF_VAR = "routingPolicyListsDiff";

   private AsPathAccessListsDiff _asPathAccessListsDiff;
   private CommunityListsDiff _communityListsDiff;
   private InterfaceListsDiff _interfaceListsDiff;
   private IpAccessListsDiff _ipAccessListsDiff;
   private NeighborListsDiff _neighborListsDiff;
   private RouteFilterListsDiff _routeFilterListsDiff;
   private RoutingPolicyListsDiff _routingPolicyListsDiff;

   @JsonCreator()
   public ConfigurationDiff() {

   }

   public ConfigurationDiff(Configuration a, Configuration b) {
      _asPathAccessListsDiff = new AsPathAccessListsDiff(
            a.getAsPathAccessLists(), b.getAsPathAccessLists());
      _communityListsDiff = new CommunityListsDiff(a.getCommunityLists(),
            b.getCommunityLists());
      _interfaceListsDiff = new InterfaceListsDiff(a.getInterfaces(),
            b.getDefaultVrf().getInterfaces());
      _ipAccessListsDiff = new IpAccessListsDiff(a.getIpAccessLists(),
            b.getIpAccessLists());
      _neighborListsDiff = new NeighborListsDiff(
            a.getDefaultVrf().getBgpProcess().getNeighbors(),
            b.getDefaultVrf().getBgpProcess().getNeighbors());
      _routeFilterListsDiff = new RouteFilterListsDiff(a.getRouteFilterLists(),
            b.getRouteFilterLists());
      _routingPolicyListsDiff = new RoutingPolicyListsDiff(
            a.getRoutingPolicies(), b.getRoutingPolicies());
   }

   @JsonProperty(AS_PATH_ACCESS_LISTS_DIFF_VAR)
   public AsPathAccessListsDiff getAsPathAccessListDiff() {
      return _asPathAccessListsDiff;
   }

   @JsonProperty(COMMUNITY_LISTS_DIFF_VAR)
   public CommunityListsDiff getCommunityListDiff() {
      return _communityListsDiff;
   }

   @JsonProperty(INTERFACE_LISTS_DIFF_VAR)
   public InterfaceListsDiff getInterfaceListsDiff() {
      return _interfaceListsDiff;
   }

   @JsonProperty(IP_ACCESS_LISTS_DSIFF_VAR)
   public IpAccessListsDiff getIpAccessListListDiff() {
      return _ipAccessListsDiff;
   }

   @JsonProperty(NEIGHBOR_LISTS_DIFF_VAR)
   public NeighborListsDiff getNeighborListsDiff() {
      return _neighborListsDiff;
   }

   @JsonProperty(ROUTE_FILTER_LISTS_DIFF_VAR)
   public RouteFilterListsDiff getRouteFilterListsDiff() {
      return _routeFilterListsDiff;
   }

   @JsonProperty(ROUTING_POLICY_LISTS_DIFF_VAR)
   public RoutingPolicyListsDiff getRoutingPolicyListsDiff() {
      return _routingPolicyListsDiff;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setAsPathAccessListDiff(AsPathAccessListsDiff d) {
      _asPathAccessListsDiff = d;
   }

   public void setCommunityListDiff(CommunityListsDiff _communityListsDiff) {
      this._communityListsDiff = _communityListsDiff;
   }

   public void setInterfaceListsDiff(InterfaceListsDiff interfaceListsDiff) {
      _interfaceListsDiff = interfaceListsDiff;
   }

   public void setIpAccessListListDiff(IpAccessListsDiff _ipAccessListsDiff) {
      this._ipAccessListsDiff = _ipAccessListsDiff;
   }

   public void setNeighborListsDiff(NeighborListsDiff neighborListsDiff) {
      _neighborListsDiff = neighborListsDiff;
   }

   public void setRouteFilterListsDiff(
         RouteFilterListsDiff routeFilterListsDiff) {
      _routeFilterListsDiff = routeFilterListsDiff;
   }

   public void setRoutingPolicyListsDiff(
         RoutingPolicyListsDiff routingPolicyListsDiff) {
      _routingPolicyListsDiff = routingPolicyListsDiff;
   }
}
