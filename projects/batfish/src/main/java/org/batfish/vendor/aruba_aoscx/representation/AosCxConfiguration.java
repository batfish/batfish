package org.batfish.vendor.aruba_aoscx.representation;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
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
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific configuration for Aruba AOS-CX. */
public class AosCxConfiguration extends VendorConfiguration {

  private transient Configuration _c;
  private String _hostname;
  private final Map<String, AosCxInterface> _interfaces = new HashMap<>();
  private final Map<Integer, AosCxOspfProcess> _ospfProcesses = new HashMap<>();
  private final Map<String, AosCxPrefixList> _prefixLists = new HashMap<>();
  private final Map<String, AosCxRouteMap> _routeMaps = new HashMap<>();
  private AosCxBgpProcess _bgpProcess;
  private String _rawHostname;
  private final List<AosCxStaticRoute> _staticRoutes = new ArrayList<>();
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

  public AosCxBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public AosCxBgpProcess getOrCreateBgpProcess(long localAs) {
    if (_bgpProcess == null) {
      _bgpProcess = new AosCxBgpProcess(localAs);
    }
    return _bgpProcess;
  }

  public Map<String, AosCxRouteMap> getRouteMaps() {
    return _routeMaps;
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

  public AosCxOspfProcess getOrCreateOspfProcess(int processId) {
    return _ospfProcesses.computeIfAbsent(processId, AosCxOspfProcess::new);
  }

  public List<AosCxStaticRoute> getStaticRoutes() {
    return _staticRoutes;
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
    return InterfaceType.PHYSICAL;
  }

  private static boolean getInterfaceAdminUpEffective(AosCxInterface iface) {
    if (iface.getEnabled() != null) {
      return iface.getEnabled();
    }
    // Physical AOS-CX interfaces are disabled by default. Loopback and VLAN
    // interfaces commonly appear enabled without an explicit "no shutdown".
    return getInterfaceType(iface) != InterfaceType.PHYSICAL;
  }

  private void convertInterface(AosCxInterface iface, Vrf vrf) {
    String name = iface.getName();

    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setAdminUp(getInterfaceAdminUpEffective(iface))
            .setBandwidth(iface.getBandwidth())
            .setType(getInterfaceType(iface))
            .setName(name)
            .setVrf(vrf)
            .setOwner(_c);

    newIface.setHumanName(name);
    newIface.setDeclaredNames(ImmutableList.of(name));

    if (iface.getAddress() != null) {
      newIface.setAddress(iface.getAddress());
    }

    newIface.build();
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

  private void convertBgpProcess(Vrf vrf) {
    if (_bgpProcess == null || _bgpProcess.getRouterId() == null) {
      return;
    }

    BgpProcess viProcess =
        BgpProcess.builder()
            .setRouterId(_bgpProcess.getRouterId())
            .setEbgpAdminCost(20)
            .setIbgpAdminCost(200)
            .setLocalAdminCost(200)
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setVrf(vrf)
            .build();

    _bgpProcess.getNeighbors().values().forEach(
        neighbor -> {
          if (neighbor.getRemoteAs() == null) {
            return;
          }

          BgpActivePeerConfig.Builder peer =
              BgpActivePeerConfig.builder()
                  .setBgpProcess(viProcess)
                  .setLocalAs(_bgpProcess.getLocalAs())
                  .setPeerAddress(neighbor.getIp())
                  .setRemoteAs(neighbor.getRemoteAs());

          if (neighbor.getIpv4UnicastActive()) {
            peer.setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder().build());
          }

          peer.build();
        });
  }

  private void convertOspfProcesses(Vrf vrf) {
    _ospfProcesses.values().forEach(
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

          Map<Long, List<String>> areaInterfaces = new HashMap<>();
          _interfaces.values().stream()
              .filter(
                  iface ->
                      iface.getOspfProcessId() != null
                          && iface.getOspfProcessId() == process.getProcessId()
                          && iface.getOspfArea() != null)
              .forEach(
                  iface ->
                      areaInterfaces
                          .computeIfAbsent(
                              toOspfAreaNumber(iface.getOspfArea()), area -> new ArrayList<>())
                          .add(iface.getName()));

          areaInterfaces.forEach(
              (area, interfaces) ->
                  OspfArea.builder()
                      .setNumber(area)
                      .addInterfaces(interfaces)
                      .setOspfProcess(viProcess)
                      .build());
        });
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

          viInterface.setOspfSettings(
              OspfInterfaceSettings.builder()
                  .setAreaName(toOspfAreaNumber(iface.getOspfArea()))
                  .setProcess(Integer.toString(iface.getOspfProcessId()))
                  .setEnabled(true)
                  .setPassive(false)
                  .setHelloInterval(10)
                  .setDeadInterval(40)
                  .setNetworkType(networkType)
                  .build());
        });
  }

  private void convertStaticRoute(AosCxStaticRoute route, Vrf vrf) {
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

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    _c = new Configuration(_hostname, _vendor);
    _c.setHumanName(_rawHostname);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    Vrf defaultVrf = new Vrf(DEFAULT_VRF_NAME);
    _c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, defaultVrf));

    convertPrefixLists();
    convertRouteMaps();
    convertOspfProcesses(defaultVrf);
    convertBgpProcess(defaultVrf);
    _interfaces.values().forEach(iface -> convertInterface(iface, defaultVrf));
    applyOspfInterfaceSettings();
    _staticRoutes.forEach(route -> convertStaticRoute(route, defaultVrf));

    return ImmutableList.of(_c);
  }
}
