package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.ospf.OspfTopologyUtils.getSessionStatus;
import static org.batfish.datamodel.ospf.OspfTopologyUtils.initNeighborConfigs;
import static org.batfish.datamodel.ospf.OspfTopologyUtils.trimLinks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Builder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.junit.Ignore;
import org.junit.Test;

/** Test of {@link org.batfish.datamodel.ospf.OspfTopologyUtils} */
public class OspfTopologyUtilsTest {

  private static final OspfNeighborConfigId LOCAL_CONFIG_ID =
      new OspfNeighborConfigId(
          "r1", "vrf1", "proc1", "iface1", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
  private static final OspfNeighborConfigId REMOTE_CONFIG_ID =
      new OspfNeighborConfigId(
          "r2", "vrf2", "proc2", "iface2", ConcreteInterfaceAddress.parse("192.0.2.1/31"));

  private static final OspfNeighborConfigId LOCAL_CONFIG_ID_UNNUMBERED =
      new OspfNeighborConfigId(
          "r1", "vrf1", "proc1", "iface1", ConcreteInterfaceAddress.parse("192.0.2.0/32"));
  private static final OspfNeighborConfigId REMOTE_CONFIG_ID_UNNUMBERED =
      new OspfNeighborConfigId(
          "r2", "vrf2", "proc2", "iface2", ConcreteInterfaceAddress.parse("192.0.2.1/32"));

  private static NetworkConfigurations buildNetworkConfigurations(Ip localIp, Ip remoteIp) {
    return buildNetworkConfigurations(
        localIp,
        false,
        localIp,
        0L,
        1500,
        StubType.NONE,
        null,
        remoteIp,
        false,
        remoteIp,
        0L,
        1500,
        StubType.NONE,
        null);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, boolean localPassive, Ip remoteIp, boolean remotePassive) {
    return buildNetworkConfigurations(
        localIp,
        localPassive,
        localIp,
        0L,
        1500,
        StubType.NONE,
        null,
        remoteIp,
        remotePassive,
        remoteIp,
        0L,
        1500,
        StubType.NONE,
        null);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, int localMtu, Ip remoteIp, int remoteMtu) {
    return buildNetworkConfigurations(
        localIp,
        false,
        localIp,
        0L,
        localMtu,
        StubType.NONE,
        null,
        remoteIp,
        false,
        remoteIp,
        0L,
        remoteMtu,
        StubType.NONE,
        null);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, long localArea, Ip remoteIp, long remoteArea) {
    return buildNetworkConfigurations(
        localIp,
        false,
        localIp,
        localArea,
        1500,
        StubType.NONE,
        null,
        remoteIp,
        false,
        remoteIp,
        remoteArea,
        1500,
        StubType.NONE,
        null);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, StubType localAreaType, Ip remoteIp, StubType remoteAreaType) {
    return buildNetworkConfigurations(
        localIp,
        false,
        localIp,
        0L,
        1500,
        localAreaType,
        null,
        remoteIp,
        false,
        remoteIp,
        0L,
        1500,
        remoteAreaType,
        null);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, Ip localRouterId, Ip remoteIp, Ip remoteRouterId) {
    return buildNetworkConfigurations(
        localIp,
        false,
        localRouterId,
        0L,
        1500,
        StubType.NONE,
        null,
        remoteIp,
        false,
        remoteRouterId,
        0L,
        1500,
        StubType.NONE,
        null);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp,
      OspfInterfaceSettings localOspfSettings,
      Ip remoteIp,
      OspfInterfaceSettings remoteOspfSettings) {
    return buildNetworkConfigurations(
        localIp, localOspfSettings, remoteIp, remoteOspfSettings, true);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp,
      OspfInterfaceSettings localOspfSettings,
      Ip remoteIp,
      OspfInterfaceSettings remoteOspfSettings,
      boolean initNeighborConfigs) {
    return buildNetworkConfigurations(
        localIp,
        false,
        localIp,
        0L,
        1500,
        StubType.NONE,
        localOspfSettings,
        remoteIp,
        false,
        remoteIp,
        0L,
        1500,
        StubType.NONE,
        remoteOspfSettings,
        initNeighborConfigs);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp,
      boolean localPassive,
      Ip localRouterId,
      long localArea,
      int localMtu,
      StubType localAreaType,
      OspfInterfaceSettings localOspfSettings,
      Ip remoteIp,
      boolean remotePassive,
      Ip remoteRouterId,
      long remoteArea,
      int remoteMtu,
      StubType remoteAreaType,
      OspfInterfaceSettings remoteOspfSettings) {
    return buildNetworkConfigurations(
        localIp,
        localPassive,
        localRouterId,
        localArea,
        localMtu,
        localAreaType,
        localOspfSettings,
        remoteIp,
        remotePassive,
        remoteRouterId,
        remoteArea,
        remoteMtu,
        remoteAreaType,
        remoteOspfSettings,
        true);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp,
      boolean localPassive,
      Ip localRouterId,
      long localArea,
      int localMtu,
      StubType localAreaType,
      OspfInterfaceSettings localOspfSettings,
      Ip remoteIp,
      boolean remotePassive,
      Ip remoteRouterId,
      long remoteArea,
      int remoteMtu,
      StubType remoteAreaType,
      OspfInterfaceSettings remoteOspfSettings,
      boolean initNeighborConfigs) {
    return NetworkConfigurations.of(
        ImmutableMap.of(
            LOCAL_CONFIG_ID.getHostname(),
            buildConfiguration(
                LOCAL_CONFIG_ID,
                localIp,
                localPassive,
                localRouterId,
                localArea,
                localMtu,
                localAreaType,
                localOspfSettings,
                initNeighborConfigs),
            REMOTE_CONFIG_ID.getHostname(),
            buildConfiguration(
                REMOTE_CONFIG_ID,
                remoteIp,
                remotePassive,
                remoteRouterId,
                remoteArea,
                remoteMtu,
                remoteAreaType,
                remoteOspfSettings,
                initNeighborConfigs)));
  }

  private static Configuration buildConfiguration(
      OspfNeighborConfigId configId,
      Ip ospfNeighborIp,
      boolean passive,
      Ip routerId,
      long area,
      int mtu,
      StubType areaType,
      OspfInterfaceSettings ospfSettings) {
    return buildConfiguration(
        configId, ospfNeighborIp, passive, routerId, area, mtu, areaType, ospfSettings, true);
  }

  private static Configuration buildConfiguration(
      OspfNeighborConfigId configId,
      Ip ospfNeighborIp,
      boolean passive,
      Ip routerId,
      long area,
      int mtu,
      StubType areaType,
      OspfInterfaceSettings ospfSettings,
      boolean initNeighborConfigs) {
    String hostname = configId.getHostname();
    String vrfName = configId.getVrfName();
    String procName = configId.getProcName();
    String ifaceName = configId.getInterfaceName();
    Configuration c = new Configuration(hostname, ConfigurationFormat.CISCO_IOS);
    Vrf vrf = Vrf.builder().setName(vrfName).build();
    vrf.setOspfProcesses(
        ImmutableSortedMap.of(
            procName,
            OspfProcess.builder()
                .setAreas(
                    ImmutableSortedMap.of(
                        area,
                        OspfArea.builder()
                            .addInterface(ifaceName)
                            .setNumber(area)
                            .setStubType(areaType)
                            .build()))
                .setProcessId(procName)
                .setReferenceBandwidth(7.0)
                .setNeighborConfigs(
                    initNeighborConfigs
                        ? ImmutableMap.of(
                            new OspfNeighborConfigId(
                                hostname,
                                vrfName,
                                procName,
                                ifaceName,
                                ConcreteInterfaceAddress.create(ospfNeighborIp, 31)),
                            OspfNeighborConfig.builder()
                                .setVrfName(vrfName)
                                .setInterfaceName(ifaceName)
                                .setHostname(hostname)
                                .setArea(area)
                                .setPassive(passive)
                                .setIp(ospfNeighborIp)
                                .build())
                        : ImmutableMap.of())
                .setRouterId(routerId)
                .build()));
    Builder iface = TestInterface.builder().setName(ifaceName).setMtu(mtu);
    iface.setOspfSettings(
        firstNonNull(ospfSettings, OspfInterfaceSettings.defaultSettingsBuilder().build()));
    c.getAllInterfaces().put(ifaceName, iface.build());
    c.getVrfs().put(vrfName, vrf);
    return c;
  }

  private static Configuration buildConfigurationNoArea(
      OspfNeighborConfigId configId,
      Ip ospfNeighborIp,
      boolean passive,
      Ip routerId,
      long area,
      int mtu,
      OspfInterfaceSettings ospfSettings) {
    String hostname = configId.getHostname();
    String vrfName = configId.getVrfName();
    String procName = configId.getProcName();
    String ifaceName = configId.getInterfaceName();
    Configuration c = new Configuration(hostname, ConfigurationFormat.CISCO_IOS);
    Vrf vrf = Vrf.builder().setName(vrfName).build();
    vrf.setOspfProcesses(
        ImmutableSortedMap.of(
            procName,
            OspfProcess.builder()
                .setProcessId(procName)
                .setReferenceBandwidth(7.0)
                .setNeighborConfigs(
                    ImmutableMap.of(
                        new OspfNeighborConfigId(
                            hostname,
                            vrfName,
                            procName,
                            ifaceName,
                            ConcreteInterfaceAddress.create(ospfNeighborIp, 31)),
                        OspfNeighborConfig.builder()
                            .setVrfName(vrfName)
                            .setInterfaceName(ifaceName)
                            .setHostname(hostname)
                            .setArea(area)
                            .setPassive(passive)
                            .setIp(ospfNeighborIp)
                            .build()))
                .setRouterId(routerId)
                .build()));
    Builder iface = TestInterface.builder().setName(ifaceName).setMtu(mtu);
    iface.setOspfSettings(
        firstNonNull(ospfSettings, OspfInterfaceSettings.defaultSettingsBuilder().build()));
    c.getAllInterfaces().put(ifaceName, iface.build());
    c.getVrfs().put(vrfName, vrf);
    return c;
  }

  @Test
  public void testTrimLinks() {
    // Setup
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    OspfNeighborConfigId n1 =
        new OspfNeighborConfigId(
            "h1", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
    OspfNeighborConfigId n2 =
        new OspfNeighborConfigId(
            "h2", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.1/31"));
    OspfNeighborConfigId n3 =
        new OspfNeighborConfigId(
            "h3", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.2/31"));
    graph.addNode(n1);
    graph.addNode(n2);
    graph.addNode(n3);
    // n1 <--> n2. Link values (IPs) don't matter
    Ip ip = Ip.parse("192.0.2.0");
    OspfSessionProperties s = new OspfSessionProperties(0, new IpLink(ip, ip));
    graph.putEdgeValue(n1, n2, s);
    graph.putEdgeValue(n2, n1, s);

    // n1  --> n3
    graph.putEdgeValue(n1, n3, s);

    // Test: resulting edges should only be n1 <--> n2
    trimLinks(graph);

    assertThat(
        graph.edges(),
        equalTo(ImmutableSet.of(EndpointPair.ordered(n1, n2), EndpointPair.ordered(n2, n1))));
  }

  @Test
  public void testGetSessionStatusCompatible() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("192.0.2.0"), Ip.parse("192.0.2.1"));

    // Confirm we correctly identify compatible sessions
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.ESTABLISHED));
  }

