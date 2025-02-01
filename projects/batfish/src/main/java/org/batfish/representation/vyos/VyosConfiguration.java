package org.batfish.representation.vyos;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.vendor.VendorConfiguration;

public class VyosConfiguration extends VendorConfiguration {

  private BgpProcess _bgpProcess;

  private Configuration _c;

  private final Map<String, EspGroup> _espGroups;

  private ConfigurationFormat _format;

  protected String _hostname;

  private final Map<String, IkeGroup> _ikeGroups;

  private final Map<String, Interface> _interfaces;

  private final Set<String> _ipsecInterfaces;

  private final Map<Ip, IpsecPeer> _ipsecPeers;

  private transient Map<Ip, org.batfish.datamodel.Interface> _ipToInterfaceMap;

  private final Map<String, PrefixList> _prefixLists;

  private final Map<String, RouteMap> _routeMaps;

  private final Set<StaticNextHopRoute> _staticNextHopRoutes;

  public VyosConfiguration() {
    _espGroups = new TreeMap<>();
    _ikeGroups = new TreeMap<>();
    _interfaces = new TreeMap<>();
    _ipsecInterfaces = new TreeSet<>();
    _ipsecPeers = new TreeMap<>();
    _prefixLists = new TreeMap<>();
    _routeMaps = new TreeMap<>();
    _staticNextHopRoutes = new HashSet<>();
  }

  private void convertInterfaces() {
    for (Entry<String, Interface> e : _interfaces.entrySet()) {
      Interface iface = e.getValue();
      toInterface(iface);
    }
  }

  private void convertPrefixLists() {
    for (Entry<String, PrefixList> e : _prefixLists.entrySet()) {
      String name = e.getKey();
      PrefixList prefixList = e.getValue();
      RouteFilterList routeFilterList = toRouteFilterList(prefixList);
      _c.getRouteFilterLists().put(name, routeFilterList);
    }
  }

  private void convertRouteMaps() {
    for (Entry<String, RouteMap> e : _routeMaps.entrySet()) {
      String name = e.getKey();
      RouteMap routeMap = e.getValue();
      RoutingPolicy rp = toRoutingPolicy(routeMap);
      _c.getRoutingPolicies().put(name, rp);
    }
  }

