package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.representation.IkeProposal;
import org.batfish.representation.IpsecProposal;

public class JuniperConfiguration implements Serializable {

   private static final String DEFAULT_ROUTING_INSTANCE = "<default-routing-instance>";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final Map<String, CommunityList> _communityLists;

   protected final RoutingInstance _defaultRoutingInstance;

   protected final Map<String, FirewallFilter> _filters;

   protected final Map<String, IkeGateway> _ikeGateways;

   protected final Map<String, IkePolicy> _ikePolicies;

   protected final Map<String, IkeProposal> _ikeProposals;

   protected final Map<String, IpsecPolicy> _ipsecPolicies;

   protected final Map<String, IpsecProposal> _ipsecProposals;

   protected final Map<String, IpsecVpn> _ipsecVpns;

   protected final Map<String, PolicyStatement> _policyStatements;

   protected final Map<String, PrefixList> _prefixLists;

   protected final Map<String, RouteFilter> _routeFilters;

   protected final Map<String, RoutingInstance> _routingInstances;

   public JuniperConfiguration() {
      _communityLists = new TreeMap<String, CommunityList>();
      _defaultRoutingInstance = new RoutingInstance(DEFAULT_ROUTING_INSTANCE);
      _filters = new TreeMap<String, FirewallFilter>();
      _ikeGateways = new TreeMap<String, IkeGateway>();
      _ikePolicies = new TreeMap<String, IkePolicy>();
      _ikeProposals = new TreeMap<String, IkeProposal>();
      _ipsecPolicies = new TreeMap<String, IpsecPolicy>();
      _ipsecProposals = new TreeMap<String, IpsecProposal>();
      _ipsecVpns = new TreeMap<String, IpsecVpn>();
      _prefixLists = new TreeMap<String, PrefixList>();
      _policyStatements = new TreeMap<String, PolicyStatement>();
      _routeFilters = new TreeMap<String, RouteFilter>();
      _routingInstances = new TreeMap<String, RoutingInstance>();
   }

   public final Map<String, CommunityList> getCommunityLists() {
      return _communityLists;
   }

   public final RoutingInstance getDefaultRoutingInstance() {
      return _defaultRoutingInstance;
   }

   public final Map<String, FirewallFilter> getFirewallFilters() {
      return _filters;
   }

   public final String getHostname() {
      return _defaultRoutingInstance.getHostname();
   }

   public final Map<String, IkeGateway> getIkeGateways() {
      return _ikeGateways;
   }

   public final Map<String, IkePolicy> getIkePolicies() {
      return _ikePolicies;
   }

   public final Map<String, IkeProposal> getIkeProposals() {
      return _ikeProposals;
   }

   public final Map<String, IpsecPolicy> getIpsecPolicies() {
      return _ipsecPolicies;
   }

   public final Map<String, IpsecProposal> getIpsecProposals() {
      return _ipsecProposals;
   }

   public final Map<String, IpsecVpn> getIpsecVpns() {
      return _ipsecVpns;
   }

   public final Map<String, PolicyStatement> getPolicyStatements() {
      return _policyStatements;
   }

   public final Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   public final Map<String, RouteFilter> getRouteFilters() {
      return _routeFilters;
   }

   public final Map<String, RoutingInstance> getRoutingInstances() {
      return _routingInstances;
   }

   public final void setHostname(String hostname) {
      _defaultRoutingInstance.setHostname(hostname);
   }

}