  @Test
  public void testGetSessionStatusMismatchArea() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("192.0.2.0"), 0L, Ip.parse("192.0.2.1"), 1L);

    // Confirm we correctly mark a session as incompatible when neighbor areas are not equal
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.AREA_MISMATCH));
  }

  @Test
  public void testGetSessionStatusMismatchAreaType() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            Ip.parse("192.0.2.0"), StubType.STUB, Ip.parse("192.0.2.1"), StubType.NONE);

    // Confirm we correctly mark a session as incompatible when neighbor area types do not match
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.AREA_TYPE_MISMATCH));
  }

  @Test
  public void testGetSessionStatusMissingProcess() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("192.0.2.0"), Ip.parse("192.0.2.1"));
    OspfNeighborConfigId bogusProcConfigId =
        new OspfNeighborConfigId(
            "r2", "vrf2", "bogusProc", "iface2", ConcreteInterfaceAddress.parse("192.0.2.0/31"));

    // No process configured for the remote should result in process missing/misconfigured status
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, bogusProcConfigId, configs);
    assertThat(val, equalTo(OspfSessionStatus.PROCESS_INVALID));
  }

  @Test
  public void testGetSessionStatusMissingArea() {
    Ip localIp = Ip.parse("192.0.2.0");
    Ip remoteIp = Ip.parse("192.0.2.1");
    NetworkConfigurations configs =
        NetworkConfigurations.of(
            ImmutableMap.of(
                LOCAL_CONFIG_ID.getHostname(),
                buildConfiguration(
                    LOCAL_CONFIG_ID, localIp, false, localIp, 1, 1500, StubType.NONE, null),
                REMOTE_CONFIG_ID.getHostname(),
                buildConfigurationNoArea(
                    REMOTE_CONFIG_ID, remoteIp, false, remoteIp, 1, 1500, null)));

    // No area configured for the remote should result in area missing/misconfigured status
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.AREA_INVALID));
  }

  @Ignore(
      "Frame/packet MTU support not fully there, currently we optimistically leave OSPF sessions"
          + " up")
  @Test
  public void testGetSessionStatusMtuMismatch() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("192.0.2.0"), 1234, Ip.parse("192.0.2.1"), 1500);

    // Confirm we correctly mark a session as incompatible when interfaces has mismatched MTU
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.MTU_MISMATCH));
  }

  @Test
  public void testGetSessionStatusDeadIntervalMismatch() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            Ip.parse("192.0.2.0"),
            OspfInterfaceSettings.defaultSettingsBuilder().setDeadInterval(44).build(),
            Ip.parse("192.0.2.1"),
            OspfInterfaceSettings.defaultSettingsBuilder().setDeadInterval(40).build());

    // Confirm we correctly mark a session as incompatible when OSPF dead intervals are mismatched
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.DEAD_INTERVAL_MISMATCH));
  }

  @Test
  public void testGetSessionStatusHelloIntervalMismatch() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            Ip.parse("192.0.2.0"),
            OspfInterfaceSettings.defaultSettingsBuilder().setHelloInterval(11).build(),
            Ip.parse("192.0.2.1"),
            OspfInterfaceSettings.defaultSettingsBuilder().setHelloInterval(10).build());

    // Confirm we correctly mark a session as incompatible when OSPF hello intervals are mismatched
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.HELLO_INTERVAL_MISMATCH));
  }

  @Test
  public void testGetSessionStatusNetworkTypeMismatch() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            Ip.parse("192.0.2.0"),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setNetworkType(OspfNetworkType.POINT_TO_POINT)
                .build(),
            Ip.parse("192.0.2.1"),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setNetworkType(OspfNetworkType.BROADCAST)
                .build());

    // Confirm we correctly mark a session as incompatible when OSPF network types are mismatched
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.NETWORK_TYPE_MISMATCH));
  }

  @Test
  public void testGetSessionStatusPassiveMismatch() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("192.0.2.0"), true, Ip.parse("192.0.2.1"), false);

    // Confirm we correctly mark a session as incompatible when one of the interfaces is passive
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.PASSIVE_MISMATCH));
  }

  @Test
  public void testGetSessionStatusBothPassive() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("192.0.2.0"), true, Ip.parse("192.0.2.1"), true);

    // Confirm we indicate there is no session when both OSPF peers are passive
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.NO_SESSION));
  }

  @Test
  public void testGetSessionStatusDuplicateRouterId() {
    Ip routerId = Ip.parse("192.0.2.0");
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            Ip.parse("192.0.2.0"), routerId, Ip.parse("192.0.2.1"), routerId);

    // Confirm we mark a session as incompatible when routerId is the same for both neighbors
    OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(OspfSessionStatus.DUPLICATE_ROUTER_ID));
  }

  @Test
  public void testGetSessionStatusNbma() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            Ip.parse("192.0.2.0"),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setNetworkType(OspfNetworkType.NON_BROADCAST_MULTI_ACCESS)
                .setNbmaNeighbors(ImmutableSet.of(Ip.parse("192.0.2.1")))
                .build(),
            Ip.parse("192.0.2.1"),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setNetworkType(OspfNetworkType.NON_BROADCAST_MULTI_ACCESS)
                .build());

    // if neighbor is specified return ESTABLISHED
    {
      OspfSessionStatus val = getSessionStatus(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
      assertThat(val, equalTo(OspfSessionStatus.ESTABLISHED));
    }
    // if neighbor is not specified return NO_SESSION
    {
      OspfSessionStatus val = getSessionStatus(REMOTE_CONFIG_ID, LOCAL_CONFIG_ID, configs);
      assertThat(val, equalTo(OspfSessionStatus.NO_SESSION));
    }
  }

  @Test
  public void testInitNeighborConfigsOspfAddresses() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            LOCAL_CONFIG_ID_UNNUMBERED.getAddress().getIp(),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setOspfAddresses(
                    OspfAddresses.of(ImmutableList.of(LOCAL_CONFIG_ID_UNNUMBERED.getAddress())))
                .setProcess(LOCAL_CONFIG_ID_UNNUMBERED.getProcName())
                .setNetworkType(OspfNetworkType.POINT_TO_POINT)
                .build(),
            REMOTE_CONFIG_ID_UNNUMBERED.getAddress().getIp(),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setOspfAddresses(
                    OspfAddresses.of(ImmutableList.of(REMOTE_CONFIG_ID_UNNUMBERED.getAddress())))
                .setProcess(REMOTE_CONFIG_ID_UNNUMBERED.getProcName())
                .setNetworkType(OspfNetworkType.POINT_TO_POINT)
                .build(),
            false);
    updateConfigsForUnnumbered(configs);

    // Confirm that unnumbered interfaces are candidates for sessions using ospfAddresses
    initNeighborConfigs(configs);
    assertThat(
        Iterables.getOnlyElement(configs.getVrf("r1", "vrf1").get().getOspfProcesses().values())
            .getOspfNeighborConfigs()
            .keySet(),
        containsInAnyOrder(LOCAL_CONFIG_ID_UNNUMBERED));
    assertThat(
        Iterables.getOnlyElement(configs.getVrf("r2", "vrf2").get().getOspfProcesses().values())
            .getOspfNeighborConfigs()
            .keySet(),
        containsInAnyOrder(REMOTE_CONFIG_ID_UNNUMBERED));
  }

  private static void updateConfigsForUnnumbered(NetworkConfigurations configs) {
    LinkLocalAddress lla = LinkLocalAddress.of(Ip.parse("169.254.0.1"));
    Interface iface1 = configs.getInterface("r1", "iface1").get();
    iface1.setAddress(lla);
    iface1.setAllAddresses(ImmutableList.of(lla));
    Interface iface2 = configs.getInterface("r2", "iface2").get();
    iface2.setAddress(lla);
    iface2.setAllAddresses(ImmutableList.of(lla));
  }

  @Test
  public void testGetSessionStatusP2PDifferentPrefix() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            LOCAL_CONFIG_ID_UNNUMBERED.getAddress().getIp(),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setOspfAddresses(
                    OspfAddresses.of(ImmutableList.of(LOCAL_CONFIG_ID_UNNUMBERED.getAddress())))
                .setProcess(LOCAL_CONFIG_ID_UNNUMBERED.getProcName())
                .setNetworkType(OspfNetworkType.POINT_TO_POINT)
                .build(),
            REMOTE_CONFIG_ID_UNNUMBERED.getAddress().getIp(),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setOspfAddresses(
                    OspfAddresses.of(ImmutableList.of(REMOTE_CONFIG_ID_UNNUMBERED.getAddress())))
                .setProcess(REMOTE_CONFIG_ID_UNNUMBERED.getProcName())
                .setNetworkType(OspfNetworkType.POINT_TO_POINT)
                .build());
    updateConfigsForUnnumbered(configs);
    initNeighborConfigs(configs);

    assertThat(
        getSessionStatus(LOCAL_CONFIG_ID_UNNUMBERED, REMOTE_CONFIG_ID_UNNUMBERED, configs),
        equalTo(OspfSessionStatus.ESTABLISHED));
  }

  @Test
  public void testGetSessionStatusNonP2PDifferentPrefix() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(
            LOCAL_CONFIG_ID_UNNUMBERED.getAddress().getIp(),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setOspfAddresses(
                    OspfAddresses.of(ImmutableList.of(LOCAL_CONFIG_ID_UNNUMBERED.getAddress())))
                .setProcess(LOCAL_CONFIG_ID_UNNUMBERED.getProcName())
                .setNetworkType(OspfNetworkType.BROADCAST)
                .build(),
            REMOTE_CONFIG_ID_UNNUMBERED.getAddress().getIp(),
            OspfInterfaceSettings.defaultSettingsBuilder()
                .setOspfAddresses(
                    OspfAddresses.of(ImmutableList.of(REMOTE_CONFIG_ID_UNNUMBERED.getAddress())))
                .setProcess(REMOTE_CONFIG_ID_UNNUMBERED.getProcName())
                .setNetworkType(OspfNetworkType.BROADCAST)
                .build(),
            false);
    updateConfigsForUnnumbered(configs);
    initNeighborConfigs(configs);

    assertThat(
        getSessionStatus(LOCAL_CONFIG_ID_UNNUMBERED, REMOTE_CONFIG_ID_UNNUMBERED, configs),
        equalTo(OspfSessionStatus.NO_SESSION));
  }
}
