package org.batfish.datamodel.ospf;

import static org.batfish.datamodel.ospf.OspfTopologyUtils.getSessionIfCompatible;
import static org.batfish.datamodel.ospf.OspfTopologyUtils.trimLinks;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Optional;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Test of {@link org.batfish.datamodel.ospf.OspfTopologyUtils} */
public class OspfTopologyUtilsTest {

  private static final OspfNeighborConfigId LOCAL_CONFIG_ID =
      new OspfNeighborConfigId("r1", "vrf1", "proc1", "iface1");
  private static final OspfNeighborConfigId REMOTE_CONFIG_ID =
      new OspfNeighborConfigId("r2", "vrf2", "proc2", "iface2");

  private static NetworkConfigurations buildNetworkConfigurations(Ip localIp, Ip remoteIp) {
    return buildNetworkConfigurations(localIp, false, localIp, 0L, remoteIp, false, remoteIp, 0L);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, boolean localPassive, Ip remoteIp, boolean remotePassive) {
    return buildNetworkConfigurations(
        localIp, localPassive, localIp, 0L, remoteIp, remotePassive, remoteIp, 0L);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, long localArea, Ip remoteIp, long remoteArea) {
    return buildNetworkConfigurations(
        localIp, false, localIp, localArea, remoteIp, false, remoteIp, remoteArea);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp, Ip localRouterId, Ip remoteIp, Ip remoteRouterId) {
    return buildNetworkConfigurations(
        localIp, false, localRouterId, 0L, remoteIp, false, remoteRouterId, 0L);
  }

  private static NetworkConfigurations buildNetworkConfigurations(
      Ip localIp,
      boolean localPassive,
      Ip localRouterId,
      long localArea,
      Ip remoteIp,
      boolean remotePassive,
      Ip remoteRouterId,
      long remoteArea) {
    return NetworkConfigurations.of(
        ImmutableMap.of(
            LOCAL_CONFIG_ID.getHostname(),
            buildConfiguration(LOCAL_CONFIG_ID, localIp, localPassive, localRouterId, localArea),
            REMOTE_CONFIG_ID.getHostname(),
            buildConfiguration(
                REMOTE_CONFIG_ID, remoteIp, remotePassive, remoteRouterId, remoteArea)));
  }

  private static Configuration buildConfiguration(
      OspfNeighborConfigId configId, Ip ospfNeighborIp, boolean passive, Ip routerId, long area) {
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
                        area, OspfArea.builder().addInterface(ifaceName).setNumber(area).build()))
                .setProcessId(procName)
                .setReferenceBandwidth(7.0)
                .setNeighbors(
                    ImmutableMap.of(
                        ifaceName,
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
    c.getVrfs().put(vrfName, vrf);
    return c;
  }

  @Test
  public void testTrimLinks() {
    // Setup
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    OspfNeighborConfigId n1 = new OspfNeighborConfigId("h1", "v", "p", "i");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("h2", "v", "p", "i");
    OspfNeighborConfigId n3 = new OspfNeighborConfigId("h3", "v", "p", "i");
    graph.addNode(n1);
    graph.addNode(n2);
    graph.addNode(n3);
    // n1 <--> n2. Link values (IPs) don't matter
    Ip ip = Ip.parse("1.1.1.1");
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
  public void testGetSessionIfCompatible() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.2"));

    // Confirm we correctly identify compatible sessions
    Optional<OspfSessionProperties> val =
        getSessionIfCompatible(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val.orElse(null), notNullValue());
  }

  @Test
  public void testGetSessionIfCompatibleMismatchArea() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("1.1.1.1"), 0L, Ip.parse("1.1.1.2"), 1L);

    // Confirm we correctly mark a session as incompatible when neighbor areas are not equal
    Optional<OspfSessionProperties> val =
        getSessionIfCompatible(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(Optional.empty()));
  }

  @Test
  public void testGetSessionIfCompatibleMissingConfig() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.2"));
    OspfNeighborConfigId nonExistentConfigId =
        new OspfNeighborConfigId("r2", "vrf2", "bogusProc", "iface2");

    // Confirm we correctly mark a session as incompatible when a neighbor config does not exist
    Optional<OspfSessionProperties> val =
        getSessionIfCompatible(LOCAL_CONFIG_ID, nonExistentConfigId, configs);
    assertThat(val, equalTo(Optional.empty()));
  }

  @Test
  public void testGetSessionIfCompatiblePassive() {
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("1.1.1.1"), true, Ip.parse("1.1.1.2"), false);

    // Confirm we correctly mark a session as incompatible when one of the interfaces is passive
    Optional<OspfSessionProperties> val =
        getSessionIfCompatible(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(Optional.empty()));
  }

  @Test
  public void testGetSessionIfCompatibleDuplicateRouterId() {
    Ip routerId = Ip.parse("1.1.1.1");
    NetworkConfigurations configs =
        buildNetworkConfigurations(Ip.parse("1.1.1.1"), routerId, Ip.parse("1.1.1.2"), routerId);

    // Confirm we mark a session as incompatible when routerId is the same for both neighbors
    Optional<OspfSessionProperties> val =
        getSessionIfCompatible(LOCAL_CONFIG_ID, REMOTE_CONFIG_ID, configs);
    assertThat(val, equalTo(Optional.empty()));
  }
}
