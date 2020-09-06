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
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))),
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                new IspFilter(ImmutableList.of(5678L), ImmutableList.of(Ip.parse("2.2.2.2"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("diffNode", "diffInterface"))),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(42, "n1"))))
        .addEqualityGroup(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
                new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
                ImmutableList.of(new IspNodeInfo(24, "diffName"))))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IspConfiguration ispConfiguration =
        new IspConfiguration(
            ImmutableList.of(new BorderInterfaceInfo(NodeInterfacePair.of("node", "interface"))),
            new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
            ImmutableList.of(new IspNodeInfo(42, "n1")));

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
            new IspNodeInfo(65401, "ISP-A", ImmutableList.of(), IspTrafficFiltering.none()),
            new IspNodeInfo(
                65402,
                "ISP-B",
                ImmutableList.of(),
                IspTrafficFiltering.blockReservedAddressesAtInternet())));
  }
}
