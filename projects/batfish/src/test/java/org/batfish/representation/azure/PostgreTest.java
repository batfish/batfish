package org.batfish.representation.azure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Test;

public class PostgreTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/azure/PostgreTest.json", UTF_8);
    JsonNode node = BatfishObjectMapper.mapper().readTree(text);

    Postgres postgres = BatfishObjectMapper.mapper().convertValue(node, Postgres.class);

    assertNotNull(postgres);

    assertEquals("testName", postgres.getName());
    assertEquals("testId", postgres.getId());
    assertEquals("Microsoft.DBforPostgreSQL/flexibleServers", postgres.getType());

    Postgres.Properties properties = postgres.getProperties();
    assertNotNull(properties);

    Postgres.Network network = properties.getNetwork();
    assertNotNull(network);

    assertEquals("subnetId", network.getDelegatedSubnetResourceId());
  }

  @Test
  public void testToConfigurationNode() {
    Region rgp = new Region("test");
    ConvertedConfiguration convertedConfiguration = new ConvertedConfiguration();

    Subnet subnet =
        new Subnet(
            "subnetId",
            "subnetName",
            "subnetType",
            new Subnet.Properties(Prefix.parse("10.0.0.0/24"), null, null, Set.of(), false));
    rgp.getSubnets().put(subnet.getId(), subnet);

    Postgres.Properties properties = new Postgres.Properties(new Postgres.Network(subnet.getId()));

    Postgres postgres = new Postgres("testId", "testName", "testType", properties);

    Configuration cfgNode = postgres.toConfigurationNode(rgp, convertedConfiguration);
    assertNotNull(cfgNode);

    assertEquals(1, cfgNode.getAllInterfaces().size());
    assertEquals(1, cfgNode.getDefaultVrf().getStaticRoutes().size());

    for (Interface iFace : cfgNode.getAllInterfaces().values()) {
      assertNotNull(iFace);
      assertEquals("to-subnet", iFace.getName());
      assertEquals(
          ConcreteInterfaceAddress.create(
              Ip.parse("10.0.0.2"), subnet.getProperties().getAddressPrefix().getPrefixLength()),
          iFace.getAddress());
    }

    assertNotNull(cfgNode.getDefaultVrf());
    for (StaticRoute st : cfgNode.getDefaultVrf().getStaticRoutes()) {
      assertNotNull(st);
      assertEquals(subnet.computeInstancesIfaceIp(), st.getNextHopIp());
      assertEquals(Prefix.ZERO, st.getNetwork());
      assertEquals(0, st.getMetric());
      assertEquals(0, st.getAdministrativeCost());
      ;
    }

    assertEquals(postgres.getCleanId().toLowerCase(), cfgNode.getHostname());
  }
}