  private void convertVpns() {
    ImmutableSortedMap.Builder<String, IkePhase1Policy> ikePhase1PolicyMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IkePhase1Key> ikePhase1KeyMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IkePhase1Proposal> ikePhase1ProposalMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IpsecPhase2Proposal> ipsecPhase2ProposalMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IpsecPhase2Policy> ipsecPhase2PolicyMapBuilder =
        ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigMapBuilder =
        ImmutableSortedMap.naturalOrder();
    for (Entry<Ip, IpsecPeer> ipsecPeerEntry : _ipsecPeers.entrySet()) {
      // converting to IPsec Phase1 and Phase2 datamodels
      // site-to-site peer
      Ip peerAddress = ipsecPeerEntry.getKey();
      IpsecPeer ipsecPeer = ipsecPeerEntry.getValue();
      String newIpsecVpnName = peerAddress.toString();

      IpsecStaticPeerConfig.Builder ipsecPeerConfigBuilder = IpsecStaticPeerConfig.builder();
      ipsecPeerConfigBuilder.setDestinationAddress(peerAddress);
      Ip localAddress = ipsecPeer.getLocalAddress();
      if (localAddress == null || !localAddress.valid()) {
        _w.redFlag("No local address configured for VPN " + newIpsecVpnName);
        continue;
      }
      org.batfish.datamodel.Interface externalInterface = _ipToInterfaceMap.get(localAddress);
      if (externalInterface == null) {
        _w.redFlag(
            "Could not determine external interface for vpn \""
                + newIpsecVpnName
                + "\" from local-address: "
                + localAddress);
      } else {
        ipsecPeerConfigBuilder.setSourceInterface(externalInterface.getName());
        ipsecPeerConfigBuilder.setLocalAddress(localAddress);
      }

      // bind interface
      String bindInterfaceName = ipsecPeer.getBindInterface();
      org.batfish.datamodel.Interface newBindInterface =
          _c.getAllInterfaces().get(bindInterfaceName);
      if (newBindInterface != null) {
        ipsecPeerConfigBuilder.setTunnelInterface(newBindInterface.getName());
      } else {
        _w.redFlag("Reference to undefined bind-interface: \"" + bindInterfaceName + "\"");
      }

      // convert the referenced ike group
      String ikeGroupName = ipsecPeer.getIkeGroup();
      IkeGroup ikeGroup = _ikeGroups.get(ikeGroupName);
      if (ikeGroup == null) {
        _w.redFlag("Reference to undefined ike-group: \"" + ikeGroupName + "\"");
      } else {
        IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(ikeGroupName);

        // pre-shared-key
        IkePhase1Key ikePhase1Key = new IkePhase1Key();
        ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
        ikePhase1Key.setKeyHash(ipsecPeer.getAuthenticationPreSharedSecretHash());

        ikePhase1KeyMapBuilder.put(
            String.format("~IKE_PHASE1_KEY_%s~", ipsecPeer.getName()), ikePhase1Key);

        ikePhase1Policy.setIkePhase1Key(ikePhase1Key);
        ikePhase1Policy.setLocalInterface(
            externalInterface == null ? null : externalInterface.getName());
        ikePhase1Policy.setRemoteIdentity(peerAddress.toIpSpace());

        ikePhase1PolicyMapBuilder.put(ikeGroupName, ikePhase1Policy);

        // convert contained ike proposals to IKE phase 1 proposals
        for (Entry<Integer, IkeProposal> ikeProposalEntry : ikeGroup.getProposals().entrySet()) {
          String newIkeProposalName = ikeGroupName + ":" + ikeProposalEntry.getKey();
          IkeProposal ikeProposal = ikeProposalEntry.getValue();
          IkePhase1Proposal ikePhase1Proposal = new IkePhase1Proposal(newIkeProposalName);
          ikePhase1Proposal.setDiffieHellmanGroup(ikeProposal.getDhGroup());
          ikePhase1Proposal.setEncryptionAlgorithm(ikeProposal.getEncryptionAlgorithm());
          ikePhase1Proposal.setLifetimeSeconds(ikeGroup.getLifetimeSeconds());
          ikePhase1Proposal.setHashingAlgorithm(
              ikeProposal.getHashAlgorithm().toIkeAuthenticationAlgorithm());
          ikePhase1Proposal.setAuthenticationMethod(ipsecPeer.getAuthenticationMode());
          ikePhase1ProposalMapBuilder.put(newIkeProposalName, ikePhase1Proposal);
          ikePhase1Policy.getIkePhase1Proposals().add(newIkeProposalName);
        }
        ipsecPeerConfigBuilder.setIkePhase1Policy(ikePhase1Policy.getName());
      }

      // convert the referenced esp group
      String espGroupName = ipsecPeer.getEspGroup();
      EspGroup espGroup = _espGroups.get(espGroupName);
      if (espGroup == null) {
        _w.redFlag("Reference to undefined esp-group: \"" + espGroupName + "\"");
      } else {
        IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
        ipsecPhase2PolicyMapBuilder.put(espGroupName, ipsecPhase2Policy);
        if (espGroup.getPfsSource() == null) {
          espGroup.setPfsSource(PfsSource.IKE_GROUP);
        }
        switch (espGroup.getPfsSource()) {
          case ESP_GROUP -> ipsecPhase2Policy.setPfsKeyGroup(espGroup.getPfsDhGroup());
          case DISABLED, IKE_GROUP -> {}
        }

        // convert contained esp proposals
        for (Entry<Integer, EspProposal> espProposalEntry : espGroup.getProposals().entrySet()) {
          String newIpsecProposalName = espGroupName + ":" + espProposalEntry.getKey();
          EspProposal espProposal = espProposalEntry.getValue();
          IpsecPhase2Proposal ipsecPhase2Proposal = new IpsecPhase2Proposal();
          ipsecPhase2Proposal.setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP));
          ipsecPhase2Proposal.setEncryptionAlgorithm(espProposal.getEncryptionAlgorithm());
          ipsecPhase2Proposal.setAuthenticationAlgorithm(
              espProposal.getHashAlgorithm().toIpsecAuthenticationAlgorithm());
          ipsecPhase2ProposalMapBuilder.put(newIpsecProposalName, ipsecPhase2Proposal);
          ipsecPhase2Policy.getProposals().add(newIpsecProposalName);
        }
        ipsecPeerConfigBuilder.setIpsecPolicy(espGroupName);
      }
      ipsecPeerConfigMapBuilder.put(newIpsecVpnName, ipsecPeerConfigBuilder.build());
    }
    _c.setIkePhase1Proposals(ikePhase1ProposalMapBuilder.build());
    _c.setIkePhase1Keys(ikePhase1KeyMapBuilder.build());
    _c.setIkePhase1Policies(ikePhase1PolicyMapBuilder.build());
    _c.setIpsecPhase2Proposals(ipsecPhase2ProposalMapBuilder.build());
    _c.setIpsecPhase2Policies(ipsecPhase2PolicyMapBuilder.build());
    _c.setIpsecPeerConfigs(ipsecPeerConfigMapBuilder.build());
  }

  public BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public Map<String, EspGroup> getEspGroups() {
    return _espGroups;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<String, IkeGroup> getIkeGroups() {
    return _ikeGroups;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public Set<String> getIpsecInterfaces() {
    return _ipsecInterfaces;
  }

  public Map<Ip, IpsecPeer> getIpsecPeers() {
    return _ipsecPeers;
  }

  public Map<String, PrefixList> getPrefixLists() {
    return _prefixLists;
  }

  public Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  public Set<StaticNextHopRoute> getStaticNextHopRoutes() {
    return _staticNextHopRoutes;
  }

  public void setBgpProcess(BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _format = format;
  }

  private org.batfish.datamodel.Interface toInterface(Interface iface) {
    String name = iface.getName();
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder().setName(name).setOwner(_c).build();
    newIface.setDeclaredNames(ImmutableSortedSet.of(name));
    newIface.setBandwidth(iface.getBandwidth());
    newIface.setDescription(iface.getDescription());
    InterfaceAddress address = iface.getAddress();
    if (address != null) {
      newIface.setAddress(iface.getAddress());
    }
    newIface.getAllConcreteAddresses().addAll(iface.getAllAddresses());
    for (ConcreteInterfaceAddress p : newIface.getAllConcreteAddresses()) {
      _ipToInterfaceMap.put(p.getIp(), newIface);
    }
    return newIface;
  }

  private RouteFilterList toRouteFilterList(PrefixList prefixList) {
    String name = prefixList.getName();
    RouteFilterList newList = new RouteFilterList(name);
    List<RouteFilterLine> newLines =
        prefixList.getRules().values().stream()
            .map(l -> new RouteFilterLine(l.getAction(), l.getPrefix(), l.getLengthRange()))
            .collect(ImmutableList.toImmutableList());
    newList.setLines(newLines);
    return newList;
  }

  private RoutingPolicy toRoutingPolicy(RouteMap routeMap) {
    String name = routeMap.getName();
    RoutingPolicy routingPolicy = new RoutingPolicy(name, _c);
    List<Statement> statements = routingPolicy.getStatements();
    for (Entry<Integer, RouteMapRule> e : routeMap.getRules().entrySet()) {
      String ruleName = Integer.toString(e.getKey());
      RouteMapRule rule = e.getValue();
      If ifStatement = new If();
      List<Statement> trueStatements = ifStatement.getTrueStatements();
      ifStatement.setComment(ruleName);
      Conjunction conj = new Conjunction();
      for (RouteMapMatch match : rule.getMatches()) {
        conj.getConjuncts().add(match.toBooleanExpr(this, _c, _w));
      }
      ifStatement.setGuard(conj.simplify());
      switch (rule.getAction()) {
        case PERMIT -> trueStatements.add(Statements.ExitAccept.toStaticStatement());
        case DENY -> trueStatements.add(Statements.ExitReject.toStaticStatement());
      }
      statements.add(ifStatement);
    }
    statements.add(Statements.ExitReject.toStaticStatement());
    return routingPolicy;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    _ipToInterfaceMap = new HashMap<>();
    _c = new Configuration(_hostname, _format);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    convertPrefixLists();
    convertRouteMaps();
    convertInterfaces();
    convertVpns();

    // TODO: convert routing processes

    return ImmutableList.of(_c);
  }
}
