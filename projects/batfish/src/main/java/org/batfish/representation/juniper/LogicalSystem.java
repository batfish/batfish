package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.vendor_family.juniper.JuniperFamily;
import org.batfish.representation.juniper.Nat.Type;

@ParametersAreNonnullByDefault
public class LogicalSystem implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Map<String, BaseApplication> _applications;

  private final Map<String, ApplicationSet> _applicationSets;

  private final Map<String, AsPathGroup> _asPathGroups;

  private final NavigableMap<String, JuniperAuthenticationKeyChain> _authenticationKeyChains;

  private final Map<String, CommunityList> _communityLists;

  private boolean _defaultAddressSelection;

  private LineAction _defaultCrossZoneAction;

  private LineAction _defaultInboundAction;

  private RoutingInstance _defaultRoutingInstance;

  private NavigableSet<String> _dnsServers;

  private final Map<String, FirewallFilter> _filters;

  private final Map<String, AddressBook> _globalAddressBooks;

  private final Map<String, IkeGateway> _ikeGateways;

  private final Map<String, IkePolicy> _ikePolicies;

  private final Map<String, IkeProposal> _ikeProposals;

  private final Map<String, Interface> _interfaces;

  private final Map<String, Zone> _interfaceZones;

  private final Map<String, IpsecPolicy> _ipsecPolicies;

  private final Map<String, IpsecProposal> _ipsecProposals;

  private final Map<String, IpsecVpn> _ipsecVpns;

  private final JuniperFamily _jf;

  private final String _name;

  @Nullable private Nat _natDestination;

  @Nullable private Nat _natSource;

  @Nullable private Nat _natStatic;

  private NavigableSet<String> _ntpServers;

  private final Map<String, PolicyStatement> _policyStatements;

  private final Map<String, PrefixList> _prefixLists;

  private final Map<String, RouteFilter> _routeFilters;

  private final Map<String, RoutingInstance> _routingInstances;

  private NavigableSet<String> _syslogHosts;

  private NavigableSet<String> _tacplusServers;

  private final Map<String, Vlan> _vlanNameToVlan;

  private final Map<String, Zone> _zones;

  public LogicalSystem(String name) {
    _name = name;
    _applications = new TreeMap<>();
    _applicationSets = new TreeMap<>();
    _asPathGroups = new TreeMap<>();
    _authenticationKeyChains = new TreeMap<>();
    _communityLists = new TreeMap<>();
    _defaultCrossZoneAction = LineAction.PERMIT;
    _defaultRoutingInstance = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    _dnsServers = new TreeSet<>();
    _filters = new TreeMap<>();
    _globalAddressBooks = new TreeMap<>();
    _ikeGateways = new TreeMap<>();
    _ikePolicies = new TreeMap<>();
    _ikeProposals = new TreeMap<>();
    _interfaces = new TreeMap<>();
    _interfaceZones = new TreeMap<>();
    _ipsecPolicies = new TreeMap<>();
    _ipsecProposals = new TreeMap<>();
    _ipsecVpns = new TreeMap<>();
    _jf = new JuniperFamily();
    _ntpServers = new TreeSet<>();
    _prefixLists = new TreeMap<>();
    _policyStatements = new TreeMap<>();
    _routeFilters = new TreeMap<>();
    _routingInstances = new TreeMap<>();
    _routingInstances.put(Configuration.DEFAULT_VRF_NAME, _defaultRoutingInstance);
    _syslogHosts = new TreeSet<>();
    _tacplusServers = new TreeSet<>();
    _vlanNameToVlan = new TreeMap<>();
    _zones = new TreeMap<>();
  }

  public Map<String, BaseApplication> getApplications() {
    return _applications;
  }

  public Map<String, ApplicationSet> getApplicationSets() {
    return _applicationSets;
  }

  public Map<String, AsPathGroup> getAsPathGroups() {
    return _asPathGroups;
  }

  public Map<String, JuniperAuthenticationKeyChain> getAuthenticationKeyChains() {
    return _authenticationKeyChains;
  }

  public Map<String, CommunityList> getCommunityLists() {
    return _communityLists;
  }

  public boolean getDefaultAddressSelection() {
    return _defaultAddressSelection;
  }

  public LineAction getDefaultCrossZoneAction() {
    return _defaultCrossZoneAction;
  }

  public LineAction getDefaultInboundAction() {
    return _defaultInboundAction;
  }

  public RoutingInstance getDefaultRoutingInstance() {
    return _defaultRoutingInstance;
  }

  public NavigableSet<String> getDnsServers() {
    return _dnsServers;
  }

  public Map<String, FirewallFilter> getFirewallFilters() {
    return _filters;
  }

  public Map<String, AddressBook> getGlobalAddressBooks() {
    return _globalAddressBooks;
  }

  public Interface getGlobalMasterInterface() {
    return _defaultRoutingInstance.getGlobalMasterInterface();
  }

  public String getHostname() {
    return _defaultRoutingInstance.getHostname();
  }

  public Map<String, IkeGateway> getIkeGateways() {
    return _ikeGateways;
  }

  public Map<String, IkePolicy> getIkePolicies() {
    return _ikePolicies;
  }

  public Map<String, IkeProposal> getIkeProposals() {
    return _ikeProposals;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public Map<String, Zone> getInterfaceZones() {
    return _interfaceZones;
  }

  public Map<String, IpsecPolicy> getIpsecPolicies() {
    return _ipsecPolicies;
  }

  public Map<String, IpsecProposal> getIpsecProposals() {
    return _ipsecProposals;
  }

  public Map<String, IpsecVpn> getIpsecVpns() {
    return _ipsecVpns;
  }

  public JuniperFamily getJf() {
    return _jf;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public Nat getNatDestination() {
    return _natDestination;
  }

  public Nat getNatSource() {
    return _natSource;
  }

  public Nat getNatStatic() {
    return _natStatic;
  }

  public NavigableSet<String> getNtpServers() {
    return _ntpServers;
  }

  public Nat getOrCreateNat(Nat.Type natType) {
    switch (natType) {
      case DESTINATION:
        if (_natDestination == null) {
          _natDestination = new Nat(Type.DESTINATION);
        }
        return _natDestination;
      case SOURCE:
        if (_natSource == null) {
          _natSource = new Nat(Type.SOURCE);
        }
        return _natSource;
      case STATIC:
        if (_natStatic == null) {
          _natStatic = new Nat(Type.STATIC);
        }
        return _natStatic;
      default:
        throw new IllegalArgumentException("Unknnown nat type " + natType);
    }
  }

  public Map<String, PolicyStatement> getPolicyStatements() {
    return _policyStatements;
  }

  public Map<String, PrefixList> getPrefixLists() {
    return _prefixLists;
  }

  public Map<String, RouteFilter> getRouteFilters() {
    return _routeFilters;
  }

  public Map<String, RoutingInstance> getRoutingInstances() {
    return _routingInstances;
  }

  public NavigableSet<String> getSyslogHosts() {
    return _syslogHosts;
  }

  public NavigableSet<String> getTacplusServers() {
    return _tacplusServers;
  }

  public Map<String, Vlan> getVlanNameToVlan() {
    return _vlanNameToVlan;
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

  public void setDefaultRoutingInstance(RoutingInstance defaultRoutingInstance) {
    _defaultRoutingInstance = defaultRoutingInstance;
  }

  public void setHostname(String hostname) {
    _defaultRoutingInstance.setHostname(hostname);
  }

  public void setNatDestination(Nat natDestination) {
    _natDestination = natDestination;
  }

  public void setNatSource(Nat natSource) {
    _natSource = natSource;
  }

  public void setNatStatic(Nat natStatic) {
    _natStatic = natStatic;
  }

  public void setSyslogHosts(NavigableSet<String> syslogHosts) {
    _syslogHosts = syslogHosts;
  }
}
