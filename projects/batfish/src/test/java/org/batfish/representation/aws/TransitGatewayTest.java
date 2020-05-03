package org.batfish.representation.aws;

import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.representation.aws.AwsConfiguration.vpnExternalInterfaceName;
import static org.batfish.representation.aws.AwsConfiguration.vpnTunnelId;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;
import static org.batfish.representation.aws.TransitGateway.connectVpc;
import static org.batfish.representation.aws.TransitGateway.createBgpProcess;
import static org.batfish.representation.aws.TransitGateway.supportedVpnBgpConfiguration;
import static org.batfish.representation.aws.TransitGateway.vrfNameForRouteTable;
import static org.batfish.representation.aws.TransitGatewayAttachment.STATE_ASSOCIATED;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.Vpc.vrfNameForLink;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.TransitGateway.TransitGatewayOptions;
import org.batfish.representation.aws.TransitGatewayAttachment.Association;
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;
import org.batfish.representation.aws.TransitGatewayPropagations.Propagation;
import org.batfish.representation.aws.TransitGatewayRoute.Type;
import org.batfish.representation.aws.VpnConnection.GatewayType;
import org.junit.Test;

/** Tests for {@link TransitGateway} */
public class TransitGatewayTest {

  private static IpsecTunnel _ipsecTunnel =
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

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/TransitGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getTransitGateways(),
        equalTo(
            ImmutableMap.of(
                "tgw-044be4464fcc69aff",
                new TransitGateway(
                    "tgw-044be4464fcc69aff",
                    new TransitGatewayOptions(
                        64512L,
                        true,
                        "tgw-rtb-0fa40c8df355dce6e",
                        true,
                        "tgw-rtb-0fa40c8df355dce6e",
                        true),
                    ImmutableMap.of(TAG_NAME, "transit-test")))));
  }

  @Test
  public void testConnectVpcAttachment() {
    String routeTableId = "tgw-rtb";
    TransitGateway tgw =
        new TransitGateway(
            "tgw",
            new TransitGatewayOptions(0L, true, routeTableId, true, "tgw-rtb", true),
            ImmutableMap.of());

    Prefix vpcPrefix = Prefix.parse("3.3.3.0/24");
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(vpcPrefix), ImmutableMap.of());

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment(
            "tgw-attach",
            tgw.getId(),
            ResourceType.VPC,
            vpc.getId(),
            new Association(routeTableId, STATE_ASSOCIATED));

    TransitGatewayVpcAttachment vpcAttachment =
        new TransitGatewayVpcAttachment(
            tgwAttachment.getId(), tgw.getId(), vpc.getId(), ImmutableList.of());

    Region region =
        Region.builder("region")
            .setTransitGateways(ImmutableMap.of(tgw.getId(), tgw))
            .setTransitGatewayAttachments(ImmutableMap.of(tgwAttachment.getId(), tgwAttachment))
            .setTransitGatewayVpcAttachments(ImmutableMap.of(vpcAttachment.getId(), vpcAttachment))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setTransitGatewayRouteTables(
                ImmutableMap.of(
                    routeTableId,
                    new TransitGatewayRouteTable(routeTableId, tgw.getId(), true, true)))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(vpcCfg.getHostname(), vpcCfg));

    Warnings warnings = new Warnings(true, true, true);
    Configuration tgwCfg = tgw.toConfigurationNode(awsConfiguration, region, warnings);
    assertThat(tgwCfg, hasDeviceModel(DeviceModel.AWS_TRANSIT_GATEWAY));

    // check that vrfs exist
    assertTrue(tgwCfg.getVrfs().containsKey(vrfNameForRouteTable(routeTableId)));
    assertTrue(vpcCfg.getVrfs().containsKey(vrfNameForLink(tgwAttachment.getId())));

    // check that interfaces are created in the right VRFs
    Interface tgwInterface =
        tgwCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(vpcCfg, routeTableId));
    Interface vpcInterface =
        vpcCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(tgwCfg, routeTableId));
    assertThat(tgwInterface, hasVrfName(vrfNameForRouteTable(routeTableId)));
    assertThat(vpcInterface, hasVrfName(vrfNameForLink(tgwAttachment.getId())));

    // check that VRFs have the right static routes
    assertThat(
        tgwCfg.getVrfs().get(vrfNameForRouteTable(routeTableId)).getStaticRoutes(),
        equalTo(ImmutableSet.of()));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForLink(tgwAttachment.getId())).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(vpcPrefix, NULL_INTERFACE_NAME)
                    .toBuilder()
                    .setAdministrativeCost(255)
                    .build(),
                toStaticRoute(
                    Prefix.ZERO,
                    Utils.interfaceNameToRemote(tgwCfg, routeTableId),
                    tgwInterface.getLinkLocalAddress().getIp()))));
  }

  /** Test that the VPC-TGW are correctly linked for the associated route table. */
  @Test
  public void testConnectVpc_associatedTable() {
    String tgwId = "tgw";
    String routeTableId = "tgw-rtb";

    Prefix vpcPrefix = Prefix.parse("3.3.3.0/24");
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(vpcPrefix), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(Vpc.nodeName(vpc.getId()), "aws");
    Configuration tgwCfg = Utils.newAwsConfiguration(tgwId, "aws");

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment(
            "tgw-attach",
            tgwId,
            ResourceType.VPC,
            vpc.getId(),
            new Association(routeTableId, STATE_ASSOCIATED));

    Region region =
        Region.builder("region")
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setTransitGatewayRouteTables(
                ImmutableMap.of(
                    routeTableId, new TransitGatewayRouteTable(routeTableId, tgwId, true, true)))
            .build();

    Vrf.builder().setName(vrfNameForRouteTable(routeTableId)).setOwner(tgwCfg).build();
    Vrf.builder().setOwner(vpcCfg).setName(vrfNameForLink(tgwAttachment.getId())).build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(vpcCfg.getHostname(), vpcCfg));
    connectVpc(tgwCfg, tgwAttachment, awsConfiguration, region, new Warnings());

    // check that interfaces are created in the right VRFs
    Interface tgwInterface =
        tgwCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(vpcCfg, routeTableId));
    Interface vpcInterface =
        vpcCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(tgwCfg, routeTableId));
    assertThat(tgwInterface, hasVrfName(vrfNameForRouteTable(routeTableId)));
    assertThat(vpcInterface, hasVrfName(vrfNameForLink(tgwAttachment.getId())));

    // check that VRFs have the right static routes
    assertThat(
        tgwCfg.getVrfs().get(vrfNameForRouteTable(routeTableId)).getStaticRoutes(),
        equalTo(ImmutableSet.of()));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForLink(tgwAttachment.getId())).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    Prefix.ZERO,
                    Utils.interfaceNameToRemote(tgwCfg, routeTableId),
                    tgwInterface.getLinkLocalAddress().getIp()))));
  }

  /**
   * Test that the VPC-TGW are correctly linked for propagated tables. There is no association table
   * in this test.
   */
  @Test
  public void testConnectVpc_progagatedTables() {
    String tgwId = "tgw";
    String routeTableId1 = "tgw-rtb1";
    String routeTableId2 = "tgw-rtb2";

    Prefix vpcPrefix = Prefix.parse("3.3.3.0/24");
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(vpcPrefix), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(Vpc.nodeName(vpc.getId()), "aws");
    Configuration tgwCfg = Utils.newAwsConfiguration(tgwId, "aws");

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment("tgw-attach", tgwId, ResourceType.VPC, vpc.getId(), null);

    Region region =
        Region.builder("region")
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setTransitGatewayRouteTables(
                ImmutableMap.of(
                    routeTableId1,
                    new TransitGatewayRouteTable(routeTableId1, tgwId, true, true),
                    routeTableId2,
                    new TransitGatewayRouteTable(routeTableId2, tgwId, false, false)))
            .setTransitGatewayPropagations(
                ImmutableMap.of(
                    routeTableId1,
                    new TransitGatewayPropagations(
                        routeTableId1,
                        ImmutableList.of(
                            new Propagation(
                                tgwAttachment.getId(), ResourceType.VPC, vpc.getId(), true))),
                    routeTableId2,
                    new TransitGatewayPropagations(
                        routeTableId2,
                        ImmutableList.of(
                            new Propagation(
                                tgwAttachment.getId(), ResourceType.VPC, vpc.getId(), true)))))
            .build();

    Vrf.builder().setName(vrfNameForRouteTable(routeTableId1)).setOwner(tgwCfg).build();
    Vrf.builder().setName(vrfNameForRouteTable(routeTableId2)).setOwner(tgwCfg).build();
    Vrf.builder().setOwner(vpcCfg).setName(vrfNameForLink(tgwAttachment.getId())).build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(vpcCfg.getHostname(), vpcCfg));
    connectVpc(tgwCfg, tgwAttachment, awsConfiguration, region, new Warnings());

    // check that interfaces are created and in the right VRFs
    assertThat(
        tgwCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(vpcCfg, routeTableId1)),
        hasVrfName(vrfNameForRouteTable(routeTableId1)));
    assertThat(
        vpcCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(tgwCfg, routeTableId1)),
        hasVrfName(vrfNameForLink(tgwAttachment.getId())));

    assertThat(
        tgwCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(vpcCfg, routeTableId2)),
        hasVrfName(vrfNameForRouteTable(routeTableId2)));
    assertThat(
        vpcCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(tgwCfg, routeTableId2)),
        hasVrfName(vrfNameForLink(tgwAttachment.getId())));
  }

  @Test
  public void testConnectStaticVpnAttachment() {

    String routeTableId = "tgw-rtb";
    TransitGateway tgw =
        new TransitGateway(
            "tgw",
            new TransitGatewayOptions(0L, true, routeTableId, true, "tgw-rtb", true),
            ImmutableMap.of());

    VpnConnection vpnConnection =
        new VpnConnection(
            false,
            "vpn",
            "cgw-fb76ace5",
            GatewayType.VPN,
            tgw.getId(),
            ImmutableList.of(_ipsecTunnel),
            ImmutableList.of(),
            ImmutableList.of(),
            true);

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment(
            "tgw-attach",
            tgw.getId(),
            ResourceType.VPN,
            vpnConnection.getId(),
            new Association(routeTableId, STATE_ASSOCIATED));

    Region region =
        Region.builder("region")
            .setTransitGateways(ImmutableMap.of(tgw.getId(), tgw))
            .setTransitGatewayAttachments(ImmutableMap.of(tgwAttachment.getId(), tgwAttachment))
            .setVpnConnections(ImmutableMap.of(vpnConnection.getId(), vpnConnection))
            .setTransitGatewayRouteTables(
                ImmutableMap.of(
                    routeTableId,
                    new TransitGatewayRouteTable(routeTableId, tgw.getId(), true, true)))
            .build();

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    Warnings warnings = new Warnings(true, true, true);
    Configuration tgwCfg = tgw.toConfigurationNode(awsConfiguration, region, warnings);
    assertThat(tgwCfg, hasDeviceModel(DeviceModel.AWS_TRANSIT_GATEWAY));

    // check that the vrf exists
    assertTrue(tgwCfg.getVrfs().containsKey(vrfNameForRouteTable(routeTableId)));

    // if applyGateway was called with the right params, such an interface must exist
    Interface tgwInterface =
        tgwCfg
            .getAllInterfaces()
            .get(vpnExternalInterfaceName(vpnTunnelId(vpnConnection.getId(), 1)));
    assertThat(tgwInterface, hasVrfName(vrfNameForRouteTable(routeTableId)));
  }

  @Test
  public void testConnectBgpVpnAttachment() {

    String routeTableId = "tgw-rtb";
    TransitGateway tgw =
        new TransitGateway(
            "tgw",
            new TransitGatewayOptions(0L, true, routeTableId, true, "tgw-rtb", true),
            ImmutableMap.of(TAG_NAME, "tgw-name"));

    VpnConnection vpnConnection =
        new VpnConnection(
            true,
            "vpn",
            "cgw-fb76ace5",
            GatewayType.VPN,
            tgw.getId(),
            ImmutableList.of(_ipsecTunnel),
            ImmutableList.of(),
            ImmutableList.of(),
            false);

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment(
            "tgw-attach",
            tgw.getId(),
            ResourceType.VPN,
            vpnConnection.getId(),
            new Association(routeTableId, STATE_ASSOCIATED));

    TransitGatewayPropagations associatedPropagation =
        new TransitGatewayPropagations(
            routeTableId,
            ImmutableList.of(
                new Propagation(
                    tgwAttachment.getId(), ResourceType.VPN, vpnConnection.getId(), true)));

    Region region =
        Region.builder("region")
            .setTransitGateways(ImmutableMap.of(tgw.getId(), tgw))
            .setTransitGatewayAttachments(ImmutableMap.of(tgwAttachment.getId(), tgwAttachment))
            .setVpnConnections(ImmutableMap.of(vpnConnection.getId(), vpnConnection))
            .setTransitGatewayPropagations(ImmutableMap.of(routeTableId, associatedPropagation))
            .setTransitGatewayRouteTables(
                ImmutableMap.of(
                    routeTableId,
                    new TransitGatewayRouteTable(routeTableId, tgw.getId(), true, true)))
            .build();

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    Warnings warnings = new Warnings(true, true, true);
    Configuration tgwCfg = tgw.toConfigurationNode(awsConfiguration, region, warnings);
    assertThat(tgwCfg.getHumanName(), equalTo("tgw-name"));
    assertThat(tgwCfg, hasDeviceModel(DeviceModel.AWS_TRANSIT_GATEWAY));

    // check that the vrf exists
    assertTrue(tgwCfg.getVrfs().containsKey(vrfNameForRouteTable(routeTableId)));

    // check that BGP process exists
    assertThat(
        tgwCfg.getVrfs().get(vrfNameForRouteTable(routeTableId)).getBgpProcess(), notNullValue());

    // if applyGateway was called with the right params, such an interface must exist
    Interface tgwInterface =
        tgwCfg
            .getAllInterfaces()
            .get(vpnExternalInterfaceName(vpnTunnelId(vpnConnection.getId(), 1)));
    assertThat(tgwInterface, hasVrfName(vrfNameForRouteTable(routeTableId)));
  }

  @Test
  public void testCreateBgpProcess() {
    Configuration tgwCfg = Utils.newAwsConfiguration("tgw", "local");
    Vrf vrf = Vrf.builder().setOwner(tgwCfg).setName("vrf").build();
    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    createBgpProcess(tgwCfg, vrf, awsConfiguration);

    // interface exists
    Interface bgpInterface = tgwCfg.getAllInterfaces().get("bgp-loopback-" + vrf.getName());
    assertThat(bgpInterface, hasVrfName(vrf.getName()));

    assertThat(vrf.getBgpProcess(), notNullValue());

    // TODO: check on routing policies once settled
  }

  @Test
  public void testPropagateRoutesVpc() {
    String routeTableId = "tgw-rtb"; // propagation table
    TransitGateway tgw =
        new TransitGateway(
            "tgw",
            new TransitGatewayOptions(0L, true, routeTableId, true, "tgw-rtb", true),
            ImmutableMap.of());

    Prefix vpcPrefix = Prefix.parse("2.2.2.2/32");
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(vpcPrefix), ImmutableMap.of()); // no prefix

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment(
            "tgw-attach",
            tgw.getId(),
            ResourceType.VPC,
            vpc.getId(),
            new Association(routeTableId, STATE_ASSOCIATED));

    TransitGatewayVpcAttachment vpcAttachment =
        new TransitGatewayVpcAttachment(
            tgwAttachment.getId(), tgw.getId(), vpc.getId(), ImmutableList.of());

    TransitGatewayRouteTable routeTable =
        new TransitGatewayRouteTable(routeTableId, tgw.getId(), true, true);

    TransitGatewayPropagations propagations =
        new TransitGatewayPropagations(
            routeTableId,
            ImmutableList.of(
                new Propagation(tgwAttachment.getId(), ResourceType.VPC, vpc.getId(), true),
                new Propagation("dummy", ResourceType.VPC, "dummy", false)));

    Region region =
        Region.builder("region")
            .setTransitGateways(ImmutableMap.of(tgw.getId(), tgw))
            .setTransitGatewayAttachments(ImmutableMap.of(tgwAttachment.getId(), tgwAttachment))
            .setTransitGatewayVpcAttachments(ImmutableMap.of(vpcAttachment.getId(), vpcAttachment))
            .setTransitGatewayRouteTables(ImmutableMap.of(routeTableId, routeTable))
            .setTransitGatewayPropagations(ImmutableMap.of(routeTableId, propagations))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());
    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(vpcCfg.getHostname(), vpcCfg));

    Warnings warnings = new Warnings(true, true, true);
    Configuration tgwCfg = tgw.toConfigurationNode(awsConfiguration, region, warnings);
    assertThat(tgwCfg, hasDeviceModel(DeviceModel.AWS_TRANSIT_GATEWAY));

    // check that vrf exists
    assertTrue(tgwCfg.getVrfs().containsKey(vrfNameForRouteTable(routeTableId)));

    // check that VRFs have the right static routes
    assertThat(
        tgwCfg.getVrfs().get(vrfNameForRouteTable(routeTableId)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    vpcPrefix,
                    tgwCfg
                        .getAllInterfaces()
                        .get(Utils.interfaceNameToRemote(vpcCfg, routeTableId))
                        .getName(),
                    vpcCfg
                        .getAllInterfaces()
                        .get(Utils.interfaceNameToRemote(tgwCfg, routeTableId))
                        .getLinkLocalAddress()
                        .getIp()))));
  }

  @Test
  public void testStaticRoutesVpc() {
    String routeTableId = "tgw-rtb";
    TransitGateway tgw =
        new TransitGateway(
            "tgw",
            new TransitGatewayOptions(0L, true, routeTableId, true, "tgw-rtb", true),
            ImmutableMap.of());

    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of()); // no prefix

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment(
            "tgw-attach",
            tgw.getId(),
            ResourceType.VPC,
            vpc.getId(),
            new Association(routeTableId, STATE_ASSOCIATED));

    TransitGatewayVpcAttachment vpcAttachment =
        new TransitGatewayVpcAttachment(
            tgwAttachment.getId(), tgw.getId(), vpc.getId(), ImmutableList.of());

    TransitGatewayRouteTable routeTable =
        new TransitGatewayRouteTable(routeTableId, tgw.getId(), true, true);

    Prefix activeRoutePrefix = Prefix.parse("6.6.6.6/32");
    Prefix blackholeRoutePrefix = Prefix.parse("9.9.9.9/32");
    TransitGatewayStaticRoutes staticRoutes =
        new TransitGatewayStaticRoutes(
            routeTableId,
            ImmutableList.of(
                new TransitGatewayRouteV4(
                    activeRoutePrefix,
                    State.ACTIVE,
                    Type.STATIC,
                    ImmutableList.of(tgwAttachment.getId())),
                new TransitGatewayRouteV4(
                    Prefix.parse("9.9.9.9/32"),
                    State.BLACKHOLE,
                    Type.STATIC,
                    ImmutableList.of(tgwAttachment.getId()))));

    Region region =
        Region.builder("region")
            .setTransitGateways(ImmutableMap.of(tgw.getId(), tgw))
            .setTransitGatewayAttachments(ImmutableMap.of(tgwAttachment.getId(), tgwAttachment))
            .setTransitGatewayVpcAttachments(ImmutableMap.of(vpcAttachment.getId(), vpcAttachment))
            .setTransitGatewayRouteTables(ImmutableMap.of(routeTableId, routeTable))
            .setTransitGatewayStaticRoutes(ImmutableMap.of(routeTableId, staticRoutes))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());
    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(vpcCfg.getHostname(), vpcCfg));

    Warnings warnings = new Warnings(true, true, true);
    Configuration tgwCfg = tgw.toConfigurationNode(awsConfiguration, region, warnings);
    assertThat(tgwCfg, hasDeviceModel(DeviceModel.AWS_TRANSIT_GATEWAY));

    // check that vrf exists
    assertTrue(tgwCfg.getVrfs().containsKey(vrfNameForRouteTable(routeTableId)));

    // check that VRFs have the right static routes
    assertThat(
        tgwCfg.getVrfs().get(vrfNameForRouteTable(routeTableId)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    activeRoutePrefix,
                    Utils.interfaceNameToRemote(vpcCfg, routeTableId),
                    Utils.getInterfaceLinkLocalIp(
                        vpcCfg, Utils.interfaceNameToRemote(tgwCfg, routeTableId))),
                toStaticRoute(blackholeRoutePrefix, NULL_INTERFACE_NAME))));
  }

  @Test
  public void testStaticRoutesVpn() {

    String routeTableId = "tgw-rtb";
    TransitGateway tgw =
        new TransitGateway(
            "tgw",
            new TransitGatewayOptions(0L, true, routeTableId, true, "tgw-rtb", true),
            ImmutableMap.of());

    VpnConnection vpnConnection =
        new VpnConnection(
            true,
            "vpn",
            "cgw-fb76ace5",
            GatewayType.VPN,
            tgw.getId(),
            ImmutableList.of(_ipsecTunnel),
            ImmutableList.of(),
            ImmutableList.of(),
            false);

    TransitGatewayAttachment tgwAttachment =
        new TransitGatewayAttachment(
            "tgw-attach",
            tgw.getId(),
            ResourceType.VPN,
            vpnConnection.getId(),
            new Association(routeTableId, STATE_ASSOCIATED));

    TransitGatewayRouteTable routeTable =
        new TransitGatewayRouteTable(routeTableId, tgw.getId(), true, true);

    Prefix activeRoutePrefix = Prefix.parse("6.6.6.6/32");
    TransitGatewayStaticRoutes staticRoutes =
        new TransitGatewayStaticRoutes(
            routeTableId,
            ImmutableList.of(
                new TransitGatewayRouteV4(
                    activeRoutePrefix,
                    State.ACTIVE,
                    Type.STATIC,
                    ImmutableList.of(tgwAttachment.getId()))));

    Region region =
        Region.builder("region")
            .setTransitGateways(ImmutableMap.of(tgw.getId(), tgw))
            .setTransitGatewayAttachments(ImmutableMap.of(tgwAttachment.getId(), tgwAttachment))
            .setVpnConnections(ImmutableMap.of(vpnConnection.getId(), vpnConnection))
            .setTransitGatewayRouteTables(ImmutableMap.of(routeTableId, routeTable))
            .setTransitGatewayStaticRoutes(ImmutableMap.of(routeTableId, staticRoutes))
            .build();

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    Warnings warnings = new Warnings(true, true, true);
    Configuration tgwCfg = tgw.toConfigurationNode(awsConfiguration, region, warnings);
    assertThat(tgwCfg, hasDeviceModel(DeviceModel.AWS_TRANSIT_GATEWAY));

    // check that the vrf exists
    assertTrue(tgwCfg.getVrfs().containsKey(vrfNameForRouteTable(routeTableId)));

    assertThat(
        tgwCfg.getVrfs().get(vrfNameForRouteTable(routeTableId)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(toStaticRoute(activeRoutePrefix, _ipsecTunnel.getCgwInsideAddress()))));
  }

  @Test
  public void testSupportedVpnConfiguration() {
    String routeTableId = "tgw-rt";
    TransitGatewayAttachment attachment =
        new TransitGatewayAttachment(
            "attachment",
            "tgw",
            ResourceType.VPN,
            "vpn",
            new Association(routeTableId, "associated"));
    VpnConnection vpnConnection =
        new VpnConnection(
            true,
            attachment.getResourceId(),
            "cust",
            GatewayType.TRANSIT,
            attachment.getGatewayId(),
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(),
            false);

    TransitGatewayPropagations associatedPropagation =
        new TransitGatewayPropagations(
            routeTableId,
            ImmutableList.of(
                new Propagation(
                    attachment.getId(), ResourceType.VPN, vpnConnection.getId(), true)));

    TransitGatewayPropagations disabledPropagation =
        new TransitGatewayPropagations(
            routeTableId,
            ImmutableList.of(
                new Propagation(
                    attachment.getId(), ResourceType.VPN, vpnConnection.getId(), false)));

    TransitGatewayPropagations otherPropagation =
        new TransitGatewayPropagations(
            "other-rt",
            ImmutableList.of(
                new Propagation(
                    attachment.getId(), ResourceType.VPN, vpnConnection.getId(), true)));

    // only associated propagation
    assertFalse(
        supportedVpnBgpConfiguration(
                attachment,
                vpnConnection,
                Region.builder("region")
                    .setTransitGatewayPropagations(
                        ImmutableMap.of(routeTableId, associatedPropagation))
                    .build())
            .isPresent());

    // disabled propagation
    assertTrue(
        supportedVpnBgpConfiguration(
                attachment,
                vpnConnection,
                Region.builder("region")
                    .setTransitGatewayPropagations(
                        ImmutableMap.of(routeTableId, disabledPropagation))
                    .build())
            .isPresent());

    // different propagation
    assertTrue(
        supportedVpnBgpConfiguration(
                attachment,
                vpnConnection,
                Region.builder("region")
                    .setTransitGatewayPropagations(ImmutableMap.of("other-rt", otherPropagation))
                    .build())
            .isPresent());

    // multiple propagation
    assertTrue(
        supportedVpnBgpConfiguration(
                attachment,
                vpnConnection,
                Region.builder("region")
                    .setTransitGatewayPropagations(
                        ImmutableMap.of(
                            routeTableId, associatedPropagation, "other-rt", otherPropagation))
                    .build())
            .isPresent());

    // no propagation
    assertTrue(
        supportedVpnBgpConfiguration(
                attachment,
                vpnConnection,
                Region.builder("region").setTransitGatewayPropagations(ImmutableMap.of()).build())
            .isPresent());
  }
}
