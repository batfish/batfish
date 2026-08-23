package org.batfish.vendor.aruba_aoscx.representation;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.generatedOspfExportPolicyName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.Ip6AccessListLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.StaticRoute6;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.batfish.datamodel.ospf.StubSettings;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific configuration for Aruba AOS-CX. */
public class AosCxConfiguration extends VendorConfiguration {

  private transient Configuration _c;
  private String _hostname;
  private final Map<String, AosCxInterface> _interfaces = new HashMap<>();
  private final Map<String, AosCxIpAccessList> _ipAccessLists = new HashMap<>();
  private final Map<String, AosCxIpv6AccessList> _ipv6AccessLists =
      new HashMap<>();
  private final Map<Integer, AosCxOspfProcess> _ospfProcesses = new HashMap<>();
  private final Map<Integer, AosCxOspfv3Process> _ospfv3Processes =
      new HashMap<>();
  private final Map<String, Map<Integer, AosCxOspfv3Process>>
      _ospfv3ProcessesByVrf = new HashMap<>();
  private final Map<String, Map<Integer, AosCxOspfProcess>> _ospfProcessesByVrf =
      new HashMap<>();
  private final Map<String, AosCxPrefixList> _prefixLists = new HashMap<>();
  private final Map<String, AosCxRouteMap> _routeMaps = new HashMap<>();
  private final Set<String> _vrfs = new HashSet<>();
  private AosCxBgpProcess _bgpProcess;
  private final Map<String, AosCxBgpProcess> _bgpProcessesByVrf =
      new HashMap<>();
  private String _rawHostname;
  private final List<AosCxStaticRoute> _staticRoutes = new ArrayList<>();
  private final List<AosCxStaticRoute6> _staticRoutes6 = new ArrayList<>();
  private ConfigurationFormat _vendor;

  @Override
  public String getHostname() {
    return _hostname;
  }

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  public Map<String, AosCxInterface> getInterfaces() {
    return _interfaces;
  }

  public AosCxInterface getOrCreateInterface(String name) {
    return _interfaces.computeIfAbsent(name, AosCxInterface::new);
  }

  public Map<String, AosCxIpAccessList> getIpAccessLists() {
    return _ipAccessLists;
  }

  public AosCxIpAccessList getOrCreateIpAccessList(String name) {
    return _ipAccessLists.computeIfAbsent(name, AosCxIpAccessList::new);
  }

  public Map<String, AosCxIpv6AccessList>
      getIpv6AccessLists() {
    return _ipv6AccessLists;
  }

  public AosCxIpv6AccessList
      getOrCreateIpv6AccessList(String name) {
    return _ipv6AccessLists.computeIfAbsent(
        name,
        AosCxIpv6AccessList::new);
  }

  public AosCxBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public AosCxBgpProcess getBgpProcess(String vrfName) {
    if (vrfName == null || vrfName.equals(DEFAULT_VRF_NAME)) {
      return _bgpProcess;
    }
    return _bgpProcessesByVrf.get(vrfName);
  }

  public AosCxBgpProcess getOrCreateBgpProcess(long localAs) {
    return getOrCreateBgpProcess(localAs, null);
  }

  public AosCxBgpProcess getOrCreateBgpProcess(
      long localAs, String vrfName) {
    if (vrfName == null || vrfName.equals(DEFAULT_VRF_NAME)) {
      if (_bgpProcess == null) {
        _bgpProcess = new AosCxBgpProcess(localAs);
      }
      return _bgpProcess;
    }

    addVrf(vrfName);
    return _bgpProcessesByVrf.computeIfAbsent(
        vrfName, name -> new AosCxBgpProcess(localAs));
  }

  public Map<String, AosCxRouteMap> getRouteMaps() {
    return _routeMaps;
  }

  public Set<String> getVrfs() {
    return _vrfs;
  }

  public void addVrf(String name) {
    _vrfs.add(name);
  }

  public AosCxRouteMap getOrCreateRouteMap(String name) {
    return _routeMaps.computeIfAbsent(name, AosCxRouteMap::new);
  }

  public Map<String, AosCxPrefixList> getPrefixLists() {
    return _prefixLists;
  }

  public AosCxPrefixList getOrCreatePrefixList(String name) {
    return _prefixLists.computeIfAbsent(name, AosCxPrefixList::new);
  }

  public Map<Integer, AosCxOspfProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  public Map<Integer, AosCxOspfv3Process> getOspfv3Processes() {
    return _ospfv3Processes;
  }

  public Map<Integer, AosCxOspfv3Process> getOspfv3Processes(
      String vrfName) {
    if (vrfName == null
        || vrfName.equals(DEFAULT_VRF_NAME)) {
      return _ospfv3Processes;
    }

    return _ospfv3ProcessesByVrf.getOrDefault(
        vrfName, Map.of());
  }

