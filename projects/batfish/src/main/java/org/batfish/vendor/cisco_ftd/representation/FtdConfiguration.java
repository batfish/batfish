package org.batfish.vendor.cisco_ftd.representation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.datamodel.vendor_family.cisco.Service;
import org.batfish.vendor.VendorConfiguration;

/** Represents a Cisco Firepower Threat Defense (FTD) configuration. */
public class FtdConfiguration extends VendorConfiguration {

  public FtdConfiguration() {
    _interfaces = new HashMap<>();
    _accessLists = new HashMap<>();
    _networkObjects = new HashMap<>();
    _networkObjectGroups = new HashMap<>();
    _serviceObjectGroups = new HashMap<>();
    _ospfProcesses = new HashMap<>();
    _natRules = new ArrayList<>();
    _routes = new ArrayList<>();
    _failoverLines = new ArrayList<>();
    _accessGroups = new ArrayList<>();
    _classMaps = new HashMap<>();
    _policyMaps = new HashMap<>();
    _servicePolicies = new ArrayList<>();
    _cryptoMaps = new HashMap<>();
    _cryptoMapInterfaceBindings = new HashMap<>();
    _ipsecTransformSets = new HashMap<>();
    _ipsecProfiles = new HashMap<>();
    _ikev2Policies = new HashMap<>();
    _tunnelGroups = new HashMap<>();
    _vrfs = new TreeMap<>();
    _vrfs.put(Configuration.DEFAULT_VRF_NAME, new Vrf(Configuration.DEFAULT_VRF_NAME));
    _names = new HashMap<>();
  }

  @Override
  public @Nonnull String getHostname() {
    return _hostname != null ? _hostname : "ftd";
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public Map<String, FtdAccessList> getAccessLists() {
    return _accessLists;
  }

  public Map<String, FtdNetworkObject> getNetworkObjects() {
    return _networkObjects;
  }

  public Map<String, FtdNetworkObjectGroup> getNetworkObjectGroups() {
    return _networkObjectGroups;
  }

  public Map<String, FtdServiceObjectGroup> getServiceObjectGroups() {
    return _serviceObjectGroups;
  }

  public Map<String, FtdOspfProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  public Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  public List<FtdNatRule> getNatRules() {
    return _natRules;
  }

  public List<FtdRoute> getRoutes() {
    return _routes;
  }

  public List<String> getFailoverLines() {
    return _failoverLines;
  }

  public List<FtdAccessGroup> getAccessGroups() {
    return _accessGroups;
  }

  public Map<String, FtdClassMap> getClassMaps() {
    return _classMaps;
  }

  public Map<String, FtdPolicyMap> getPolicyMaps() {
    return _policyMaps;
  }

  public List<FtdServicePolicy> getServicePolicies() {
    return _servicePolicies;
  }

  public Map<String, FtdCryptoMapSet> getCryptoMaps() {
    return _cryptoMaps;
  }

  public Map<String, Set<String>> getCryptoMapInterfaceBindings() {
    return _cryptoMapInterfaceBindings;
  }

  public Map<String, FtdIpsecTransformSet> getIpsecTransformSets() {
    return _ipsecTransformSets;
  }

  public Map<String, FtdIpsecProfile> getIpsecProfiles() {
    return _ipsecProfiles;
  }

  public Map<Integer, FtdIkev2Policy> getIkev2Policies() {
    return _ikev2Policies;
  }

  public Map<String, FtdTunnelGroup> getTunnelGroups() {
    return _tunnelGroups;
  }

  public void addNatRule(FtdNatRule rule) {
    _natRules.add(rule);
  }

  public void addClassMap(@Nonnull FtdClassMap classMap) {
    _classMaps.put(classMap.getName(), classMap);
  }

  public void addPolicyMap(@Nonnull FtdPolicyMap policyMap) {
    _policyMaps.put(policyMap.getName(), policyMap);
  }

  public void addServicePolicy(@Nonnull FtdServicePolicy servicePolicy) {
    _servicePolicies.add(servicePolicy);
  }

  public void addCryptoMapInterfaceBinding(@Nonnull String cryptoMap, @Nonnull String ifaceName) {
    _cryptoMapInterfaceBindings.computeIfAbsent(cryptoMap, key -> new HashSet<>()).add(ifaceName);
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    Configuration c =
        Configuration.builder().setHostname(hostname).setConfigurationFormat(_vendor).build();
    c.setDeviceModel(DeviceModel.CISCO_FTD);
    c.setHumanName(getHostname());
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);

    // Convert VRFs
    for (String vrfName : _vrfs.keySet()) {
      if (!vrfName.equals(Configuration.DEFAULT_VRF_NAME)) {
        c.getVrfs().computeIfAbsent(vrfName, Vrf::new);
      }
    }

    // Convert simple static routes parsed from 'route' stanzas
    Vrf defaultVrf = c.getDefaultVrf();
    if (defaultVrf != null) {
      for (FtdRoute r : _routes) {
        Prefix p = Prefix.create(r.getNetwork(), r.getMask());
        StaticRoute sr =
            StaticRoute.builder()
                .setNetwork(p)
                .setNextHop(NextHopIp.of(r.getGateway()))
                .setAdministrativeCost(r.getMetric())
                .setMetric(0L)
                .setTag(0)
                .build();
        defaultVrf.getStaticRoutes().add(sr);
      }
    }

    // Convert access lists
    _accessLists.forEach((name, acl) -> convertAccessList(name, acl, c));

    // Convert MPF class-map/policy-map into synthetic ACLs
    convertMpfToAcls(c);

    // Create zones from nameif values
    createZones(c);

    // Convert interfaces
    _interfaces.forEach((name, repIface) -> convertInterface(repIface, c));

    // Apply access-groups to interfaces
    applyAccessGroups(c);

    // Apply default security-level behavior if no ACL is present
    applySecurityLevelDefaults(c);

    // Apply NAT translations to interfaces
    applyNatRules(c);

    // Convert OSPF processes
    convertOspfProcesses(c);

    // Convert BGP process
    if (_bgpProcess != null) {
      convertBgpProcess(_bgpProcess, c);
    }

    // Record MPF metadata in vendor family
    applyMpfMetadata(c);

    // Convert VPN settings (IKEv2, IPsec, Tunnel Groups)
    convertVpnSettings(c);

    // Convert names (hostname mappings) to vendor family metadata
    convertNames(c);

    // Convert ARP timeout to interface settings
    convertArpTimeout(c);

    return List.of(c);
  }

  private void convertNames(Configuration c) {
    if (_names.isEmpty() || !_namesEnabled) {
      return;
    }
    CiscoFamily cisco = c.getVendorFamily().getCisco();
    if (cisco == null) {
      cisco = new CiscoFamily();
      c.getVendorFamily().setCisco(cisco);
    }
    Service namesService = cisco.getServices().computeIfAbsent("names", k -> new Service());
    _names.forEach((name, ip) -> namesService.getSubservices().put(name + ":" + ip, new Service()));
  }

  private void convertArpTimeout(Configuration c) {
    if (_arpTimeout == null) {
      return;
    }
    // Store ARP timeout in vendor family metadata
    CiscoFamily cisco = c.getVendorFamily().getCisco();
    if (cisco == null) {
      cisco = new CiscoFamily();
      c.getVendorFamily().setCisco(cisco);
    }
    Service arpService = cisco.getServices().computeIfAbsent("arp", k -> new Service());
    arpService.getSubservices().put("timeout:" + _arpTimeout, new Service());
  }

