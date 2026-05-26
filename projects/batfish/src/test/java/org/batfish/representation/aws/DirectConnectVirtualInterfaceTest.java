package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.representation.aws.DirectConnectVirtualInterface.BgpPeer;
import org.junit.Test;

/** Tests for {@link DirectConnectVirtualInterface} */
public class DirectConnectVirtualInterfaceTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource(
            "org/batfish/representation/aws/DirectConnectVirtualInterfaceTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    // Only the "available" VIFs should be included
    assertThat(
        region.getDirectConnectVirtualInterfaces(),
        equalTo(
            ImmutableMap.of(
                "dxvif-fghi1234",
                new DirectConnectVirtualInterface(
                    "dxvif-fghi1234",
                    "my-transit-vif",
                    "transit",
                    "dxcon-abc12345",
                    "12345678-90ab-4cde-9f01-23456789abcd",
                    null,
                    100,
                    65001L,
                    ConcreteInterfaceAddress.parse("169.254.100.1/30"),
                    ConcreteInterfaceAddress.parse("169.254.100.2/30"),
                    ImmutableList.of(
                        new BgpPeer(
                            65001L,
                            ConcreteInterfaceAddress.parse("169.254.100.1/30"),
                            ConcreteInterfaceAddress.parse("169.254.100.2/30"))),
                    ImmutableMap.of("Name", "prod-vif")),
                "dxvif-vgw0001",
                new DirectConnectVirtualInterface(
                    "dxvif-vgw0001",
                    "vgw-private-vif",
                    "private",
                    "dxcon-vgw01",
                    null,
                    "vgw-abcd1234",
                    300,
                    65003L,
                    ConcreteInterfaceAddress.parse("169.254.30.1/30"),
                    ConcreteInterfaceAddress.parse("169.254.30.2/30"),
                    ImmutableList.of(
                        new BgpPeer(
                            65003L,
                            ConcreteInterfaceAddress.parse("169.254.30.1/30"),
                            ConcreteInterfaceAddress.parse("169.254.30.2/30"))),
                    ImmutableMap.of()))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new DirectConnectVirtualInterface(
                "vif1",
                "name",
                "transit",
                "conn1",
                "dxgw-1",
                null,
                100,
                65001L,
                ConcreteInterfaceAddress.parse("169.254.1.1/30"),
                ConcreteInterfaceAddress.parse("169.254.1.2/30"),
                ImmutableList.of(),
                ImmutableMap.of()),
            new DirectConnectVirtualInterface(
                "vif1",
                "name",
                "transit",
                "conn1",
                "dxgw-1",
                null,
                100,
                65001L,
                ConcreteInterfaceAddress.parse("169.254.1.1/30"),
                ConcreteInterfaceAddress.parse("169.254.1.2/30"),
                ImmutableList.of(),
                ImmutableMap.of()))
        .addEqualityGroup(
            new DirectConnectVirtualInterface(
                "vif2",
                "name",
                "transit",
                "conn1",
                "dxgw-1",
                null,
                100,
                65001L,
                ConcreteInterfaceAddress.parse("169.254.1.1/30"),
                ConcreteInterfaceAddress.parse("169.254.1.2/30"),
                ImmutableList.of(),
                ImmutableMap.of()))
        .addEqualityGroup(
            new DirectConnectVirtualInterface(
                "vif1",
                "name",
                "transit",
                "conn1",
                "dxgw-1",
                null,
                200,
                65001L,
                ConcreteInterfaceAddress.parse("169.254.1.1/30"),
                ConcreteInterfaceAddress.parse("169.254.1.2/30"),
                ImmutableList.of(),
                ImmutableMap.of()))
        .addEqualityGroup(
            new DirectConnectVirtualInterface(
                "vif1",
                "name",
                "private",
                "conn1",
                null,
                "vgw-1",
                100,
                65001L,
                ConcreteInterfaceAddress.parse("169.254.1.1/30"),
                ConcreteInterfaceAddress.parse("169.254.1.2/30"),
                ImmutableList.of(),
                ImmutableMap.of()))
        .testEquals();
  }
}