  public AosCxOspfv3Process getOrCreateOspfv3Process(
      int processId) {
    return getOrCreateOspfv3Process(
        processId, null);
  }

  public AosCxOspfv3Process getOrCreateOspfv3Process(
      int processId,
      String vrfName) {
    if (vrfName == null
        || vrfName.equals(DEFAULT_VRF_NAME)) {
      return _ospfv3Processes.computeIfAbsent(
          processId,
          AosCxOspfv3Process::new);
    }

    addVrf(vrfName);

    return _ospfv3ProcessesByVrf
        .computeIfAbsent(
            vrfName,
            name -> new HashMap<>())
        .computeIfAbsent(
            processId,
            AosCxOspfv3Process::new);
  }

  public Map<Integer, AosCxOspfProcess> getOspfProcesses(String vrfName) {
    if (vrfName == null || vrfName.equals(DEFAULT_VRF_NAME)) {
      return _ospfProcesses;
    }
    return _ospfProcessesByVrf.getOrDefault(vrfName, Map.of());
  }

  public AosCxOspfProcess getOrCreateOspfProcess(int processId) {
    return getOrCreateOspfProcess(processId, null);
  }

  public AosCxOspfProcess getOrCreateOspfProcess(
      int processId, String vrfName) {
    if (vrfName == null || vrfName.equals(DEFAULT_VRF_NAME)) {
      return _ospfProcesses.computeIfAbsent(
          processId, AosCxOspfProcess::new);
    }

    addVrf(vrfName);
    return _ospfProcessesByVrf
        .computeIfAbsent(vrfName, name -> new HashMap<>())
        .computeIfAbsent(processId, AosCxOspfProcess::new);
  }

