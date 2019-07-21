package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPN_CONNECTIONS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link VpnConnection} */
public class VpnConnectionTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/VpnConnectionTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_VPN_CONNECTIONS);
    List<VpnConnection> vpnConnections = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      vpnConnections.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), VpnConnection.class));
    }

    assertThat(
        vpnConnections,
        equalTo(
            ImmutableList.of(
                new VpnConnection(
                    "vpn-ba2e34a8",
                    "cgw-fb76ace5",
                    "vgw-81fd279f",
                    ImmutableList.of(
                        new IpsecTunnel(
                            65301L,
                            Ip.parse("169.254.15.194"),
                            30,
                            Ip.parse("147.75.69.27"),
                            "sha1",
                            "aes-128-cbc",
                            28800,
                            "main",
                            "group2",
                            "7db2fd6e9dcffcf826743b57bc0518cfcbca8f4db0b80a7a2c3f0c3b09deb49a",
                            "hmac-sha1-96",
                            "aes-128-cbc",
                            3600,
                            "tunnel",
                            "group2",
                            "esp",
                            65401L,
                            Ip.parse("169.254.15.193"),
                            30,
                            Ip.parse("52.27.166.152"),
                            null),
                        new IpsecTunnel(
                            65301L,
                            Ip.parse("169.254.13.238"),
                            30,
                            Ip.parse("147.75.69.27"),
                            "sha1",
                            "aes-128-cbc",
                            28800,
                            "main",
                            "group2",
                            "84d71e5f49cce153c80a1f13b47989d25f2aa29d9bbc75624ab73435db792f87",
                            "hmac-sha1-96",
                            "aes-128-cbc",
                            3600,
                            "tunnel",
                            "group2",
                            "esp",
                            65401L,
                            Ip.parse("169.254.13.237"),
                            30,
                            Ip.parse("52.39.121.126"),
                            null)),
                    ImmutableList.of(),
                    ImmutableList.of(
                        new VgwTelemetry(5, Ip.parse("52.27.166.152"), "UP", "5 BGP ROUTES"),
                        new VgwTelemetry(5, Ip.parse("52.39.121.126"), "UP", "5 BGP ROUTES")),
                    false))));
  }
}
