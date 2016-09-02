package org.batfish.representation.juniper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.batfish.datamodel.IkeProposal;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.LineAction;
import org.batfish.representation.VendorConfiguration;

public abstract class JuniperConfiguration extends VendorConfiguration {

   private static final String DEFAULT_ROUTING_INSTANCE = "<default-routing-instance>";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final Set<Long> _allStandardCommunities;

   protected final Map<String, BaseApplication> _applications;

   protected final Map<String, CommunityList> _communityLists;

   protected boolean _defaultAddressSelection;

   protected LineAction _defaultCrossZoneAction;

   protected LineAction _defaultInboundAction;

   protected final RoutingInstance _defaultRoutingInstance;

   protected final Map<String, FirewallFilter> _filters;

   protected final Map<String, AddressBook> _globalAddressBooks;

   protected final Set<String> _ignoredPrefixLists;

   protected final Map<String, IkeGateway> _ikeGateways;

   protected final Map<String, IkePolicy> _ikePolicies;

   protected final Map<String, IkeProposal> _ikeProposals;

   protected final Map<Interface, Zone> _interfaceZones;

   protected final Map<String, IpsecPolicy> _ipsecPolicies;

   protected final Map<String, IpsecProposal> _ipsecProposals;

   protected final Map<String, IpsecVpn> _ipsecVpns;

   protected final Map<String, PolicyStatement> _policyStatements;

   protected final Map<String, PrefixList> _prefixLists;

   protected final Map<String, RouteFilter> _routeFilters;

   protected final Map<String, RoutingInstance> _routingInstances;

   protected final Map<String, Zone> _zones;

   public JuniperConfiguration() {
      _allStandardCommunities = new HashSet<>();
      _applications = new TreeMap<>();
      _communityLists = new TreeMap<>();
      _defaultCrossZoneAction = LineAction.ACCEPT;
      _defaultRoutingInstance = new RoutingInstance(DEFAULT_ROUTING_INSTANCE);
      _filters = new TreeMap<>();
      _globalAddressBooks = new TreeMap<>();
      _ignoredPrefixLists = new HashSet<>();
      _ikeGateways = new TreeMap<>();
      _ikePolicies = new TreeMap<>();
      _ikeProposals = new TreeMap<>();
      _interfaceZones = new TreeMap<>();
      _ipsecPolicies = new TreeMap<>();
      _ipsecProposals = new TreeMap<>();
      _ipsecVpns = new TreeMap<>();
      _prefixLists = new TreeMap<>();
      _policyStatements = new TreeMap<>();
      _routeFilters = new TreeMap<>();
      _routingInstances = new TreeMap<>();
      _zones = new TreeMap<>();
   }

   public Set<Long> getAllStandardCommunities() {
      return _allStandardCommunities;
   }

   public Map<String, BaseApplication> getApplications() {
      return _applications;
   }

   public final Map<String, CommunityList> getCommunityLists() {
      return _communityLists;
   }

   public LineAction getDefaultCrossZoneAction() {
      return _defaultCrossZoneAction;
   }

   public final RoutingInstance getDefaultRoutingInstance() {
      return _defaultRoutingInstance;
   }

   public final Map<String, FirewallFilter> getFirewallFilters() {
      return _filters;
   }

   public Map<String, AddressBook> getGlobalAddressBooks() {
      return _globalAddressBooks;
   }

   @Override
   public final String getHostname() {
      return _defaultRoutingInstance.getHostname();
   }

   public Set<String> getIgnoredPrefixLists() {
      return _ignoredPrefixLists;
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

   public Map<String, Zone> getZones() {
      return _zones;
   }

   public void setDefaultAddressSelection(boolean defaultAddressSelection) {
      _defaultAddressSelection = defaultAddressSelection;
   }

   public void setDefaultCrossZoneAction(LineAction defaultCrossZoneAction) {
      _defaultCrossZoneAction = defaultCrossZoneAction;
   }

   public void setDefaultInboundAction(LineAction defaultInboundAction) {
      _defaultInboundAction = defaultInboundAction;
   }

   @Override
   public final void setHostname(String hostname) {
      _defaultRoutingInstance.setHostname(hostname);
   }

}
