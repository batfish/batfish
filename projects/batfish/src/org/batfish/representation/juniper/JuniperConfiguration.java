package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class JuniperConfiguration implements Serializable {

   private static final String DEFAULT_ROUTING_INSTANCE = "<default-routing-instance>";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final Map<String, CommunityList> _communityLists;

   protected final RoutingInstance _defaultRoutingInstance;

   protected final Map<String, FirewallFilter> _filters;

   protected final Map<String, PolicyStatement> _policyStatements;

   protected final Map<String, PrefixList> _prefixLists;

   protected final Map<String, RouteFilter> _routeFilters;

   protected final Map<String, RoutingInstance> _routingInstances;

   public JuniperConfiguration() {
      _communityLists = new TreeMap<String, CommunityList>();
      _defaultRoutingInstance = new RoutingInstance(DEFAULT_ROUTING_INSTANCE);
      _filters = new TreeMap<String, FirewallFilter>();
      _prefixLists = new TreeMap<String, PrefixList>();
      _policyStatements = new TreeMap<String, PolicyStatement>();
      _routeFilters = new TreeMap<String, RouteFilter>();
      _routingInstances = new TreeMap<String, RoutingInstance>();
   }

   public Map<String, CommunityList> getCommunityLists() {
      return _communityLists;
   }

   public final RoutingInstance getDefaultRoutingInstance() {
      return _defaultRoutingInstance;
   }

   public final Map<String, FirewallFilter> getFirewallFilters() {
      return _filters;
   }

   public String getHostname() {
      return _defaultRoutingInstance.getHostname();
   }

   public final Map<String, PolicyStatement> getPolicyStatements() {
      return _policyStatements;
   }

   public final Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   public Map<String, RouteFilter> getRouteFilters() {
      return _routeFilters;
   }

   public final Map<String, RoutingInstance> getRoutingInstances() {
      return _routingInstances;
   }

}
