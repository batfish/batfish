package org.batfish.representation.azure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.junit.Test;

public class NatGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/azure/NatGatewayTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    NatGateway natGateway = BatfishObjectMapper.mapper().convertValue(node, NatGateway.class);
    assertNotNull(natGateway);

    assertEquals("testName", natGateway.getName());
    assertEquals("testId", natGateway.getId());
    assertEquals("Microsoft.Network/natGateways", natGateway.getType());

    NatGateway.Properties properties = natGateway.getProperties();
    assertNotNull(properties);

    assertNotNull(properties.getPublicIpAddresses());
    for (IdReference idReference : properties.getPublicIpAddresses()) {
      assertNotNull(idReference);
      assertNotNull(idReference.getId());
      assertEquals("testPublicIpAddressId", idReference.getId());
    }

    for (IdReference idReference : properties.getPublicIpPrefixes()) {
      assertNotNull(idReference);
      assertNotNull(idReference.getId());
      assertEquals("testPublicPrefixId", idReference.getId());
    }

    for (IdReference idReference : properties.getSubnets()) {
      assertNotNull(idReference);
      assertNotNull(idReference.getId());
      assertEquals("testSubnetId", idReference.getId());
    }
  }

  @Test
  public void toConfigurationNodeNoIp() {

    Region region = new Region("test");
    ConvertedConfiguration convertedConfiguration = new ConvertedConfiguration();

    PublicIpAddress publicIpAddress =
        new PublicIpAddress(
            "testPublicIpId",
            "testPublicIpName",
            "Microsoft.Network/publicIPAddresses",
            new PublicIpAddress.Properties(Ip.parse("1.1.1.1")));
    region.getPublicIpAddresses().put(publicIpAddress.getId(), publicIpAddress);

    String natGatewayId = "natGatewayId";

    NatGateway.Properties natGatewayProperties =
        new NatGateway.Properties(
            Set.of(new IdReference("testPublicIpId")),
            Set.of(),
            Set.of(new IdReference("subnetId")));

    NatGateway natGateway =
        new NatGateway(
            natGatewayId, "natGatwayName", "Microsoft.Network/natGateways", natGatewayProperties);
    region.getNatGateways().put(natGateway.getId(), natGateway);

    Configuration cfgNode = natGateway.toConfigurationNode(region, convertedConfiguration);
    assertNotNull(cfgNode);
    assertEquals(natGateway.getNodeName(), cfgNode.getHostname());
    convertedConfiguration.addNode(cfgNode);

    Subnet.Properties subnetProperties =
        new Subnet.Properties(
            Prefix.parse("192.168.1.0/24"), null, natGatewayId, new HashSet<>(), false);

    Subnet subnet =
        new Subnet(
            "subnetId",
            "subnetName",
            "Microsoft.Network/virtualNetworks/subnets",
            subnetProperties);

    region.getSubnets().put(subnet.getId(), subnet);
    convertedConfiguration.addNode(subnet.toConfigurationNode(region, convertedConfiguration));

    Interface toSubnet = cfgNode.getAllInterfaces().get(subnet.getCleanId());
    assertNotNull(toSubnet);

    Interface toInternet = cfgNode.getAllInterfaces().get(natGateway.getBackboneIfaceName());
    assertNotNull(toInternet);

    Transformation snatTransformation = toInternet.getOutgoingTransformation();
    assertNotNull(snatTransformation);

    assertEquals(
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP)
                        .build()))
            .apply(
                TransformationStep.assignSourceIp(publicIpAddress.getProperties().getIpAddress()),
                TransformationStep.assignSourcePort(1024, 65525))
            .build(),
        snatTransformation);

    Vrf vrf = cfgNode.getDefaultVrf();
    assertNotNull(vrf);

    for (StaticRoute st : vrf.getStaticRoutes()) {
      if (st.getNetwork().equals(subnet.getProperties().getAddressPrefix())) {
        assertEquals(subnet.getCleanId(), st.getNextHopInterface());
        assertEquals(AzureConfiguration.LINK_LOCAL_IP, st.getNextHopIp());
      }
    }
  }

  @Test
  public void testToConfigurationNodeWithVmPublicIp() {
    Region region = new Region("test");
    ConvertedConfiguration convertedConfiguration = new ConvertedConfiguration();

    String natGatewayId = "natGatewayId";
    NatGateway.Properties natGatewayProperties =
        new NatGateway.Properties(Set.of(), Set.of(), Set.of(new IdReference("subnetId")));

    NatGateway natGateway =
        new NatGateway(
            natGatewayId, "natGatwayName", "Microsoft.Network/natGateways", natGatewayProperties);

    region.getNatGateways().put(natGateway.getId(), natGateway);
    Configuration cfgNode = natGateway.toConfigurationNode(region, convertedConfiguration);
    convertedConfiguration.addNode(cfgNode);
    assertNotNull(cfgNode);
    assertEquals(natGateway.getNodeName(), cfgNode.getHostname());

    String subnetId = "subnetId";

    PublicIpAddress publicIpAddress =
        new PublicIpAddress(
            "testPublicIpId",
            "testPublicIpName",
            "Microsoft.Network/publicIPAddresses",
            new PublicIpAddress.Properties(Ip.parse("1.1.1.1")));
    region.getPublicIpAddresses().put(publicIpAddress.getId(), publicIpAddress);

    IPConfiguration ipConfiguration =
        new IPConfiguration(
            "ipconfigName",
            "Microsoft.Network/networkInterfaces/ipConfigurations",
            "ipconfigId",
            new IPConfiguration.Properties(
                Ip.parse("192.168.0.1"),
                new IdReference(subnetId),
                new IdReference(publicIpAddress.getId()),
                true));
    region.getIpConfigurations().put(ipConfiguration.getId().toLowerCase(), ipConfiguration);

    ipConfiguration.advertisePublicIpIfAny(region, convertedConfiguration, natGateway);

    Subnet.Properties subnetProperties =
        new Subnet.Properties(
            Prefix.parse("192.168.1.0/24"),
            null,
            natGatewayId,
            Set.of(new IdReference(ipConfiguration.getId())),
            false);

    Subnet subnet =
        new Subnet(
            "subnetId",
            "subnetName",
            "Microsoft.Network/virtualNetworks/subnets",
            subnetProperties);

    region.getSubnets().put(subnet.getId(), subnet);
    convertedConfiguration.addNode(subnet.toConfigurationNode(region, convertedConfiguration));

    Interface toSubnet = cfgNode.getAllInterfaces().get(subnet.getCleanId());
    assertNotNull(toSubnet);

    Interface toInternet = cfgNode.getAllInterfaces().get(natGateway.getBackboneIfaceName());
    assertNotNull(toInternet);

    Transformation snatTransformation = toInternet.getOutgoingTransformation();
    assertNotNull(snatTransformation);

    assertEquals(
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP)
                        .setSrcIps(
                            ipConfiguration.getProperties().getPrivateIpAddress().toIpSpace())
                        .build()))
            .apply(
                TransformationStep.assignSourceIp(publicIpAddress.getProperties().getIpAddress()))
            .build(),
        snatTransformation);

    Transformation dnatTransformation = toInternet.getIncomingTransformation();
    assertNotNull(dnatTransformation);
    assertEquals(
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP)
                        .setDstIps(publicIpAddress.getProperties().getIpAddress().toIpSpace())
                        .build()))
            .apply(
                TransformationStep.assignDestinationIp(
                    ipConfiguration.getProperties().getPrivateIpAddress(),
                    ipConfiguration.getProperties().getPrivateIpAddress()))
            .build(),
        dnatTransformation);

    Vrf vrf = cfgNode.getDefaultVrf();
    assertNotNull(vrf);

    boolean found1 = false;
    boolean found2 = false;
    for (StaticRoute st : vrf.getStaticRoutes()) {
      if (st.getNetwork().equals(subnet.getProperties().getAddressPrefix())) {
        found1 = true;
        assertEquals(subnet.getCleanId(), st.getNextHopInterface());
        assertEquals(AzureConfiguration.LINK_LOCAL_IP, st.getNextHopIp());
      }
      if (st.getNetwork().equals(publicIpAddress.getProperties().getIpAddress().toPrefix())) {
        found2 = true;
        assertEquals(NULL_INTERFACE_NAME, st.getNextHopInterface());
      }
    }
    assertTrue(found1);
    assertTrue(found2);
  }
}
