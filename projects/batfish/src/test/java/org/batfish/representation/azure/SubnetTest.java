package org.batfish.representation.azure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class SubnetTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/azure/SubnetTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    Subnet subnet = BatfishObjectMapper.mapper().convertValue(node, Subnet.class);
    assertNotNull(subnet);

    assertEquals("SubnetTest", subnet.getName());

    assertEquals(
        "resourceGroups/test/providers/Microsoft.Network/virtualNetworks/VM1-vnet/subnets/default",
        subnet.getId());

    assertEquals("Microsoft.Network/virtualNetworks/subnets", subnet.getType());

    Subnet.Properties subnetProperties = subnet.getProperties();
    assertNotNull(subnetProperties);

    assertEquals(subnetProperties.getAddressPrefix(), Prefix.parse("10.0.0.0/24"));
  }

  @Test
  public void testToConfigurationNodes() {

    Region region = new Region("test");
    ConvertedConfiguration convertedConfiguration = new ConvertedConfiguration();

    Prefix addressPrefix = Prefix.parse("10.0.0.0/24");

    Subnet.Properties subnetProperties =
        new Subnet.Properties(addressPrefix, null, null, new HashSet<>(), false);

    Subnet subnet = new Subnet("testId", "testSubnet", "testType", subnetProperties);

    region.getSubnets().put(subnet.getId(), subnet);
    Configuration cfgNode = subnet.toConfigurationNode(region, convertedConfiguration);
    assertNotNull(cfgNode);

    assertEquals(cfgNode.getHostname(), subnet.getNodeName().toLowerCase());

    Map<String, Interface> interfaces = cfgNode.getAllInterfaces(cfgNode.getDefaultVrf().getName());
    assertNotNull(interfaces);

    // check 1 interface facing lan
    assertEquals(3, interfaces.size());
    assertNotNull(interfaces.get(subnet.getToLanInterfaceName()));
  }
}