  public List<AosCxStaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public List<AosCxStaticRoute6> getStaticRoutes6() {
    return _staticRoutes6;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  private static InterfaceType getInterfaceType(AosCxInterface iface) {
    String name = iface.getName();
    if (name.startsWith("loopback ")) {
      return InterfaceType.LOOPBACK;
    }
    if (name.startsWith("vlan ")) {
      return InterfaceType.VLAN;
    }
    if (name.startsWith("lag ")) {
      return InterfaceType.AGGREGATED;
    }
    return InterfaceType.PHYSICAL;
  }

  private static boolean getInterfaceAdminUpEffective(AosCxInterface iface) {
    if (iface.getEnabled() != null) {
      return iface.getEnabled();
    }
    // The dedicated management interface is enabled by default.
    if (iface.getName().equals("mgmt")) {
      return true;
    }
    // Physical AOS-CX interfaces are disabled by default. Loopback and VLAN
    // interfaces commonly appear enabled without an explicit "no shutdown".
    return getInterfaceType(iface) != InterfaceType.PHYSICAL;
  }

  private static String getInterfaceVrfName(AosCxInterface iface) {
    return iface.getVrfName() == null
        ? DEFAULT_VRF_NAME
        : iface.getVrfName();
  }

  private void convertInterface(AosCxInterface iface) {
    String name = iface.getName();
    String vrfName = getInterfaceVrfName(iface);
    Vrf vrf = _c.getVrfs().get(vrfName);
    if (vrf == null) {
      vrf = _c.getDefaultVrf();
    }

    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setAdminUp(getInterfaceAdminUpEffective(iface))
            .setBandwidth(iface.getBandwidth())
            .setChannelGroup(iface.getLagName())
            .setDescription(iface.getDescription())
            .setType(getInterfaceType(iface))
            .setName(name)
            .setVrf(vrf)
            .setOwner(_c);

    newIface.setHumanName(name);
    newIface.setDeclaredNames(ImmutableList.of(name));

    // AOS-CX distinguishes overall interface MTU from IP MTU.
    // Batfish has a single interface MTU, so prefer the effective
    // L3 IP MTU when explicitly configured.
    if (iface.getIpMtu() != null) {
      newIface.setMtu(iface.getIpMtu());
    } else if (iface.getMtu() != null) {
      newIface.setMtu(iface.getMtu());
    }

    if (Boolean.TRUE.equals(iface.getSwitchport())) {
      newIface.setSwitchport(true);

      // Presence of trunk-specific configuration identifies this as a trunk.
      if (iface.getAllowedVlans() != null || iface.getNativeVlan() != null) {
        newIface.setSwitchportMode(SwitchportMode.TRUNK);
      }

      if (iface.getAllowedVlans() != null) {
        newIface.setAllowedVlans(iface.getAllowedVlans());
      }

      // Batfish nativeVlan represents an untagged native VLAN.
      // AOS-CX "vlan trunk native <id> tag" carries that VLAN tagged,
      // so model it only through allowedVlans.
      if (iface.getNativeVlan() != null && !iface.getNativeVlanTagged()) {
        newIface.setNativeVlan(iface.getNativeVlan());
      }
    }

    if (getInterfaceType(iface) == InterfaceType.VLAN) {
      newIface.setVlan(Integer.parseInt(name.substring("vlan ".length())));
    }

    List<InterfaceAddress> addresses = new ArrayList<>();
    if (iface.getAddress() != null) {
      // Preserve the existing IPv4 address as primary when dual-stack.
      addresses.add(iface.getAddress());
    }
    addresses.addAll(iface.getIpv6Addresses());

    if (!addresses.isEmpty()) {
      newIface.setAddresses(
          addresses.get(0),
          addresses.subList(1, addresses.size()));
    }

    if (iface.getIncomingAcl() != null) {
      IpAccessList acl = _c.getIpAccessLists().get(iface.getIncomingAcl());
      if (acl != null) {
        newIface.setIncomingFilter(acl);
      }
    }

    if (iface.getOutgoingAcl() != null) {
      IpAccessList acl = _c.getIpAccessLists().get(iface.getOutgoingAcl());
      if (acl != null) {
        newIface.setOutgoingFilter(acl);
      }
    }

    if (iface.getIncomingIpv6Acl() != null) {
      Ip6AccessList acl =
          _c.getIp6AccessLists().get(
              iface.getIncomingIpv6Acl());
      if (acl != null) {
        newIface.setIncomingFilter6(acl);
      }
    }

    if (iface.getOutgoingIpv6Acl() != null) {
      Ip6AccessList acl =
          _c.getIp6AccessLists().get(
              iface.getOutgoingIpv6Acl());
      if (acl != null) {
        newIface.setOutgoingFilter6(acl);
      }
    }

    newIface.build();
  }


  private void finalizeLagMembership() {
    _interfaces.values().stream()
        .filter(iface -> getInterfaceType(iface) == InterfaceType.AGGREGATED)
        .forEach(
            aggregate -> {
              Interface viAggregate =
                  _c.getAllInterfaces().get(aggregate.getName());
              if (viAggregate == null) {
                return;
              }

              Set<String> members = new HashSet<>();
              Set<Interface.Dependency> dependencies = new HashSet<>();

              _interfaces.values().stream()
                  .filter(member -> aggregate.getName().equals(member.getLagName()))
                  .forEach(
                      member -> {
                        members.add(member.getName());
                        dependencies.add(
                            new Interface.Dependency(
                                member.getName(),
                                Interface.DependencyType.AGGREGATE));
                      });

              viAggregate.setChannelGroupMembers(members);
              viAggregate.setDependencies(dependencies);
            });
  }

  private static IpSpace toAclIpSpace(String text) {
    if (text.equalsIgnoreCase("any")) {
      return UniverseIpSpace.INSTANCE;
    }

    if (text.contains("/")) {
      String[] parts = text.split("/", 2);
      if (parts[1].contains(".")) {
        return Prefix.create(Ip.parse(parts[0]), Ip.parse(parts[1])).toIpSpace();
      }
      return Prefix.parse(text).toIpSpace();
    }

    return Ip.parse(text).toIpSpace();
  }

  private static IntegerSpace toPortSpace(AosCxPortSpec portSpec) {
    int first = portSpec.getFirst();

    return switch (portSpec.getOperator()) {
      case EQ -> IntegerSpace.of(first);
      case GT ->
          first >= 65535
              ? IntegerSpace.EMPTY
              : IntegerSpace.of(new SubRange(first + 1, 65535));
      case LT ->
          first <= 0
              ? IntegerSpace.EMPTY
              : IntegerSpace.of(new SubRange(0, first - 1));
      case RANGE -> {
        Integer second = portSpec.getSecond();
        yield second == null
            ? IntegerSpace.EMPTY
            : IntegerSpace.of(new SubRange(first, second));
      }
    };
  }

  private static AclLineMatchExpr toAclProtocolMatch(String protocol) {
    if (protocol.equalsIgnoreCase("any") || protocol.equalsIgnoreCase("ip")) {
      return AclLineMatchExprs.TRUE;
    }

    try {
      IpProtocol ipProtocol =
          protocol.equalsIgnoreCase("ah")
              ? IpProtocol.AHP
              : IpProtocol.fromString(protocol);
      return AclLineMatchExprs.matchIpProtocol(ipProtocol);
    } catch (RuntimeException e) {
      // Never turn an unsupported protocol into a match-all ACE.
      return AclLineMatchExprs.FALSE;
    }
  }

  private void convertIpAccessLists() {
    _ipAccessLists.values().forEach(
        acl -> {
          List<AclLine> lines = new ArrayList<>();

          acl.getEntries().values().forEach(
              entry -> {
                List<AclLineMatchExpr> conditions = new ArrayList<>();
                conditions.add(toAclProtocolMatch(entry.getProtocol()));
                conditions.add(
                    AclLineMatchExprs.matchSrc(toAclIpSpace(entry.getSource())));
                conditions.add(
                    AclLineMatchExprs.matchDst(toAclIpSpace(entry.getDestination())));

                if (entry.getSourcePort() != null) {
                  conditions.add(
                      AclLineMatchExprs.matchSrcPort(
                          toPortSpace(entry.getSourcePort())));
                }

                if (entry.getDestinationPort() != null) {
                  conditions.add(
                      AclLineMatchExprs.matchDstPort(
                          toPortSpace(entry.getDestinationPort())));
                }

                AclLineMatchExpr matchCondition =
                    AclLineMatchExprs.and(conditions);

                lines.add(
                    ExprAclLine.builder()
                        .setName(Long.toString(entry.getSequence()))
                        .setAction(entry.getAction())
                        .setMatchCondition(matchCondition)
                        .build());
              });

          IpAccessList.builder()
              .setName(acl.getName())
              .setOwner(_c)
              .setLines(lines)
              .build();
        });
  }

  private static Prefix6 toAclIp6Prefix(
      String text) {
    if (text.equalsIgnoreCase("any")) {
      return null;
    }

    if (text.contains("/")) {
      return Prefix6.parse(text);
    }

    return Ip6.parse(text).toPrefix6();
  }

  private static boolean isIpv6AnyProtocol(
      String protocol) {
    return protocol.equalsIgnoreCase("any")
        || protocol.equalsIgnoreCase("ipv6");
  }

  private static Optional<IpProtocol>
      toIpv6AclProtocol(String protocol) {
    try {
      if (protocol.equalsIgnoreCase("icmpv6")) {
        return Optional.of(
            IpProtocol.IPV6_ICMP);
      }

      if (protocol.equalsIgnoreCase("ah")) {
        return Optional.of(
            IpProtocol.AHP);
      }

      return Optional.of(
          IpProtocol.fromString(protocol));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  private static Optional<SubRange>
      toIpv6AclPortRange(
          AosCxPortSpec portSpec) {
    int first = portSpec.getFirst();

    return switch (portSpec.getOperator()) {
      case EQ ->
          first >= 0 && first <= 65535
              ? Optional.of(
                  SubRange.singleton(first))
              : Optional.empty();

      case GT ->
          first >= 0 && first < 65535
              ? Optional.of(
                  new SubRange(
                      first + 1, 65535))
              : Optional.empty();

      case LT ->
          first > 0 && first <= 65535
              ? Optional.of(
                  new SubRange(
                      0, first - 1))
              : Optional.empty();

      case RANGE -> {
        Integer second =
            portSpec.getSecond();

        yield second != null
                && first >= 0
                && first <= second
                && second <= 65535
            ? Optional.of(
                new SubRange(
                    first, second))
            : Optional.empty();
      }
    };
  }

  private void convertIpv6AccessLists() {
    _ipv6AccessLists.values().forEach(
        acl -> {
          List<Ip6AccessListLine> lines =
              new ArrayList<>();

          acl.getEntries().values().forEach(
              entry -> {
                Ip6AccessListLine.Builder line =
                    Ip6AccessListLine.builder()
                        .setName(
                            Long.toString(
                                entry.getSequence()))
                        .setAction(
                            entry.getAction())
                        .setSrcPrefix(
                            toAclIp6Prefix(
                                entry.getSource()))
                        .setDstPrefix(
                            toAclIp6Prefix(
                                entry.getDestination()));

                if (!isIpv6AnyProtocol(
                    entry.getProtocol())) {
                  Optional<IpProtocol> protocol =
                      toIpv6AclProtocol(
                          entry.getProtocol());

                  if (protocol.isEmpty()) {
                    return;
                  }

                  line.setProtocol(
                      protocol.get());
                }

                if (entry.getSourcePort()
                    != null) {
                  Optional<SubRange> ports =
                      toIpv6AclPortRange(
                          entry.getSourcePort());

                  if (ports.isEmpty()) {
                    return;
                  }

                  line.setSrcPorts(
                      ports.get());
                }

                if (entry.getDestinationPort()
                    != null) {
                  Optional<SubRange> ports =
                      toIpv6AclPortRange(
                          entry.getDestinationPort());

                  if (ports.isEmpty()) {
                    return;
                  }

                  line.setDstPorts(
                      ports.get());
                }

                lines.add(line.build());
              });

          _c.getIp6AccessLists().put(
              acl.getName(),
              Ip6AccessList.builder()
                  .setName(acl.getName())
                  .setLines(lines)
                  .build());
        });
  }

  private static long toOspfAreaNumber(String area) {
    return area.contains(".") ? Ip.parse(area).asLong() : Long.parseLong(area);
  }

  private void convertPrefixLists() {
    _prefixLists.values().forEach(
        prefixList -> {
          List<RouteFilterLine> lines = new ArrayList<>();

          prefixList.getEntries().values().forEach(
              entry -> {
                int prefixLength = entry.getPrefix().getPrefixLength();

                int minLength =
                    entry.getGe() != null
                        ? entry.getGe()
                        : prefixLength;

                int maxLength =
                    entry.getLe() != null
                        ? entry.getLe()
                        : entry.getGe() != null
                            ? Prefix.MAX_PREFIX_LENGTH
                            : prefixLength;

                lines.add(
                    new RouteFilterLine(
                        entry.getAction(),
                        entry.getPrefix(),
                        new SubRange(minLength, maxLength)));
              });

          _c.getRouteFilterLists()
              .put(
                  prefixList.getName(),
                  new RouteFilterList(prefixList.getName(), lines));
        });
  }

  private static List<Statement> routeMapActionStatements(LineAction action) {
    return action == LineAction.PERMIT
        ? ImmutableList.of(
            new If(
                BooleanExprs.CALL_EXPR_CONTEXT,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ExitAccept.toStaticStatement())))
        : ImmutableList.of(
            new If(
                BooleanExprs.CALL_EXPR_CONTEXT,
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
                ImmutableList.of(Statements.ExitReject.toStaticStatement())));
  }

  private void convertRouteMaps() {
    _routeMaps.values().forEach(
        routeMap -> {
          RoutingPolicy.Builder policy =
              RoutingPolicy.builder().setOwner(_c).setName(routeMap.getName());

          routeMap.getEntries().values().forEach(
              entry -> {
                BooleanExpr guard;

                if (entry.getMatchPrefixList() == null) {
                  guard = BooleanExprs.TRUE;
                } else if (_c.getRouteFilterLists().containsKey(entry.getMatchPrefixList())) {
                  guard =
                      new MatchPrefixSet(
                          DestinationNetwork.instance(),
                          new NamedPrefixSet(entry.getMatchPrefixList()));
                } else {
                  // An undefined prefix-list must never turn into a match-all entry.
                  guard = BooleanExprs.FALSE;
                }

                List<Statement> trueStatements = new ArrayList<>();

                if (entry.getSetLocalPreference() != null) {
                  trueStatements.add(
                      new SetLocalPreference(
                          new LiteralLong(entry.getSetLocalPreference())));
                }

                trueStatements.addAll(routeMapActionStatements(entry.getAction()));

                policy.addStatement(new If(guard, trueStatements));
              });

          // AOS-CX route-map fall-through is an implicit deny.
          policy
              .addStatement(
                  new If(
                      BooleanExprs.CALL_EXPR_CONTEXT,
                      ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
                      ImmutableList.of(Statements.ExitReject.toStaticStatement())))
              .build();
        });
  }

  private void convertBgpProcess(
      Vrf vrf, AosCxBgpProcess process) {
    if (process == null || process.getRouterId() == null) {
      return;
    }

    BgpProcess viProcess =
        BgpProcess.builder()
            .setRouterId(process.getRouterId())
            .setEbgpAdminCost(20)
            .setIbgpAdminCost(200)
            .setLocalAdminCost(200)
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setVrf(vrf)
            .build();

    process.getNeighbors().values().forEach(
        neighbor -> {
          if (neighbor.getRemoteAs() == null) {
            return;
          }

          BgpActivePeerConfig.Builder peer =
              BgpActivePeerConfig.builder()
                  .setBgpProcess(viProcess)
                  .setLocalAs(process.getLocalAs())
                  .setPeerAddress(neighbor.getIp())
                  .setRemoteAs(neighbor.getRemoteAs());

          if (neighbor.getIpv4UnicastActive()) {
            Ipv4UnicastAddressFamily.Builder addressFamily =
                Ipv4UnicastAddressFamily.builder();

            if (neighbor.getRouteMapIn() != null
                && _c.getRoutingPolicies()
                    .containsKey(neighbor.getRouteMapIn())) {
              addressFamily.setImportPolicy(neighbor.getRouteMapIn());
            }

            if (neighbor.getRouteMapOut() != null
                && _c.getRoutingPolicies()
                    .containsKey(neighbor.getRouteMapOut())) {
              addressFamily.setExportPolicy(neighbor.getRouteMapOut());
            }

            peer.setIpv4UnicastAddressFamily(addressFamily.build());
          }

          peer.build();
        });
  }

  private void convertBgpProcesses() {
    convertBgpProcess(_c.getDefaultVrf(), _bgpProcess);

    _bgpProcessesByVrf.forEach(
        (vrfName, process) -> {
          Vrf vrf = _c.getVrfs().get(vrfName);
          if (vrf != null) {
            convertBgpProcess(vrf, process);
          }
        });
  }

  private void convertOspfProcessesForVrf(
      String vrfName, Map<Integer, AosCxOspfProcess> processes) {
    Vrf vrf = _c.getVrfs().get(vrfName);
    if (vrf == null) {
      return;
    }

    processes.values().forEach(
        process -> {
          // Automatic router-ID selection is not implemented yet.
          if (process.getRouterId() == null) {
            return;
          }

          OspfProcess viProcess =
              OspfProcess.builder()
                  .setProcessId(Integer.toString(process.getProcessId()))
                  .setRouterId(process.getRouterId())
                  .setReferenceBandwidth(100_000_000_000D)
                  .setAllAdminCosts(110)
                  .setVrf(vrf)
                  .build();

          if (process.getRedistributeConnected()) {
            String exportPolicyName =
                generatedOspfExportPolicyName(
                    vrfName, Integer.toString(process.getProcessId()));

            RoutingPolicy.builder()
                .setOwner(_c)
                .setName(exportPolicyName)
                .addStatement(
                    new If(
                        new MatchProtocol(RoutingProtocol.CONNECTED),
                        ImmutableList.of(
                            new SetOspfMetricType(OspfMetricType.E2),
                            new SetMetric(new LiteralLong(25L)),
                            Statements.ExitAccept.toStaticStatement()),
                        ImmutableList.of(
                            Statements.ExitReject.toStaticStatement())))
                .build();

            viProcess.setExportPolicy(exportPolicyName);
          }

          Map<Long, List<String>> areaInterfaces = new HashMap<>();
          _interfaces.values().stream()
              .filter(
                  iface ->
                      getInterfaceVrfName(iface).equals(vrfName)
                          && iface.getOspfProcessId() != null
                          && iface.getOspfProcessId()
                              == process.getProcessId()
                          && iface.getOspfArea() != null)
              .forEach(
                  iface ->
                      areaInterfaces
                          .computeIfAbsent(
                              toOspfAreaNumber(iface.getOspfArea()),
                              area -> new ArrayList<>())
                          .add(iface.getName()));

          Map<Long, Boolean> stubAreas = new HashMap<>();

          process
              .getStubAreas()
              .forEach(
                  (areaId, suppressType3) -> {
                    long area = toOspfAreaNumber(areaId);
                    stubAreas.put(area, suppressType3);
                    areaInterfaces.computeIfAbsent(
                        area, ignored -> new ArrayList<>());
                  });

          areaInterfaces.forEach(
              (area, interfaces) -> {
                OspfArea.Builder areaBuilder =
                    OspfArea.builder()
                        .setNumber(area)
                        .addInterfaces(interfaces)
                        .setOspfProcess(viProcess);

                Boolean suppressType3 = stubAreas.get(area);
                if (suppressType3 != null) {
                  areaBuilder
                      .setStub(
                          StubSettings.builder()
                              .setSuppressType3(suppressType3)
                              .build())
                      // AOS-CX default cost for the stub default route is 1.
                      .setMetricOfDefaultRoute(1);
                }

                areaBuilder.build();
              });
        });
  }

  private void convertOspfProcesses() {
    convertOspfProcessesForVrf(DEFAULT_VRF_NAME, _ospfProcesses);
    _ospfProcessesByVrf.forEach(this::convertOspfProcessesForVrf);
  }

  /**
   * Compute the dynamic AOS-CX OSPFv3 router ID for a VRF.
   *
   * <p>AOS-CX prefers the highest IPv4 address on an active loopback.
   * If no active loopback has an IPv4 address, it uses the highest IPv4
   * address on an active interface in the VRF.
   */
  private Ip getDynamicOspfv3RouterId(
      String vrfName) {
    Ip highestLoopback = null;
    Ip highestInterface = null;

    for (AosCxInterface iface :
        _interfaces.values()) {

      if (!getInterfaceVrfName(iface)
              .equals(vrfName)
          || !getInterfaceAdminUpEffective(iface)
          || iface.getAddress() == null) {
        continue;
      }

      Ip candidate =
          iface.getAddress().getIp();

      if (highestInterface == null
          || candidate.asLong()
              > highestInterface.asLong()) {
        highestInterface = candidate;
      }

      if (getInterfaceType(iface)
              == InterfaceType.LOOPBACK
          && (highestLoopback == null
              || candidate.asLong()
                  > highestLoopback.asLong())) {
        highestLoopback = candidate;
      }
    }

    return highestLoopback != null
        ? highestLoopback
        : highestInterface;
  }

  private void convertOspfv3ProcessesForVrf(
      String vrfName,
      Map<Integer, AosCxOspfv3Process> processes) {
    Vrf vrf =
        _c.getVrfs().get(vrfName);

    if (vrf == null) {
      return;
    }

    processes.values().forEach(
        process -> {
          Ip routerId =
              process.getRouterId() != null
                  ? process.getRouterId()
                  : getDynamicOspfv3RouterId(
                      vrfName);

          /*
           * AOS-CX cannot bring up OSPFv3 without an IPv4-format
           * router ID, whether configured explicitly or dynamically
           * selected from an interface address.
           */
          if (routerId == null) {
            return;
          }

          Map<Long, List<String>> areaInterfaces =
              new HashMap<>();

          // Preserve explicitly declared process areas.
          process
              .getAreas()
              .forEach(
                  area ->
                      areaInterfaces.computeIfAbsent(
                          toOspfAreaNumber(area),
                          ignored ->
                              new ArrayList<>()));

          // Associate only interfaces in this process's VRF.
          _interfaces.values().stream()
              .filter(
                  iface ->
                      getInterfaceVrfName(iface)
                              .equals(vrfName)
                          && iface.getOspfv3ProcessId()
                              != null
                          && iface.getOspfv3ProcessId()
                              == process.getProcessId()
                          && iface.getOspfv3Area()
                              != null)
              .forEach(
                  iface ->
                      areaInterfaces
                          .computeIfAbsent(
                              toOspfAreaNumber(
                                  iface.getOspfv3Area()),
                              ignored ->
                                  new ArrayList<>())
                          .add(iface.getName()));

          Map<Long, Boolean> stubAreas =
              new HashMap<>();

          process
              .getStubAreas()
              .forEach(
                  (areaId, suppressInterArea) ->
                      stubAreas.put(
                          toOspfAreaNumber(areaId),
                          suppressInterArea));

          Map<Long, Ospfv3Area> areas =
              new HashMap<>();

          areaInterfaces.forEach(
              (area, interfaces) -> {
                Ospfv3Area.Builder areaBuilder =
                    Ospfv3Area.builder()
                        .setNumber(area)
                        .addInterfaces(interfaces);

                Boolean suppressInterArea =
                    stubAreas.get(area);

                if (suppressInterArea != null) {
                  areaBuilder
                      .setStub(true)
                      .setSuppressInterArea(
                          suppressInterArea);
                }

                areas.put(
                    area,
                    areaBuilder.build());
              });

          Ospfv3Process.builder()
              .setProcessId(
                  Integer.toString(
                      process.getProcessId()))
              .setRouterId(routerId)
              .setAreas(areas)
              .setAdminCost(110)
              .setReferenceBandwidth(
                  process.getReferenceBandwidth())
              .setRedistributeConnected(
                  process.getRedistributeConnected())
              .setRedistributionMetric(25L)
              .setVrf(vrf)
              .build();
        });
  }

  private void convertOspfv3Processes() {
    convertOspfv3ProcessesForVrf(
        DEFAULT_VRF_NAME,
        _ospfv3Processes);

    _ospfv3ProcessesByVrf.forEach(
        this::convertOspfv3ProcessesForVrf);
  }

  private void applyOspfInterfaceSettings() {
    _interfaces.values().forEach(
        iface -> {
          if (iface.getOspfProcessId() == null || iface.getOspfArea() == null) {
            return;
          }

          Interface viInterface = _c.getAllInterfaces().get(iface.getName());
          if (viInterface == null) {
            return;
          }

          OspfNetworkType networkType =
              iface.getOspfNetworkType() == AosCxInterface.OspfNetworkType.POINT_TO_POINT
                  ? OspfNetworkType.POINT_TO_POINT
                  : OspfNetworkType.BROADCAST;

          Integer ospfCost =
              iface.getOspfCost() != null
                  ? iface.getOspfCost()
                  : getInterfaceType(iface) == InterfaceType.LOOPBACK ? 1 : null;

          viInterface.setOspfSettings(
              OspfInterfaceSettings.builder()
                  .setAreaName(toOspfAreaNumber(iface.getOspfArea()))
                  .setCost(ospfCost)
                  .setProcess(Integer.toString(iface.getOspfProcessId()))
                  .setEnabled(true)
                  .setPassive(false)
                  .setHelloInterval(10)
                  .setDeadInterval(40)
                  .setNetworkType(networkType)
                  .build());
        });
  }

  private void applyOspfv3InterfaceSettings() {
    _interfaces.values().forEach(
        iface -> {
          if (iface.getOspfv3ProcessId() == null
              || iface.getOspfv3Area() == null) {
            return;
          }

          Interface viInterface =
              _c.getAllInterfaces().get(iface.getName());
          if (viInterface == null) {
            return;
          }

          OspfNetworkType networkType =
              iface.getOspfv3NetworkType()
                      == AosCxInterface.OspfNetworkType.POINT_TO_POINT
                  ? OspfNetworkType.POINT_TO_POINT
                  : OspfNetworkType.BROADCAST;

          Integer ospfv3Cost =
              iface.getOspfv3Cost() != null
                  ? iface.getOspfv3Cost()
                  : getInterfaceType(iface) == InterfaceType.LOOPBACK
                      ? 1
                      : null;

          String vrfName =
              getInterfaceVrfName(iface);

          AosCxOspfv3Process vendorProcess =
              getOspfv3Processes(vrfName)
                  .get(iface.getOspfv3ProcessId());

          Boolean passiveOverride =
              iface.getOspfv3Passive();

          boolean passive =
              passiveOverride != null
                  ? passiveOverride
                  : vendorProcess != null
                      && vendorProcess
                          .getPassiveInterfaceDefault();

          viInterface.setOspfv3Settings(
              Ospfv3InterfaceSettings.builder()
                  .setAreaName(
                      toOspfAreaNumber(iface.getOspfv3Area()))
                  .setCost(ospfv3Cost)
                  .setProcess(
                      Integer.toString(iface.getOspfv3ProcessId()))
                  .setEnabled(true)
                  .setPassive(passive)
                  .setHelloInterval(
                      iface.getOspfv3HelloInterval() != null
                          ? iface.getOspfv3HelloInterval()
                          : 10)
                  .setDeadInterval(
                      iface.getOspfv3DeadInterval() != null
                          ? iface.getOspfv3DeadInterval()
                          : 40)
                  .setNetworkType(networkType)
                  .build());
        });
  }

  private void convertStaticRoute(AosCxStaticRoute route) {
    String vrfName =
        route.getVrfName() == null ? DEFAULT_VRF_NAME : route.getVrfName();
    Vrf vrf = _c.getVrfs().get(vrfName);
    if (vrf == null) {
      vrf = _c.getDefaultVrf();
    }

    NextHop nextHop =
        switch (route.getNextHopType()) {
          case IP -> NextHopIp.of(Ip.parse(route.getNextHop()));
          case INTERFACE -> NextHopInterface.of(route.getNextHop());
          case NULL_ROUTE, REJECT -> NextHopDiscard.instance();
        };

    vrf.getStaticRoutes()
        .add(
            org.batfish.datamodel.StaticRoute.builder()
                .setNetwork(route.getPrefix())
                .setNextHop(nextHop)
                .setAdministrativeCost(1)
                .setRecursive(route.getNextHopType() == AosCxStaticRoute.NextHopType.IP)
                .build());
  }

  private void convertStaticRoute6(
      AosCxStaticRoute6 route) {
    String vrfName =
        route.getVrfName() == null
            ? DEFAULT_VRF_NAME
            : route.getVrfName();

    Vrf vrf = _c.getVrfs().get(vrfName);
    if (vrf == null) {
      vrf = _c.getDefaultVrf();
    }

    StaticRoute6.Builder builder =
        StaticRoute6.builder()
            .setNetwork(route.getPrefix())
            .setAdministrativeCost(
                route.getAdministrativeDistance())
            .setTag(route.getTag());

    switch (route.getNextHopType()) {
      case IP ->
          builder.setNextHopIp(
              Ip6.parse(route.getNextHop()));

      case INTERFACE ->
          builder.setNextHopInterface(
              route.getNextHop());

      case NULL_ROUTE, REJECT ->
          builder.setNextHopInterface(
              Interface.NULL_INTERFACE_NAME);
    }

    vrf.getStaticRoutes6().add(builder.build());
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    _c = new Configuration(_hostname, _vendor);
    _c.setHumanName(_rawHostname);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    Map<String, Vrf> viVrfs = new HashMap<>();
    viVrfs.put(DEFAULT_VRF_NAME, new Vrf(DEFAULT_VRF_NAME));
    _vrfs.forEach(name -> viVrfs.putIfAbsent(name, new Vrf(name)));
    _c.setVrfs(ImmutableMap.copyOf(viVrfs));

    Vrf defaultVrf = _c.getDefaultVrf();

    convertIpAccessLists();
    convertIpv6AccessLists();
    convertPrefixLists();
    convertRouteMaps();
    convertOspfProcesses();
    convertOspfv3Processes();
    convertBgpProcesses();
    _interfaces.values().forEach(this::convertInterface);
    finalizeLagMembership();
    applyOspfInterfaceSettings();
    applyOspfv3InterfaceSettings();
    _staticRoutes.forEach(this::convertStaticRoute);
    _staticRoutes6.forEach(this::convertStaticRoute6);

    return ImmutableList.of(_c);
  }
}
