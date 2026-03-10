package org.batfish.datamodel.isp_configuration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.IspNodeInfo.Role;
import org.batfish.datamodel.isp_configuration.IspPeeringInfo.Peer;
import org.batfish.datamodel.isp_configuration.traffic_filtering.IspTrafficFiltering;
import org.junit.Test;

/** Tests for {@link IspConfiguration} */
public class IspConfigurationTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                ImmutableList.of(),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1")),
                ImmutableList.of()),
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                ImmutableList.of(),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1")),
                ImmutableList.of()))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                ImmutableList.of(),
                new IspFilter(ImmutableList.of(5678L), ImmutableList.of(Ip.parse("2.2.2.2"))),
                ImmutableList.of(new IspNodeInfo(42, "n1")),
                ImmutableList.of()))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("diffNode", "diffInterface"))),
                ImmutableList.of(),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1")),
                ImmutableList.of()))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                ImmutableList.of(new BgpPeerInfo("other", null, Ip.ZERO, null, null)),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1")),
                ImmutableList.of()))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                ImmutableList.of(),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(24, "diffName")),
                ImmutableList.of()))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                ImmutableList.of(),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(24, "n1")),
                ImmutableList.of(new IspPeeringInfo(new Peer(1L), new Peer(2L)))))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IspConfiguration ispConfiguration =
        new IspConfiguration(
            ImmutableList.of(new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
            ImmutableList.of(new BgpPeerInfo("node", null, Ip.ZERO, null, null)),
            new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
            ImmutableList.of(new IspNodeInfo(42, "n1")),
            ImmutableList.of(new IspPeeringInfo(new Peer(1L), new Peer(2L))));

    assertThat(
        BatfishObjectMapper.clone(ispConfiguration, IspConfiguration.class),
        equalTo(ispConfiguration));
  }

  @Test
  public void testDeserializeJsonExample() throws JsonProcessingException {
    String json =
        readResource("org/batfish/datamodel/isp_configuration/isp_configuration.json", UTF_8);
    IspConfiguration c = BatfishObjectMapper.mapper().readValue(json, IspConfiguration.class);
    assertThat(
        c.getBorderInterfaces(),
        contains(
            new BorderInterfaceInfo(NodeInterfacePair.of("bor01", "xe-1/2/0.0")),
            new BorderInterfaceInfo(NodeInterfacePair.of("bor02", "xe-1/2/0.0"))));
    assertThat(c.getFilter(), equalTo(IspFilter.ALLOW_ALL));
    assertThat(
        c.getIspNodeInfos(),
        contains(
            new IspNodeInfo(
                65401, "ISP-A", Role.TRANSIT, ImmutableList.of(), IspTrafficFiltering.none()),
            new IspNodeInfo(
                65402,
                "ISP-B",
                Role.TRANSIT,
                ImmutableList.of(),
                IspTrafficFiltering.blockReservedAddressesAtInternet())));
  }
}