  // Configuration properties
  private @Nullable String _hostname;
  private @Nullable ConfigurationFormat _vendor;
  private final @Nonnull Map<String, Interface> _interfaces;
  private final @Nonnull Map<String, FtdAccessList> _accessLists;
  private final @Nonnull Map<String, FtdNetworkObject> _networkObjects;
  private final @Nonnull Map<String, FtdNetworkObjectGroup> _networkObjectGroups;
  private final @Nonnull Map<String, FtdServiceObjectGroup> _serviceObjectGroups;
  private final @Nonnull Map<String, FtdOspfProcess> _ospfProcesses;
  private final @Nonnull Map<String, Vrf> _vrfs;
  private final @Nonnull List<FtdNatRule> _natRules;
  private final @Nonnull List<FtdRoute> _routes;
  private final @Nonnull List<String> _failoverLines;
  private final @Nonnull List<FtdAccessGroup> _accessGroups;
  private final @Nonnull Map<String, FtdClassMap> _classMaps;
  private final @Nonnull Map<String, FtdPolicyMap> _policyMaps;
  private final @Nonnull List<FtdServicePolicy> _servicePolicies;
  private final @Nonnull Map<String, FtdCryptoMapSet> _cryptoMaps;
  private final @Nonnull Map<String, Set<String>> _cryptoMapInterfaceBindings;
  private final @Nonnull Map<String, FtdIpsecTransformSet> _ipsecTransformSets;
  private final @Nonnull Map<String, FtdIpsecProfile> _ipsecProfiles;
  private final @Nonnull Map<Integer, FtdIkev2Policy> _ikev2Policies;
  private final @Nonnull Map<String, FtdTunnelGroup> _tunnelGroups;
  private @Nullable FtdBgpProcess _bgpProcess;
  private final @Nonnull Map<String, String> _names; // name-to-IP mappings
  private @Nullable Integer _arpTimeout; // ARP timeout in seconds
  private boolean _namesEnabled = false;

  public @Nullable FtdBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public void setBgpProcess(@Nullable FtdBgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  public @Nonnull Map<String, String> getNames() {
    return _names;
  }

  public void setNamesEnabled(boolean enabled) {
    _namesEnabled = enabled;
  }

  public boolean getNamesEnabled() {
    return _namesEnabled;
  }

  public void setArpTimeout(@Nullable Integer arpTimeout) {
    _arpTimeout = arpTimeout;
  }

  public @Nullable Integer getArpTimeout() {
    return _arpTimeout;
  }

  private void convertVpnSettings(Configuration c) {
    convertIkev2Policies(c);
    convertIpsecTransformSets(c);
    convertIpsecProfiles(c);
    convertCryptoMaps(c);
    convertTunnelGroups(c);
  }

  private void convertIkev2Policies(Configuration c) {
    Map<String, IkePhase1Proposal> proposals = new TreeMap<>();
    Map<String, IkePhase1Policy> policies = new TreeMap<>();

    for (FtdIkev2Policy repPolicy : _ikev2Policies.values()) {
      List<IkePhase1Proposal> viProposals = FtdConversions.toIkePhase1Proposals(repPolicy);
      for (IkePhase1Proposal p : viProposals) {
        proposals.put(p.getName(), p);
      }
      IkePhase1Policy viPolicy = new IkePhase1Policy(String.valueOf(repPolicy.getPriority()));
      viPolicy.setIkePhase1Proposals(
          viProposals.stream().map(IkePhase1Proposal::getName).collect(Collectors.toList()));
      policies.put(viPolicy.getName(), viPolicy);
    }

    c.setIkePhase1Proposals(ImmutableSortedMap.copyOf(proposals));
    c.setIkePhase1Policies(ImmutableSortedMap.copyOf(policies));
  }

  private void convertIpsecTransformSets(Configuration c) {
    Map<String, IpsecPhase2Proposal> proposals = new TreeMap<>();
    for (FtdIpsecTransformSet repSet : _ipsecTransformSets.values()) {
      proposals.put(repSet.getName(), FtdConversions.toIpsecPhase2Proposal(repSet));
    }
    c.setIpsecPhase2Proposals(ImmutableSortedMap.copyOf(proposals));
  }

  private void convertIpsecProfiles(Configuration c) {
    Map<String, IpsecPhase2Policy> policies = new TreeMap<>();
    for (FtdIpsecProfile repProfile : _ipsecProfiles.values()) {
      policies.put(repProfile.getName(), FtdConversions.toIpsecPhase2Policy(repProfile));
    }
    c.setIpsecPhase2Policies(ImmutableSortedMap.copyOf(policies));
  }

  private void convertCryptoMaps(Configuration c) {
    // Build nameif to interface name mapping
    Map<String, String> nameifToInterface = new HashMap<>();
    _interfaces.forEach(
        (ifName, repIface) -> {
          if (repIface.getNameif() != null) {
            nameifToInterface.put(repIface.getNameif(), ifName);
          }
        });

    // Set crypto maps on interfaces
    for (Map.Entry<String, Set<String>> binding : _cryptoMapInterfaceBindings.entrySet()) {
      String cryptoMapName = binding.getKey();
      for (String interfaceNameOrNameif : binding.getValue()) {
        // Try direct interface name first, then nameif mapping
        String interfaceName =
            nameifToInterface.getOrDefault(interfaceNameOrNameif, interfaceNameOrNameif);
        org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(interfaceName);
        if (iface != null) {
          iface.setCryptoMap(cryptoMapName);
        }
      }
    }

    Map<String, IpsecPeerConfig> peerConfigs = new TreeMap<>(c.getIpsecPeerConfigs());
    Map<String, IpsecPhase2Policy> phase2Policies = new TreeMap<>(c.getIpsecPhase2Policies());

    for (FtdCryptoMapSet repMapSet : _cryptoMaps.values()) {
      for (FtdCryptoMapEntry entry : repMapSet.getEntries().values()) {
        String policyName =
            String.format(
                "~IPSEC_PHASE2_POLICY:%s:%d~", repMapSet.getName(), entry.getSequenceNumber());
        phase2Policies.put(policyName, FtdConversions.toIpsecPhase2Policy(entry));
        peerConfigs.putAll(
            FtdConversions.toIpsecPeerConfigs(c, entry, repMapSet.getName(), policyName, _w));
      }
    }

    c.setIpsecPhase2Policies(ImmutableSortedMap.copyOf(phase2Policies));
    c.setIpsecPeerConfigs(ImmutableSortedMap.copyOf(peerConfigs));
  }

  private void convertTunnelGroups(Configuration c) {
    Map<String, IkePhase1Key> keys = new TreeMap<>();
    for (FtdTunnelGroup tg : _tunnelGroups.values()) {
      if (tg.getPresharedKey() != null) {
        keys.put(tg.getName(), FtdConversions.toIkePhase1Key(tg));
      }
    }
    c.setIkePhase1Keys(ImmutableSortedMap.copyOf(keys));
  }

  /** Create zones from nameif values. */
  private void createZones(Configuration c) {
    Map<String, Zone> zones = c.getZones();
    Map<String, Set<String>> zoneInterfaces = new HashMap<>();
    for (Map.Entry<String, Interface> entry : _interfaces.entrySet()) {
      String interfaceName = entry.getKey();
      Interface iface = entry.getValue();
      String nameif = iface.getNameif();
      if (nameif != null) {
        zoneInterfaces.computeIfAbsent(nameif, key -> new HashSet<>()).add(interfaceName);
      }
    }
    zoneInterfaces.forEach(
        (name, interfaces) -> {
          Zone zone = zones.get(name);
          if (zone == null) {
            zone = new Zone(name);
            zones.put(name, zone);
          }
          zone.setInterfaces(interfaces);
        });
  }

  private void convertBgpProcess(FtdBgpProcess bgp, Configuration c) {
    if (bgp.getRouterId() == null) {
      return;
    }
    BgpProcess proc =
        BgpProcess.builder()
            .setRouterId(bgp.getRouterId())
            .setEbgpAdminCost(20)
            .setIbgpAdminCost(200)
            .setLocalAdminCost(200)
            .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP)
            .build();
    Vrf defaultVrf = c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new);
    defaultVrf.setBgpProcess(proc);

