package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableMap;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute6;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.batfish.dataplane.rib.ConnectedRib6;
import org.junit.Test;

/** Tests for {@link Ospfv3RoutingProcess}. */
public final class Ospfv3RoutingProcessTest {

  @Test
  public void testInternalAndConnectedRedistribution() {
    Node node = TestUtils.makeIosRouter("n1");
    Configuration c = node.getConfiguration();
    Vrf vrf = c.getDefaultVrf();

    ConcreteInterfaceAddress6 internalAddress =
        ConcreteInterfaceAddress6.parse(
            "2001:db8:1::1/64");

    Interface.builder()
        .setName("Ethernet1")
        .setOwner(c)
        .setVrf(vrf)
        .setType(InterfaceType.PHYSICAL)
        .setAddress(internalAddress)
        .setBandwidth(10_000_000_000D)
        .setOspfv3Settings(
            Ospfv3InterfaceSettings.builder()
                .setAreaName(0L)
                .setCost(25)
                .setProcess("1")
                .setEnabled(true)
                .setPassive(false)
                .setHelloInterval(10)
                .setDeadInterval(40)
                .setNetworkType(
                    OspfNetworkType.POINT_TO_POINT)
                .build())
        .build();

    ConcreteInterfaceAddress6 externalAddress =
        ConcreteInterfaceAddress6.parse(
            "2001:db8:2::1/64");

    Interface.builder()
        .setName("Ethernet2")
        .setOwner(c)
        .setVrf(vrf)
        .setType(InterfaceType.PHYSICAL)
        .setAddress(externalAddress)
        .build();

    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterface("Ethernet1")
            .build();

    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(Ip.parse("192.0.2.1"))
            .setAreas(ImmutableMap.of(0L, area))
            .setRedistributeConnected(true)
            .build();

    ConnectedRib6 connectedRib = new ConnectedRib6();
    connectedRib.mergeRoute(
        new ConnectedRoute6(
            internalAddress.getPrefix(),
            "Ethernet1"));
    connectedRib.mergeRoute(
        new ConnectedRoute6(
            externalAddress.getPrefix(),
            "Ethernet2"));

    Ospfv3RoutingProcess routingProcess =
        new Ospfv3RoutingProcess(
            process, Configuration.DEFAULT_VRF_NAME, c);

    routingProcess.initialize(connectedRib);

    /*
     * Locally redistributed routes are control-plane advertisements,
     * not locally installed OSPF routing candidates. The source
     * connected route remains the router's forwarding route.
     */
    assertThat(
        routingProcess.getRoutingRoutes(),
        hasSize(1));

    assertThat(
        routingProcess.refreshLocalExternalAdvertisements(
            connectedRib,
            Set.of(),
            ImmutableMap.of(),
            false),
        equalTo(true));

    Set<AbstractRoute6> routes =
        routingProcess.getRoutes();

    /*
     * Neighbors see both the internal OSPF route and the locally
     * originated external advertisement.
     */
    assertThat(routes, hasSize(2));

    Ospfv3IntraAreaRoute6 internal =
        (Ospfv3IntraAreaRoute6)
            routes.stream()
                .filter(
                    route ->
                        route instanceof
                            Ospfv3IntraAreaRoute6)
                .findFirst()
                .orElseThrow();

    assertThat(
        internal.getNetwork(),
        equalTo(internalAddress.getPrefix()));
    assertThat(internal.getMetric(), equalTo(25L));
    assertThat(internal.getArea(), equalTo(0L));

    Ospfv3ExternalType2Route6 external =
        (Ospfv3ExternalType2Route6)
            routes.stream()
                .filter(
                    route ->
                        route instanceof
                            Ospfv3ExternalType2Route6)
                .findFirst()
                .orElseThrow();

    assertThat(
        external.getNetwork(),
        equalTo(externalAddress.getPrefix()));
    assertThat(external.getMetric(), equalTo(25L));
    assertThat(
        external.getAdvertiser(),
        equalTo(Ip.parse("192.0.2.1")));
  }

  @Test
  public void testAutomaticInterfaceCost() {
    Node node = TestUtils.makeIosRouter("n1");
    Configuration c = node.getConfiguration();
    Vrf vrf = c.getDefaultVrf();

    ConcreteInterfaceAddress6 address =
        ConcreteInterfaceAddress6.parse(
            "2001:db8:3::1/64");

    Interface iface =
        Interface.builder()
            .setName("Ethernet3")
            .setOwner(c)
            .setVrf(vrf)
            .setType(InterfaceType.PHYSICAL)
            .setAddress(address)
            .setBandwidth(10_000_000_000D)
            .setOspfv3Settings(
                Ospfv3InterfaceSettings.builder()
                    .setAreaName(0L)
                    .setProcess("1")
                    .setEnabled(true)
                    .setPassive(false)
                    .setHelloInterval(10)
                    .setDeadInterval(40)
                    .setNetworkType(
                        OspfNetworkType.POINT_TO_POINT)
                    .build())
            .build();

    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterface("Ethernet3")
            .build();

    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(Ip.parse("192.0.2.1"))
            .setAreas(ImmutableMap.of(0L, area))
            .build();

    Ospfv3RoutingProcess routingProcess =
        new Ospfv3RoutingProcess(
            process, Configuration.DEFAULT_VRF_NAME, c);

    assertThat(
        routingProcess.computeInterfaceCost(iface),
        equalTo(10L));
  }
}
