package org.batfish.representation.aws;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.representation.aws.AwsConfiguration.vpnExternalInterfaceName;
import static org.batfish.representation.aws.AwsConfiguration.vpnInterfaceName;
import static org.batfish.representation.aws.AwsConfiguration.vpnTunnelId;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPN_CONNECTIONS;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.representation.aws.VpnConnection.GatewayType;
import org.junit.Test;

/** Tests for {@link VpnConnection} */
public class VpnConnectionTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/VpnConnectionTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available connections should show up.
     */
    assertThat(
        region.getVpnConnections(),
        equalTo(
            ImmutableMap.of(
                "vpn-ba2e34a8",
                new VpnConnection(
                    true,
                    "vpn-ba2e34a8",
                    "cgw-fb76ace5",
                    GatewayType.VPN,
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
                            Ip.parse("52.27.166.152")),
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
                            Ip.parse("52.39.121.126"))),
                    ImmutableList.of(),
                    ImmutableList.of(
                        new VgwTelemetry(5, Ip.parse("52.27.166.152"), "UP", "5 BGP ROUTES"),
                        new VgwTelemetry(5, Ip.parse("52.39.121.126"), "UP", "5 BGP ROUTES")),
                    false))));
  }

  @Test
  public void testDeserializationTransitGateway() throws IOException {
    String text =
        CommonUtil.readResource(
            "org/batfish/representation/aws/VpnConnectionTransitGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_VPN_CONNECTIONS);
    List<VpnConnection> vpnConnections = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      vpnConnections.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), VpnConnection.class));
    }

    assertThat(vpnConnections, equalTo(vpnConnections));

    assertThat(
        vpnConnections,
        equalTo(
            ImmutableList.of(
                new VpnConnection(
                    true,
                    "vpn-034885b776a3f8a80",
                    "cgw-09e5ca92b2f2d71cc",
                    GatewayType.TRANSIT,
                    "tgw-044be4464fcc69aff",
                    ImmutableList.of(
                        new IpsecTunnel(
                            65000L,
                            Ip.parse("169.254.195.142"),
                            30,
                            Ip.parse("2.2.2.2"),
                            "sha1",
                            "aes-128-cbc",
                            28800,
                            "main",
                            "group2",
                            "ede3364653dd277b63b140be0abf66290208d86be59162efac5adea69bbcdf1b",
                            "hmac-sha1-96",
                            "aes-128-cbc",
                            3600,
                            "tunnel",
                            "group2",
                            "esp",
                            64512L,
                            Ip.parse("169.254.195.141"),
                            30,
                            Ip.parse("3.130.56.250")),
                        new IpsecTunnel(
                            65000L,
                            Ip.parse("169.254.70.162"),
                            30,
                            Ip.parse("2.2.2.2"),
                            "sha1",
                            "aes-128-cbc",
                            28800,
                            "main",
                            "group2",
                            "29b2e8a12b8d59098f17913a7f7451518f16eae441aa3ee52c5cf6e49ae38f58",
                            "hmac-sha1-96",
                            "aes-128-cbc",
                            3600,
                            "tunnel",
                            "group2",
                            "esp",
                            64512L,
                            Ip.parse("169.254.70.161"),
                            30,
                            Ip.parse("3.133.183.196"))),
                    ImmutableList.of(),
                    ImmutableList.of(
                        new VgwTelemetry(0, Ip.parse("3.130.56.250"), "DOWN", "IPSEC IS DOWN"),
                        new VgwTelemetry(0, Ip.parse("3.133.183.196"), "DOWN", "IPSEC IS DOWN")),
                    false))));
  }

  @Test
  public void testApplyToGateway() {
    VpnGateway vgw = new VpnGateway("vpn", ImmutableList.of(), ImmutableMap.of());
    Configuration vgwConfig = Utils.newAwsConfiguration(vgw.getId(), "awstest");
    BgpProcess bgpProc =
        BgpProcess.builder()
            .setRouterId(Ip.parse("1.1.1.1"))
            .setVrf(vgwConfig.getDefaultVrf())
            .setAdminCostsToVendorDefaults(ConfigurationFormat.AWS)
            .build();

    IpsecTunnel ipsecTunnel =
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
            Ip.parse("52.27.166.152"));

    List<Prefix> staticRoutes = ImmutableList.of(Prefix.parse("2.2.2.2/23"));

    VpnConnection vpnConnection =
        new VpnConnection(
            true,
            "vpn-ba2e34a8",
            "cgw-fb76ace5",
            GatewayType.VPN,
            vgw.getId(),
            ImmutableList.of(ipsecTunnel),
            staticRoutes,
            ImmutableList.of(),
            false);

    Warnings warnings = new Warnings(true, true, true);
    vpnConnection.applyToGateway(
        vgwConfig, vgwConfig.getDefaultVrf(), "ExportPolicy", "ImportPolicy", warnings);

    // quick check to see if things processed fine
    assertTrue(warnings.getRedFlagWarnings().isEmpty());

    assertThat(
        vgwConfig.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(
                vpnExternalInterfaceName(vpnTunnelId(vpnConnection.getVpnConnectionId(), 1)),
                vpnInterfaceName(vpnTunnelId(vpnConnection.getVpnConnectionId(), 1)))));

    // TODO: check IPSec

    BgpActivePeerConfig bgpActivePeerConfig =
        getOnlyElement(vgwConfig.getDefaultVrf().getBgpProcess().getActiveNeighbors().values());

    assertThat(
        bgpActivePeerConfig,
        equalTo(
            BgpActivePeerConfig.builder()
                .setPeerAddress(ipsecTunnel.getCgwInsideAddress())
                .setRemoteAs(ipsecTunnel.getCgwBgpAsn())
                .setBgpProcess(bgpProc)
                .setLocalAs(ipsecTunnel.getVgwBgpAsn())
                .setLocalIp(ipsecTunnel.getVgwInsideAddress())
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder()
                        .setExportPolicy("ExportPolicy")
                        .setImportPolicy("ImportPolicy")
                        .build())
                .build()));

    assertThat(
        vgwConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            staticRoutes.stream()
                .map(pfx -> toStaticRoute(pfx, ipsecTunnel.getCgwInsideAddress()))
                .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()))));
  }
}
