package org.batfish.representation.azure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.junit.Test;

public class ContainerGroupTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/azure/ContainerGroupTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    ContainerGroup containerGroup =
        BatfishObjectMapper.mapper().convertValue(node, ContainerGroup.class);
    assertNotNull(containerGroup);

    assertEquals("testName", containerGroup.getName());
    assertEquals("testId", containerGroup.getId());
    assertEquals("Microsoft.ContainerInstance/containerGroups", containerGroup.getType());

    ContainerGroup.Properties properties = containerGroup.getProperties();
    assertNotNull(properties);

    ContainerGroup.IpAddress ipAddress = properties.getIpAddress();
    assertNotNull(ipAddress);

    assertEquals(Ip.parse("10.0.1.4"), ipAddress.getIp());
    assertEquals(
        Set.of(
            new ContainerGroup.Port(IpProtocol.UDP, 443),
            new ContainerGroup.Port(IpProtocol.UDP, 80),
            new ContainerGroup.Port(IpProtocol.TCP, 443),
            new ContainerGroup.Port(IpProtocol.TCP, 80)),
        ipAddress.getPorts());

    assertEquals(1, properties.getSubnetIds().size());
    for (IdReference idReference : properties.getSubnetIds()) {
      assertEquals("testSubnetId", idReference.getId());
    }

    boolean found1 = false;
    boolean found2 = false;
    for (ContainerInstance containerInstance : properties.getContainers()) {
      if (containerInstance.getName().equals("test1")) {
        found1 = true;
        ContainerInstance.Properties containerProperties = containerInstance.getProperties();
        assertNotNull(containerProperties);
        assertEquals(
            Set.of(
                new ContainerInstance.Port(IpProtocol.TCP, 80),
                new ContainerInstance.Port(IpProtocol.TCP, 443)),
            containerProperties.getPorts());
      } else {
        found2 = true;
        ContainerInstance.Properties containerProperties = containerInstance.getProperties();
        assertNotNull(containerProperties);
        assertEquals(
            Set.of(
                new ContainerInstance.Port(IpProtocol.UDP, 80),
                new ContainerInstance.Port(IpProtocol.UDP, 443)),
            containerProperties.getPorts());
      }
    }

    assertTrue(found1);
    assertTrue(found2);
  }

  @Test
  public void testConfigurationNode() {

    Region region = new Region("test");
    ConvertedConfiguration convertedConfiguration = new ConvertedConfiguration();

    Subnet subnet =
        new Subnet(
            "testSubnetId",
            "testSubnetName",
            "testSubnetType",
            new Subnet.Properties(Prefix.parse("10.0.1.0/24"), null, null, Set.of(), false));

    region.getSubnets().put(subnet.getId(), subnet);

    ContainerInstance containerInstance =
        new ContainerInstance(
            "test",
            new ContainerInstance.Properties(
                Set.of(new ContainerInstance.Port(IpProtocol.TCP, 80))));

    ContainerGroup containerGroup =
        new ContainerGroup(
            "testId",
            "testName",
            "testType",
            new ContainerGroup.Properties(
                Set.of(containerInstance),
                new ContainerGroup.IpAddress(
                    Ip.parse("10.0.1.4"), Set.of(new ContainerGroup.Port(IpProtocol.TCP, 80))),
                Set.of(new IdReference(subnet.getId()))));

    Configuration containerGroupNode =
        containerGroup.toConfigurationNode(region, convertedConfiguration);
    assertNotNull(containerGroupNode);

    assertEquals(containerGroup.getCleanId(), containerGroupNode.getHostname());
    assertNotNull(containerGroupNode.getDefaultVrf());
    assertNotNull(containerGroupNode.getDefaultVrf().getStaticRoutes());
    assertEquals(1, containerGroupNode.getDefaultVrf().getStaticRoutes().size());
    for (StaticRoute staticRoute : containerGroupNode.getDefaultVrf().getStaticRoutes()) {
      assertEquals(0, staticRoute.getMetric());
      assertEquals(0, staticRoute.getAdministrativeCost());
      assertEquals(Prefix.ZERO, staticRoute.getNetwork());
      assertEquals(subnet.computeInstancesIfaceIp(), staticRoute.getNextHopIp());
    }

    Interface toSubnet = containerGroupNode.getAllInterfaces().get("to-subnet");
    assertNotNull(toSubnet);
    assertNotNull(toSubnet.getConcreteAddress());
    assertEquals(Ip.parse("10.0.1.4"), toSubnet.getConcreteAddress().getIp());

    Transformation snatTransformation = toSubnet.getOutgoingTransformation();
    assertNotNull(snatTransformation);
    assertEquals(
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP)
                        .build()))
            .apply(
                TransformationStep.assignSourceIp(Ip.parse("10.0.1.4")),
                TransformationStep.assignSourcePort(1024, 65525))
            .build(),
        snatTransformation);

    Transformation dnatTransformation = toSubnet.getIncomingTransformation();
    assertNotNull(dnatTransformation);
    assertEquals(
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstPorts(new SubRange(80))
                        .setIpProtocols(IpProtocol.TCP)
                        .build()))
            .apply(TransformationStep.assignDestinationIp(Ip.parse("172.17.0.2")))
            .build(),
        dnatTransformation);

    Configuration containerInstanceNode =
        convertedConfiguration.getNode(
            containerGroup.getCleanId() + "_" + containerInstance.getName());

    assertNotNull(containerInstanceNode);
    assertNotNull(containerInstanceNode.getDefaultVrf());
    assertNotNull(containerInstanceNode.getDefaultVrf().getStaticRoutes());
    assertEquals(1, containerInstanceNode.getDefaultVrf().getStaticRoutes().size());

    for (StaticRoute staticRoute : containerInstanceNode.getDefaultVrf().getStaticRoutes()) {
      assertEquals(0, staticRoute.getMetric());
      assertEquals(0, staticRoute.getAdministrativeCost());
      assertEquals(Prefix.ZERO, staticRoute.getNetwork());
      assertEquals(Ip.parse("172.17.0.1"), staticRoute.getNextHopIp());
    }
  }
}
