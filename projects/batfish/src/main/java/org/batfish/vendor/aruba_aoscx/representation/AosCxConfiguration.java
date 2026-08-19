package org.batfish.vendor.aruba_aoscx.representation;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
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

    convertOspfProcesses(defaultVrf);
    _interfaces.values().forEach(iface -> convertInterface(iface, defaultVrf));
    applyOspfInterfaceSettings();
    _staticRoutes.forEach(route -> convertStaticRoute(route, defaultVrf));

    return ImmutableList.of(_c);
  }
}