    for (FtdBgpNeighbor neighbor : bgp.getNeighbors().values()) {
      if (neighbor.getRemoteAs() == null) {
        continue;
      }
      if (bgp.hasIpv4AddressFamily() && !neighbor.isIpv4UnicastActive()) {
        continue;
      }
      BgpActivePeerConfig.Builder peerBuilder =
          BgpActivePeerConfig.builder()
              .setLocalAs(bgp.getAsn())
              .setRemoteAs(neighbor.getRemoteAs())
              .setPeerAddress(neighbor.getIp())
              .setBgpProcess(proc)
              .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build());

      if (neighbor.getDescription() != null) {
        peerBuilder.setDescription(neighbor.getDescription());
      }

      // Heuristic: if LocalIP is not set, Batfish might fail to bring up session if
      // it can't resolve it.
      // But for now we rely on Batfish core to match via PeerAddress.
      peerBuilder.build();
    }
  }

  private void convertAccessList(String name, FtdAccessList acl, Configuration c) {
    Map<String, String> nameifToInterface = new HashMap<>();
    _interfaces.forEach(
        (ifName, repIface) -> {
          if (repIface.getNameif() != null) {
            nameifToInterface.put(repIface.getNameif(), ifName);
          }
        });

    // FTD configs commonly include FMC metadata (rule-id, RULE name, policy name) in remark lines.
    // Preserve that metadata in the VI ACL line names so questions like filterLineReachability
    // produce actionable results instead of raw ExprAclLine{...} blobs.
    Map<Long, RemarkRuleMetadata> remarkByRuleId = buildRemarkMetadata(acl);

    List<AclLine> lines =
        acl.getLines().stream()
            .filter(line -> !line.isInactive()) // Skip inactive lines
            .map(line -> convertAccessListLine(line, nameifToInterface, remarkByRuleId))
            .filter(line -> line != null) // Skip unsupported lines
            .collect(Collectors.toList());

    IpAccessList ipAccessList = IpAccessList.builder().setName(name).setLines(lines).build();
    c.getIpAccessLists().put(name, ipAccessList);
  }

  private static final class RemarkRuleMetadata {
    @Nullable String _ruleName;
    @Nullable String _policyName;
  }

  private static final Pattern RULE_ID_PATTERN = Pattern.compile("\\brule-id\\s+(\\d+)\\b");
  private static final Pattern RULE_NAME_PATTERN =
      Pattern.compile("\\bRULE:\\s*(.+)$"); // "RULE: <name>"
  private static final Pattern POLICY_NAME_PATTERN =
      Pattern.compile("\\bPREFILTER\\s+POLICY:\\s*(.+)$"); // "PREFILTER POLICY: <name>"

  private static Map<Long, RemarkRuleMetadata> buildRemarkMetadata(FtdAccessList acl) {
    Map<Long, RemarkRuleMetadata> out = new HashMap<>();
    for (FtdAccessListLine line : acl.getLines()) {
      if (line.getAclType() != FtdAccessListLine.AclType.REMARK) {
        continue;
      }
      String remark = line.getRemark();
      if (remark == null) {
        continue;
      }
      Matcher idm = RULE_ID_PATTERN.matcher(remark);
      if (!idm.find()) {
        continue;
      }
      Long ruleId;
      try {
        ruleId = Long.parseLong(idm.group(1));
      } catch (NumberFormatException e) {
        continue;
      }
      RemarkRuleMetadata meta = out.computeIfAbsent(ruleId, k -> new RemarkRuleMetadata());

      Matcher rm = RULE_NAME_PATTERN.matcher(remark);
      if (rm.find()) {
        meta._ruleName = rm.group(1).trim();
      }
      Matcher pm = POLICY_NAME_PATTERN.matcher(remark);
      if (pm.find()) {
        meta._policyName = pm.group(1).trim();
      }
    }
    return out;
  }

  private static String summarizeAddress(@Nullable FtdAccessListAddressSpecifier spec) {
    if (spec == null) {
      return "any";
    }
    switch (spec.getType()) {
      case ANY:
      case ANY4:
        return "any";
      case HOST:
        return spec.getIp() != null ? "host " + spec.getIp() : "host ?";
      case NETWORK_MASK:
        return (spec.getIp() != null && spec.getMask() != null)
            ? spec.getIp() + " " + spec.getMask()
            : "network ?";
      case OBJECT:
        return spec.getObjectName() != null ? "object " + spec.getObjectName() : "object ?";
      case OBJECT_GROUP:
        return spec.getObjectName() != null
            ? "object-group " + spec.getObjectName()
            : "object-group ?";
      default:
        return "any";
    }
  }

  private static String buildLineDisplayName(
      FtdAccessListLine line, Map<Long, RemarkRuleMetadata> remarkByRuleId) {
    StringBuilder sb = new StringBuilder();

    Long ruleId = line.getRuleId();
    if (ruleId != null) {
      sb.append("rule-id ").append(ruleId);
      RemarkRuleMetadata meta = remarkByRuleId.get(ruleId);
      if (meta != null) {
        if (meta._ruleName != null && !meta._ruleName.isEmpty()) {
          sb.append(" | ").append(meta._ruleName);
        }
        if (meta._policyName != null && !meta._policyName.isEmpty()) {
          sb.append(" | ").append(meta._policyName);
        }
      }
      sb.append(" :: ");
    }

    if (line.getInterfaceName() != null && !line.getInterfaceName().isEmpty()) {
      sb.append("ifc ").append(line.getInterfaceName()).append(' ');
    }

    // In FTD, "trust" is a distinct keyword but semantically a PERMIT.
    if (line.isTrust()) {
      sb.append("trust");
    } else {
      sb.append(line.getAction() != null ? line.getAction() : LineAction.PERMIT);
    }
    sb.append(' ');
    sb.append(line.getProtocol() != null ? line.getProtocol() : "ip");
    sb.append(' ');
    sb.append(summarizeAddress(line.getSourceAddressSpecifier()));
    sb.append(" -> ");
    sb.append(summarizeAddress(line.getDestinationAddressSpecifier()));

    if (line.getDestinationPortSpecifier() != null
        && !line.getDestinationPortSpecifier().isEmpty()) {
      sb.append(' ').append(line.getDestinationPortSpecifier().trim());
    }

    return sb.toString().trim();
  }

  private AclLine convertAccessListLine(
      FtdAccessListLine line,
      Map<String, String> nameifToInterface,
      Map<Long, RemarkRuleMetadata> remarkByRuleId) {
    if (line.getAclType() == FtdAccessListLine.AclType.REMARK) {
      // Remarks are not converted to ACL lines
      return null;
    }

    // Build header space for the ACL line
    HeaderSpace.Builder headerSpace = HeaderSpace.builder();

    // Set protocol
    IpProtocol ipProtocol = toIpProtocol(line.getProtocol());
    if (ipProtocol != null) {
      headerSpace.setIpProtocols(ImmutableSet.of(ipProtocol));
    }

    // Convert source address
    if (line.getSourceAddressSpecifier() != null) {
      IpSpace srcIpSpace = toIpSpace(line.getSourceAddressSpecifier());
      if (srcIpSpace != null) {
        headerSpace.setSrcIps(srcIpSpace);
      }
    }

    // Convert destination address
    if (line.getDestinationAddressSpecifier() != null) {
      IpSpace dstIpSpace = toIpSpace(line.getDestinationAddressSpecifier());
      if (dstIpSpace != null) {
        headerSpace.setDstIps(dstIpSpace);
      }
    }

    // Convert destination ports if present
    if (line.getDestinationPortSpecifier() != null) {
      String portSpec = line.getDestinationPortSpecifier();
      String trimmed = portSpec.trim();
      if (trimmed.toLowerCase().startsWith("object-group")) {
        String groupName = trimmed.substring("object-group".length()).trim();
        ServiceObjectGroupMatch match = resolveServiceObjectGroupMatch(groupName, new HashSet<>());
        if (match != null) {
          if (!match.getProtocols().isEmpty()) {
            if (ipProtocol == null) {
              headerSpace.setIpProtocols(ImmutableSet.copyOf(match.getProtocols()));
            } else if (!match.getProtocols().contains(ipProtocol)) {
              return null;
            }
          }
          if (!match.isAnyPort() && match.getPorts() != null) {
            headerSpace.setDstPorts(match.getPorts().getSubRanges());
          }
        }
      } else {
        IntegerSpace ports = toPortSpace(portSpec);
        if (ports != null) {
          headerSpace.setDstPorts(ports.getSubRanges());
        }
      }
    }

    // For now, create a simple match header space ACL line
    MatchHeaderSpace matchCondition = new MatchHeaderSpace(headerSpace.build());
    AclLineMatchExpr combinedMatch = matchCondition;
    if (line.getInterfaceName() != null) {
      String interfaceName =
          nameifToInterface.getOrDefault(line.getInterfaceName(), line.getInterfaceName());
      combinedMatch =
          AclLineMatchExprs.and(combinedMatch, AclLineMatchExprs.matchSrcInterface(interfaceName));
    }
    return ExprAclLine.builder()
        .setAction(line.getAction())
        .setMatchCondition(combinedMatch)
        .setName(buildLineDisplayName(line, remarkByRuleId))
        .build();
  }

  private @Nullable IpSpace toIpSpace(FtdAccessListAddressSpecifier specifier) {
    switch (specifier.getType()) {
      case ANY:
        return UniverseIpSpace.INSTANCE;
      case ANY4:
        return UniverseIpSpace.INSTANCE;
      case ANY6:
        return null;
      case HOST:
        return specifier.getIp() != null ? specifier.getIp().toIpSpace() : null;
      case NETWORK_MASK:
        if (specifier.getIp() == null || specifier.getMask() == null) {
          return null;
        }
        return Prefix.create(specifier.getIp(), specifier.getMask()).toIpSpace();
      case OBJECT:
        return resolveNetworkObjectIpSpace(specifier.getObjectName(), new HashSet<>());
      case OBJECT_GROUP:
        return resolveNetworkObjectGroupIpSpace(specifier.getObjectName(), new HashSet<>());
    }
    throw new IllegalStateException("Unhandled AddressType: " + specifier.getType());
  }

  private @Nullable IpSpace resolveNetworkObjectIpSpace(
      @Nullable String name, Set<String> visited) {
    if (name == null || !visited.add(name)) {
      return null;
    }
    FtdNetworkObject obj = _networkObjects.get(name);
    if (obj == null) {
      return null;
    }
    return obj.toIpSpace();
  }

  private @Nullable IpSpace resolveNetworkObjectGroupIpSpace(
      @Nullable String name, Set<String> visited) {
    if (name == null || !visited.add(name)) {
      return null;
    }
    FtdNetworkObjectGroup group = _networkObjectGroups.get(name);
    if (group == null) {
      return null;
    }
    List<IpSpace> members = new ArrayList<>();
    for (FtdNetworkObjectGroupMember member : group.getMembers()) {
      IpSpace memberSpace = toMemberIpSpace(member, visited);
      if (memberSpace != null) {
        members.add(memberSpace);
      }
    }
    if (members.isEmpty()) {
      return null;
    }
    return AclIpSpace.union(members);
  }

  private @Nullable IpSpace toMemberIpSpace(
      FtdNetworkObjectGroupMember member, Set<String> visited) {
    switch (member.getType()) {
      case HOST:
        return member.getIp() != null ? member.getIp().toIpSpace() : null;
      case NETWORK_MASK:
        if (member.getIp() == null || member.getMask() == null) {
          return null;
        }
        return Prefix.create(member.getIp(), member.getMask()).toIpSpace();
      case OBJECT:
        return resolveNetworkObjectIpSpace(member.getObjectName(), visited);
      case GROUP_OBJECT:
        return resolveNetworkObjectGroupIpSpace(member.getObjectName(), visited);
    }
    throw new IllegalStateException("Unhandled MemberType: " + member.getType());
  }

  private @Nullable IpProtocol toIpProtocol(@Nullable String protocol) {
    if (protocol == null) {
      return null;
    }
    switch (protocol.toLowerCase()) {
      case "tcp":
        return IpProtocol.TCP;
      case "udp":
        return IpProtocol.UDP;
      case "icmp":
        return IpProtocol.ICMP;
      case "ip":
        return null;
      default:
        return null;
    }
  }

  private @Nullable IntegerSpace toPortSpace(String spec) {
    String trimmed = spec.trim().toLowerCase();
    if (trimmed.startsWith("object-group")) {
      return null;
    }
    String[] parts = trimmed.split("\\s+");
    if (parts.length < 2) {
      return null;
    }
    Integer port = parsePort(parts[1]);
    switch (parts[0]) {
      case "eq":
        return port != null ? IntegerSpace.of(port) : null;
      case "gt":
        return port != null ? IntegerSpace.of(new SubRange(port + 1, 65535)) : null;
      case "lt":
        return port != null ? IntegerSpace.of(new SubRange(0, port - 1)) : null;
      case "neq":
        return port != null
            ? IntegerSpace.builder()
                .including(new SubRange(0, port - 1))
                .including(new SubRange(port + 1, 65535))
                .build()
            : null;
      case "range":
        if (parts.length < 3) {
          return null;
        }
        Integer high = parsePort(parts[2]);
        if (port == null || high == null) {
          return null;
        }
        return IntegerSpace.of(new SubRange(port, high));
      default:
        return null;
    }
  }

  private @Nullable Integer parsePort(String token) {
    try {
      return Integer.parseInt(token);
    } catch (NumberFormatException e) {
      return switch (token) {
        case "domain", "dns" -> 53;
        case "http" -> 80;
        case "https" -> 443;
        case "smtp" -> 25;
        default -> null;
      };
    }
  }

  private static final class ServiceObjectGroupMatch {
    private final @Nullable IntegerSpace _ports;
    private final boolean _anyPort;
    private final @Nonnull Set<IpProtocol> _protocols;

    private ServiceObjectGroupMatch(
        @Nullable IntegerSpace ports, boolean anyPort, @Nonnull Set<IpProtocol> protocols) {
      _ports = ports;
      _anyPort = anyPort;
      _protocols = protocols;
    }

    public @Nullable IntegerSpace getPorts() {
      return _ports;
    }

    public boolean isAnyPort() {
      return _anyPort;
    }

    public @Nonnull Set<IpProtocol> getProtocols() {
      return _protocols;
    }
  }

  private @Nullable ServiceObjectGroupMatch resolveServiceObjectGroupMatch(
      @Nullable String name, Set<String> visited) {
    if (name == null || !visited.add(name)) {
      return null;
    }
    FtdServiceObjectGroup group = _serviceObjectGroups.get(name);
    if (group == null) {
      return null;
    }
    IntegerSpace ports = null;
    boolean anyPort = false;
    Set<IpProtocol> protocols = EnumSet.noneOf(IpProtocol.class);

    if (group.getProtocol() != null) {
      IpProtocol protocol = toIpProtocol(group.getProtocol());
      if (protocol != null) {
        protocols.add(protocol);
      }
    }

    for (FtdServiceObjectGroupMember member : group.getMembers()) {
      switch (member.getType()) {
        case SERVICE_OBJECT:
        case PORT_OBJECT:
          if (member.getProtocol() != null) {
            IpProtocol protocol = toIpProtocol(member.getProtocol());
            if (protocol != null) {
              protocols.add(protocol);
            }
          }
          if (member.getPortSpec() == null) {
            anyPort = true;
          } else {
            IntegerSpace memberPorts = toPortSpace(member.getPortSpec());
            if (memberPorts != null) {
              ports = ports == null ? memberPorts : IntegerSpace.unionOf(ports, memberPorts);
            }
          }
          break;
        case GROUP_OBJECT:
          ServiceObjectGroupMatch nested =
              resolveServiceObjectGroupMatch(member.getObjectName(), visited);
          if (nested != null) {
            protocols.addAll(nested.getProtocols());
            if (nested.isAnyPort()) {
              anyPort = true;
            } else if (nested.getPorts() != null) {
              ports =
                  ports == null
                      ? nested.getPorts()
                      : IntegerSpace.unionOf(ports, nested.getPorts());
            }
          }
          break;
      }
    }

    if (anyPort) {
      ports = null;
    }
    return new ServiceObjectGroupMatch(ports, anyPort, protocols);
  }

  private void applyAccessGroups(Configuration c) {
    if (_accessGroups.isEmpty()) {
      return;
    }
    Map<String, String> nameifToInterface = new HashMap<>();
    _interfaces.forEach(
        (name, repIface) -> {
          if (repIface.getNameif() != null) {
            nameifToInterface.put(repIface.getNameif(), name);
          }
        });

    for (FtdAccessGroup accessGroup : _accessGroups) {
      String aclName = accessGroup.getAclName();
      IpAccessList acl = c.getIpAccessLists().get(aclName);
      if (acl == null) {
        continue;
      }
      if (accessGroup.getDirection().equalsIgnoreCase("global")) {
        continue;
      }
      String interfaceName = accessGroup.getInterfaceName();
      if (interfaceName == null) {
        continue;
      }
      String resolvedInterface = nameifToInterface.getOrDefault(interfaceName, interfaceName);
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(resolvedInterface);
      if (iface == null) {
        continue;
      }
      if (accessGroup.getDirection().equalsIgnoreCase("in")) {
        iface.setIncomingFilter(acl);
        iface.setIncomingFilterName(aclName);
      } else if (accessGroup.getDirection().equalsIgnoreCase("out")) {
        iface.setOutgoingFilter(acl);
        iface.setOutgoingFilterName(aclName);
      }
    }
  }

  private void applyNatRules(Configuration c) {
    if (_natRules.isEmpty()) {
      return;
    }
    Map<String, String> nameifToInterface = new HashMap<>();
    _interfaces.forEach(
        (name, repIface) -> {
          if (repIface.getNameif() != null) {
            nameifToInterface.put(repIface.getNameif(), name);
          }
        });

    Map<String, List<OptionalTransformationBuilder>> incomingByInterface = new HashMap<>();
    Map<String, List<OptionalTransformationBuilder>> outgoingByInterface = new HashMap<>();

    for (FtdNatRule rule : _natRules) {
      String srcInterface =
          nameifToInterface.getOrDefault(rule.getSourceInterface(), rule.getSourceInterface());
      String dstInterface =
          nameifToInterface.getOrDefault(
              rule.getDestinationInterface(), rule.getDestinationInterface());

      boolean isManual = rule.getPosition() != FtdNatRule.NatPosition.AUTO;
      if (isManual
          && rule.getSourceTranslation() != null
          && rule.getDestinationTranslation() != null) {
        Transformation.Builder outgoing = toManualTwiceNatOutgoing(rule, srcInterface);
        if (outgoing != null) {
          outgoingByInterface
              .computeIfAbsent(srcInterface, key -> new ArrayList<>())
              .add(new OptionalTransformationBuilder(outgoing));
        }
        Transformation.Builder incoming = toManualTwiceNatIncoming(rule, dstInterface);
        if (incoming != null) {
          incomingByInterface
              .computeIfAbsent(dstInterface, key -> new ArrayList<>())
              .add(new OptionalTransformationBuilder(incoming));
        }
        continue;
      }

      if (rule.getSourceTranslation() != null) {
        Transformation.Builder t =
            toSourceNatTransformation(
                rule.getSourceTranslation(), rule.getServiceTranslation(), srcInterface);
        OptionalTransformationBuilder builder = new OptionalTransformationBuilder(t);
        outgoingByInterface.computeIfAbsent(srcInterface, key -> new ArrayList<>()).add(builder);
      }
      if (rule.getDestinationTranslation() != null) {
        Transformation.Builder t =
            toDestinationNatTransformation(
                rule.getDestinationTranslation(), rule.getServiceTranslation(), dstInterface);
        OptionalTransformationBuilder builder = new OptionalTransformationBuilder(t);
        incomingByInterface.computeIfAbsent(dstInterface, key -> new ArrayList<>()).add(builder);
      }
    }

    incomingByInterface.forEach(
        (ifaceName, builders) -> {
          org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(ifaceName);
          if (iface == null) {
            return;
          }
          Transformation chain = toTransformationChain(builders);
          iface.setIncomingTransformation(
              chainWithOrElse(chain, iface.getIncomingTransformation()));
        });

    outgoingByInterface.forEach(
        (ifaceName, builders) -> {
          org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(ifaceName);
          if (iface == null) {
            return;
          }
          Transformation chain = toTransformationChain(builders);
          iface.setOutgoingTransformation(
              chainWithOrElse(chain, iface.getOutgoingTransformation()));
        });
  }

  private void applySecurityLevelDefaults(Configuration c) {
    for (Map.Entry<String, Interface> entry : _interfaces.entrySet()) {
      String ifaceName = entry.getKey();
      Interface repIface = entry.getValue();
      if (repIface.getSecurityLevel() == null) {
        continue;
      }
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(ifaceName);
      if (iface == null || iface.getIncomingFilter() != null) {
        continue;
      }
      boolean permit = repIface.getSecurityLevel() > 0;
      String aclName = String.format("~SECURITY_LEVEL_DEFAULT~%s~", ifaceName);
      ExprAclLine line =
          ExprAclLine.builder()
              .setAction(permit ? LineAction.PERMIT : LineAction.DENY)
              .setMatchCondition(new MatchHeaderSpace(HeaderSpace.builder().build()))
              .setName(String.format("security-level %s default", repIface.getSecurityLevel()))
              .build();
      IpAccessList acl =
          IpAccessList.builder().setName(aclName).setLines(ImmutableList.of(line)).build();
      c.getIpAccessLists().put(aclName, acl);
      iface.setIncomingFilter(acl);
      iface.setIncomingFilterName(aclName);
    }
  }

  private static final class OptionalTransformationBuilder {
    private final @Nullable Transformation.Builder _builder;

    private OptionalTransformationBuilder(@Nullable Transformation.Builder builder) {
      _builder = builder;
    }
  }

  private @Nullable Transformation.Builder toSourceNatTransformation(
      FtdNatSource source, @Nullable FtdNatService service, String srcInterface) {
    NatAddressInfo real = resolveNatAddressInfo(source.getReal());
    NatAddressInfo mapped = resolveNatAddressInfo(source.getMapped());
    if (real == null || mapped == null || real._matchSpace == null) {
      return null;
    }
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    TransformationStep ipStep = buildSourceNatStep(real, mapped, source.getType());
    if (ipStep == null) {
      return null;
    }
    steps.add(ipStep);
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();
    conjuncts.add(AclLineMatchExprs.matchSrcInterface(srcInterface));
    conjuncts.add(AclLineMatchExprs.matchSrc(real._matchSpace));
    if (service != null) {
      ServiceTranslation translation = parseServiceTranslation(service);
      if (translation != null
          && translation._real._port != null
          && translation._mapped._port != null) {
        addProtocolMatch(conjuncts, translation.effectiveProtocol());
        conjuncts.add(AclLineMatchExprs.matchSrcPort(translation._real._port));
        steps.add(TransformationStep.assignSourcePort(translation._mapped._port));
      }
    }
    return Transformation.when(AclLineMatchExprs.and(conjuncts.build())).apply(steps.build());
  }

  private @Nullable Transformation.Builder toDestinationNatTransformation(
      FtdNatDestination destination, @Nullable FtdNatService service, String dstInterface) {
    NatAddressInfo real = resolveNatAddressInfo(destination.getReal());
    NatAddressInfo mapped = resolveNatAddressInfo(destination.getMapped());
    if (real == null || mapped == null || mapped._matchSpace == null) {
      return null;
    }
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    TransformationStep ipStep = buildDestinationNatStep(real, mapped);
    if (ipStep == null) {
      return null;
    }
    steps.add(ipStep);
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();
    conjuncts.add(AclLineMatchExprs.matchSrcInterface(dstInterface));
    conjuncts.add(AclLineMatchExprs.matchDst(mapped._matchSpace));
    if (service != null) {
      ServiceTranslation translation = parseServiceTranslation(service);
      if (translation != null
          && translation._real._port != null
          && translation._mapped._port != null) {
        addProtocolMatch(conjuncts, translation.effectiveProtocol());
        conjuncts.add(AclLineMatchExprs.matchDstPort(translation._mapped._port));
        steps.add(TransformationStep.assignDestinationPort(translation._real._port));
      }
    }
    return Transformation.when(AclLineMatchExprs.and(conjuncts.build())).apply(steps.build());
  }

  private @Nullable TransformationStep buildSourceNatStep(
      NatAddressInfo real, NatAddressInfo mapped, FtdNatSource.Type type) {
    if (type == FtdNatSource.Type.STATIC
        && mapped._prefix != null
        && real._prefix != null
        && mapped._prefix.getPrefixLength() == real._prefix.getPrefixLength()
        && mapped._prefix.getPrefixLength() < Prefix.MAX_PREFIX_LENGTH) {
      return TransformationStep.shiftSourceIp(mapped._prefix);
    }
    if (type == FtdNatSource.Type.DYNAMIC && mapped._prefix != null) {
      return TransformationStep.assignSourceIp(
          mapped._prefix.getStartIp(), mapped._prefix.getEndIp());
    }
    Ip mappedIp = mapped.getSingleIp();
    if (mappedIp != null) {
      return TransformationStep.assignSourceIp(mappedIp);
    }
    if (mapped._rangeStart != null && mapped._rangeEnd != null) {
      return TransformationStep.assignSourceIp(mapped._rangeStart, mapped._rangeEnd);
    }
    return null;
  }

  private @Nullable TransformationStep buildDestinationNatStep(
      NatAddressInfo real, NatAddressInfo mapped) {
    if (mapped._prefix != null
        && real._prefix != null
        && mapped._prefix.getPrefixLength() == real._prefix.getPrefixLength()
        && real._prefix.getPrefixLength() < Prefix.MAX_PREFIX_LENGTH) {
      return TransformationStep.shiftDestinationIp(real._prefix);
    }
    Ip realIp = real.getSingleIp();
    if (realIp != null) {
      return TransformationStep.assignDestinationIp(realIp);
    }
    if (real._rangeStart != null && real._rangeEnd != null) {
      return TransformationStep.assignDestinationIp(real._rangeStart, real._rangeEnd);
    }
    return null;
  }

  private @Nullable TransformationStep buildForwardDestinationNatStep(
      NatAddressInfo real, NatAddressInfo mapped) {
    if (mapped._prefix != null
        && real._prefix != null
        && mapped._prefix.getPrefixLength() == real._prefix.getPrefixLength()
        && mapped._prefix.getPrefixLength() < Prefix.MAX_PREFIX_LENGTH) {
      return TransformationStep.shiftDestinationIp(mapped._prefix);
    }
    Ip mappedIp = mapped.getSingleIp();
    if (mappedIp != null) {
      return TransformationStep.assignDestinationIp(mappedIp);
    }
    if (mapped._rangeStart != null && mapped._rangeEnd != null) {
      return TransformationStep.assignDestinationIp(mapped._rangeStart, mapped._rangeEnd);
    }
    return null;
  }

  private @Nullable TransformationStep buildReverseSourceNatStep(
      NatAddressInfo real, NatAddressInfo mapped, FtdNatSource.Type type) {
    if (type != FtdNatSource.Type.STATIC) {
      return null;
    }
    return buildSourceNatStep(mapped, real, type);
  }

  private @Nullable Transformation.Builder toManualTwiceNatOutgoing(
      FtdNatRule rule, String srcInterface) {
    FtdNatSource source = rule.getSourceTranslation();
    FtdNatDestination destination = rule.getDestinationTranslation();
    if (source == null || destination == null) {
      return null;
    }
    NatAddressInfo srcReal = resolveNatAddressInfo(source.getReal());
    NatAddressInfo srcMapped = resolveNatAddressInfo(source.getMapped());
    NatAddressInfo dstReal = resolveNatAddressInfo(destination.getReal());
    NatAddressInfo dstMapped = resolveNatAddressInfo(destination.getMapped());
    if (srcReal == null
        || dstReal == null
        || srcReal._matchSpace == null
        || dstReal._matchSpace == null) {
      return null;
    }
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    if (srcMapped != null) {
      TransformationStep srcStep = buildSourceNatStep(srcReal, srcMapped, source.getType());
      if (srcStep != null) {
        steps.add(srcStep);
      }
    }
    if (dstMapped != null) {
      TransformationStep dstStep = buildForwardDestinationNatStep(dstReal, dstMapped);
      if (dstStep != null) {
        steps.add(dstStep);
      }
    }
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();
    conjuncts.add(AclLineMatchExprs.matchSrcInterface(srcInterface));
    conjuncts.add(AclLineMatchExprs.matchSrc(srcReal._matchSpace));
    conjuncts.add(AclLineMatchExprs.matchDst(dstReal._matchSpace));
    if (rule.getServiceTranslation() != null) {
      ServiceTranslation translation = parseServiceTranslation(rule.getServiceTranslation());
      if (translation != null
          && translation._real._port != null
          && translation._mapped._port != null) {
        addProtocolMatch(conjuncts, translation.effectiveProtocol());
        conjuncts.add(AclLineMatchExprs.matchDstPort(translation._real._port));
        steps.add(TransformationStep.assignDestinationPort(translation._mapped._port));
      }
    }
    ImmutableList<TransformationStep> builtSteps = steps.build();
    if (builtSteps.isEmpty()) {
      return null;
    }
    return Transformation.when(AclLineMatchExprs.and(conjuncts.build())).apply(builtSteps);
  }

  private @Nullable Transformation.Builder toManualTwiceNatIncoming(
      FtdNatRule rule, String dstInterface) {
    FtdNatSource source = rule.getSourceTranslation();
    FtdNatDestination destination = rule.getDestinationTranslation();
    if (source == null || destination == null) {
      return null;
    }
    NatAddressInfo srcReal = resolveNatAddressInfo(source.getReal());
    NatAddressInfo srcMapped = resolveNatAddressInfo(source.getMapped());
    NatAddressInfo dstReal = resolveNatAddressInfo(destination.getReal());
    NatAddressInfo dstMapped = resolveNatAddressInfo(destination.getMapped());
    if (dstMapped == null || dstReal == null || dstMapped._matchSpace == null) {
      return null;
    }
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    if (srcMapped != null && srcReal != null) {
      TransformationStep srcStep = buildReverseSourceNatStep(srcReal, srcMapped, source.getType());
      if (srcStep != null) {
        steps.add(srcStep);
      }
    }
    TransformationStep dstStep = buildDestinationNatStep(dstReal, dstMapped);
    if (dstStep != null) {
      steps.add(dstStep);
    }
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();
    conjuncts.add(AclLineMatchExprs.matchSrcInterface(dstInterface));
    conjuncts.add(AclLineMatchExprs.matchDst(dstMapped._matchSpace));
    if (srcMapped != null && srcMapped._matchSpace != null) {
      conjuncts.add(AclLineMatchExprs.matchSrc(srcMapped._matchSpace));
    }
    if (rule.getServiceTranslation() != null) {
      ServiceTranslation translation = parseServiceTranslation(rule.getServiceTranslation());
      if (translation != null
          && translation._real._port != null
          && translation._mapped._port != null) {
        addProtocolMatch(conjuncts, translation.effectiveProtocol());
        conjuncts.add(AclLineMatchExprs.matchDstPort(translation._mapped._port));
        steps.add(TransformationStep.assignDestinationPort(translation._real._port));
      }
    }
    ImmutableList<TransformationStep> builtSteps = steps.build();
    if (builtSteps.isEmpty()) {
      return null;
    }
    return Transformation.when(AclLineMatchExprs.and(conjuncts.build())).apply(builtSteps);
  }

  private static final class NatAddressInfo {
    private final @Nullable IpSpace _matchSpace;
    private final @Nullable Prefix _prefix;
    private final @Nullable Ip _rangeStart;
    private final @Nullable Ip _rangeEnd;

    private NatAddressInfo(
        @Nullable IpSpace matchSpace,
        @Nullable Prefix prefix,
        @Nullable Ip rangeStart,
        @Nullable Ip rangeEnd) {
      _matchSpace = matchSpace;
      _prefix = prefix;
      _rangeStart = rangeStart;
      _rangeEnd = rangeEnd;
    }

    private @Nullable Ip getSingleIp() {
      if (_prefix != null && _prefix.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
        return _prefix.getStartIp();
      }
      if (_rangeStart != null && _rangeEnd != null && Objects.equals(_rangeStart, _rangeEnd)) {
        return _rangeStart;
      }
      return null;
    }
  }

  private static final class ServiceSpec {
    private final @Nullable IpProtocol _protocol;
    private final @Nullable Integer _port;

    private ServiceSpec(@Nullable IpProtocol protocol, @Nullable Integer port) {
      _protocol = protocol;
      _port = port;
    }
  }

  private static final class ServiceTranslation {
    private final @Nonnull ServiceSpec _real;
    private final @Nonnull ServiceSpec _mapped;

    private ServiceTranslation(ServiceSpec real, ServiceSpec mapped) {
      _real = real;
      _mapped = mapped;
    }

    private @Nullable IpProtocol effectiveProtocol() {
      return _mapped._protocol != null ? _mapped._protocol : _real._protocol;
    }
  }

  private @Nullable ServiceTranslation parseServiceTranslation(FtdNatService service) {
    String realSpec = service.getRealService();
    String mappedSpec = service.getMappedService();
    ServiceSpec real = parseServiceSpec(realSpec);
    ServiceSpec mapped = parseServiceSpec(mappedSpec);
    List<Integer> realPorts = parsePortList(realSpec);
    List<Integer> mappedPorts = parsePortList(mappedSpec);
    if (real == null) {
      real =
          new ServiceSpec(parseProtocol(realSpec), realPorts.isEmpty() ? null : realPorts.get(0));
    }
    if (mapped == null) {
      mapped =
          new ServiceSpec(
              parseProtocol(mappedSpec), mappedPorts.isEmpty() ? null : mappedPorts.get(0));
    }
    if (real._port == null || mapped._port == null) {
      if (realPorts.size() >= 2 && mappedPorts.isEmpty()) {
        real = new ServiceSpec(real._protocol, realPorts.get(0));
        mapped = new ServiceSpec(mapped._protocol, realPorts.get(1));
      } else if (mappedPorts.size() >= 2 && realPorts.isEmpty()) {
        real = new ServiceSpec(real._protocol, mappedPorts.get(0));
        mapped = new ServiceSpec(mapped._protocol, mappedPorts.get(1));
      } else if (!realPorts.isEmpty() && !mappedPorts.isEmpty()) {
        real = new ServiceSpec(real._protocol, realPorts.get(0));
        mapped = new ServiceSpec(mapped._protocol, mappedPorts.get(0));
      }
    }
    return new ServiceTranslation(real, mapped);
  }

  private @Nullable ServiceSpec parseServiceSpec(@Nullable String spec) {
    if (spec == null || spec.isBlank()) {
      return null;
    }
    String[] parts = spec.trim().toLowerCase().split("\\s+");
    if (parts.length == 0) {
      return null;
    }
    int index = 0;
    IpProtocol protocol = null;
    switch (parts[index]) {
      case "tcp":
        protocol = IpProtocol.TCP;
        index++;
        break;
      case "udp":
        protocol = IpProtocol.UDP;
        index++;
        break;
      case "icmp":
        protocol = IpProtocol.ICMP;
        index++;
        break;
      default:
        break;
    }
    if (index >= parts.length) {
      return new ServiceSpec(protocol, null);
    }
    if ("eq".equals(parts[index])) {
      index++;
    }
    if (index >= parts.length) {
      return new ServiceSpec(protocol, null);
    }
    Integer port = parsePort(parts[index]);
    if (port == null) {
      return null;
    }
    return new ServiceSpec(protocol, port);
  }

  private void addProtocolMatch(
      ImmutableList.Builder<AclLineMatchExpr> conjuncts, @Nullable IpProtocol protocol) {
    if (protocol == null) {
      return;
    }
    conjuncts.add(AclLineMatchExprs.matchIpProtocol(protocol));
  }

  private List<Integer> parsePortList(@Nullable String spec) {
    if (spec == null || spec.isBlank()) {
      return List.of();
    }
    String[] parts = spec.trim().toLowerCase().split("\\s+");
    List<Integer> ports = new ArrayList<>();
    for (String part : parts) {
      if ("eq".equals(part) || "range".equals(part)) {
        continue;
      }
      Integer port = parsePort(part);
      if (port != null) {
        ports.add(port);
      }
    }
    return ports;
  }

  private @Nullable IpProtocol parseProtocol(@Nullable String spec) {
    if (spec == null || spec.isBlank()) {
      return null;
    }
    String[] parts = spec.trim().toLowerCase().split("\\s+");
    if (parts.length == 0) {
      return null;
    }
    return switch (parts[0]) {
      case "tcp" -> IpProtocol.TCP;
      case "udp" -> IpProtocol.UDP;
      case "icmp" -> IpProtocol.ICMP;
      default -> null;
    };
  }

  private @Nullable NatAddressInfo resolveNatAddressInfo(FtdNatAddress address) {
    return address.accept(
        new FtdNatAddress.Visitor<>() {
          @Override
          public NatAddressInfo visitFtdNatAddressIp(
              FtdNatAddress.FtdNatAddressIp ftdNatAddressIp) {
            Ip ip = ftdNatAddressIp.getIp();
            return new NatAddressInfo(ip.toIpSpace(), ip.toPrefix(), null, null);
          }

          @Override
          public NatAddressInfo visitFtdNatAddressName(
              FtdNatAddress.FtdNatAddressName ftdNatAddressName) {
            String name = ftdNatAddressName.getName();
            FtdNetworkObject obj = _networkObjects.get(name);
            if (obj == null || obj.getType() == null) {
              return null;
            }
            switch (obj.getType()) {
              case HOST:
                if (obj.getHostIp() == null) {
                  return null;
                }
                Ip hostIp = obj.getHostIp();
                return new NatAddressInfo(hostIp.toIpSpace(), hostIp.toPrefix(), null, null);
              case SUBNET:
                if (obj.getSubnetNetwork() == null || obj.getSubnetMask() == null) {
                  return null;
                }
                Prefix prefix = Prefix.create(obj.getSubnetNetwork(), obj.getSubnetMask());
                return new NatAddressInfo(prefix.toIpSpace(), prefix, null, null);
              case RANGE:
                if (obj.getRangeStart() == null || obj.getRangeEnd() == null) {
                  return null;
                }
                return new NatAddressInfo(
                    IpRange.range(obj.getRangeStart(), obj.getRangeEnd()),
                    null,
                    obj.getRangeStart(),
                    obj.getRangeEnd());
              case FQDN:
                return null;
            }
            throw new IllegalStateException("Unhandled NetworkObjectType: " + obj.getType());
          }
        });
  }

  private @Nullable Transformation toTransformationChain(
      List<OptionalTransformationBuilder> builders) {
    Transformation previous = null;
    for (int i = builders.size() - 1; i >= 0; i--) {
      OptionalTransformationBuilder wrapper = builders.get(i);
      if (wrapper._builder != null) {
        previous = wrapper._builder.setOrElse(previous).build();
      }
    }
    return previous;
  }

  private @Nullable Transformation chainWithOrElse(
      @Nullable Transformation head, @Nullable Transformation orElse) {
    if (head == null) {
      return orElse;
    }
    if (orElse == null) {
      return head;
    }
    if (head.getOrElse() == null) {
      return new Transformation(
          head.getGuard(), head.getTransformationSteps(), head.getAndThen(), orElse);
    }
    return new Transformation(
        head.getGuard(),
        head.getTransformationSteps(),
        head.getAndThen(),
        chainWithOrElse(head.getOrElse(), orElse));
  }

  private void convertOspfProcesses(Configuration c) {
    _ospfProcesses.forEach(
        (name, ospf) -> {
          c.getVrfs()
              .computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new)
              .addOspfProcess(toOspfProcess(name, ospf));
        });
  }

  private OspfProcess toOspfProcess(String name, FtdOspfProcess ospf) {
    // Create OSPF process
    OspfProcess.Builder ospfBuilder =
        OspfProcess.builder()
            .setProcessId(name)
            .setRouterId(ospf.getRouterId() != null ? ospf.getRouterId() : Ip.ZERO)
            .setReferenceBandwidth(100_000_000.0);

    // Convert networks to areas
    Map<Long, OspfArea.Builder> areas = new HashMap<>();
    for (FtdOspfNetwork network : ospf.getNetworks()) {
      long areaId = network.getAreaId();
      areas.computeIfAbsent(areaId, k -> OspfArea.builder().setNumber(k));
    }

    // Build areas
    Map<Long, OspfArea> builtAreas =
        areas.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build()));

    ospfBuilder.setAreas(builtAreas);

    return ospfBuilder.build();
  }

  private void convertInterface(
      org.batfish.vendor.cisco_ftd.representation.Interface repIface, Configuration c) {
    String ifName = repIface.getName();
    org.batfish.datamodel.Interface.Builder ib =
        org.batfish.datamodel.Interface.builder()
            .setOwner(c)
            .setName(ifName)
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(repIface.getActive());

    if (repIface.getDescription() != null) {
      ib.setDescription(repIface.getDescription());
    }

    if (repIface.getAddress() != null) {
      ConcreteInterfaceAddress cia = repIface.getAddress();
      ib.setAddress(cia);
    }

    String nameif = repIface.getNameif();
    if (nameif != null) {
      ib.setZoneName(nameif);
    }

    if (repIface.getMtu() != null) {
      ib.setMtu(repIface.getMtu());
    }

    org.batfish.datamodel.Interface newIface = ib.build();

    // Set VRF following the Cisco parser pattern
    String vrfName = repIface.getVrf() != null ? repIface.getVrf() : Configuration.DEFAULT_VRF_NAME;
    Vrf vrf = c.getVrfs().computeIfAbsent(vrfName, Vrf::new);
    newIface.setVrf(vrf);

    _cryptoMapInterfaceBindings.forEach(
        (cryptoMap, ifaceNames) -> {
          if (ifaceNames.contains(ifName) || (nameif != null && ifaceNames.contains(nameif))) {
            newIface.setCryptoMap(cryptoMap);
          }
        });
    c.getAllInterfaces().put(ifName, newIface);
  }

  private void applyMpfMetadata(Configuration c) {
    if (_classMaps.isEmpty() && _policyMaps.isEmpty() && _servicePolicies.isEmpty()) {
      return;
    }
    CiscoFamily cisco = c.getVendorFamily().getCisco();
    if (cisco == null) {
      cisco = new CiscoFamily();
      c.getVendorFamily().setCisco(cisco);
    }
    Service mpfService = cisco.getServices().computeIfAbsent("mpf", k -> new Service());

    if (!_classMaps.isEmpty()) {
      Service classMaps =
          mpfService.getSubservices().computeIfAbsent("class-map", k -> new Service());
      _classMaps.keySet().forEach(name -> classMaps.getSubservices().put(name, new Service()));
    }
    if (!_policyMaps.isEmpty()) {
      Service policyMaps =
          mpfService.getSubservices().computeIfAbsent("policy-map", k -> new Service());
      _policyMaps.keySet().forEach(name -> policyMaps.getSubservices().put(name, new Service()));
    }
    if (!_servicePolicies.isEmpty()) {
      Service servicePolicies =
          mpfService.getSubservices().computeIfAbsent("service-policy", k -> new Service());
      _servicePolicies.forEach(
          policy -> servicePolicies.getSubservices().put(policy.getPolicyMapName(), new Service()));
    }
  }

  private void convertMpfToAcls(Configuration c) {
    if (_classMaps.isEmpty() && _policyMaps.isEmpty()) {
      return;
    }

    _classMaps.forEach(
        (name, classMap) -> {
          List<AclLine> lines = new ArrayList<>();
          if (!classMap.getAccessListReferences().isEmpty()) {
            for (String aclName : classMap.getAccessListReferences()) {
              if (!c.getIpAccessLists().containsKey(aclName)) {
                continue;
              }
              lines.add(
                  ExprAclLine.builder()
                      .setAction(LineAction.PERMIT)
                      .setMatchCondition(new PermittedByAcl(aclName))
                      .setName(String.format("class-map %s match access-list %s", name, aclName))
                      .build());
            }
          } else if (classMap.getMatchLines().stream()
              .anyMatch(line -> line.equalsIgnoreCase("match default-inspection-traffic"))) {
            lines.add(
                ExprAclLine.builder()
                    .setAction(LineAction.PERMIT)
                    .setMatchCondition(new MatchHeaderSpace(HeaderSpace.builder().build()))
                    .setName(String.format("class-map %s match default-inspection-traffic", name))
                    .build());
          }
          if (!lines.isEmpty()) {
            String aclName = computeClassMapAclName(name);
            c.getIpAccessLists()
                .put(aclName, IpAccessList.builder().setName(aclName).setLines(lines).build());
          }
        });

    _policyMaps.forEach(
        (name, policyMap) -> {
          List<AclLine> lines = new ArrayList<>();
          for (String className : policyMap.getClassNames()) {
            String classMapAclName = computeClassMapAclName(className);
            if (!c.getIpAccessLists().containsKey(classMapAclName)) {
              continue;
            }
            lines.add(
                ExprAclLine.builder()
                    .setAction(LineAction.PERMIT)
                    .setMatchCondition(new PermittedByAcl(classMapAclName))
                    .setName(String.format("policy-map %s class %s", name, className))
                    .build());
          }
          if (!lines.isEmpty()) {
            String aclName = computePolicyMapAclName(name);
            c.getIpAccessLists()
                .put(aclName, IpAccessList.builder().setName(aclName).setLines(lines).build());
          }
        });
  }

  private static @Nonnull String computeClassMapAclName(@Nonnull String classMapName) {
    return String.format("~FTD_CLASS_MAP_ACL~%s~", classMapName);
  }

  private static @Nonnull String computePolicyMapAclName(@Nonnull String policyMapName) {
    return String.format("~FTD_POLICY_MAP_ACL~%s~", policyMapName);
  }
}
