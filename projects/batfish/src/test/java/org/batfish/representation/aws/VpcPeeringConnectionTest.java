package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTIONS;
import static org.batfish.representation.aws.Utils.suffixedInterfaceName;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.hamcrest.Matchers;
import org.junit.Test;

/** Tests for {@link VpcPeeringConnection} */
public class VpcPeeringConnectionTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/VpcPeeringConnectionsTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_VPC_PEERING_CONNECTIONS);
    List<VpcPeeringConnection> vpcPeeringConnections = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      vpcPeeringConnections.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), VpcPeeringConnection.class));
    }

    assertThat(
        vpcPeeringConnections,
        equalTo(
            ImmutableList.of(
                new VpcPeeringConnection(
                    "pcx-f754069e",
                    "vpc-f6c5c790",
                    ImmutableList.of(Prefix.parse("10.199.100.0/24")),
                    "vpc-07acbc61",
                    ImmutableList.of(
                        Prefix.parse("10.130.0.0/16"), Prefix.parse("10.131.0.0/16"))))));
  }

  @Test
  public void testIgnoreNonActive() throws IOException {
    String text =
        CommonUtil.readResource(
            "org/batfish/representation/aws/VpcPeeringConnectionsTestIgnoreNonActive.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * We should have an entry for the vpc peering connection with status code "active", but not
     * for the one with status code "deleted".
     */
    assertThat(region.getVpcPeeringConnections(), hasKey("pcx-f754069e"));
    assertThat(region.getVpcPeeringConnections(), not(hasKey("pcx-4ee8b427")));
    assertThat(region.getVpcPeeringConnections(), not(hasKey("pcx-0ff752226f8e366f2")));
  }

  @Test
  public void testCreateConnection() {
    Vpc vpc1 = new Vpc("vpc1", ImmutableSet.of());
    Configuration vpc1Cfg = Utils.newAwsConfiguration(vpc1.getId(), "awstest");
    Prefix vpc1Prefix = Prefix.parse("10.10.10.0/24");

    Vpc vpc2 = new Vpc("vpc2", ImmutableSet.of());
    Configuration vpc2Cfg = Utils.newAwsConfiguration(vpc2.getId(), "awstest");
    Prefix vpc2Prefix = Prefix.parse("10.10.20.0/24");

    // add a static route to both
    VpcPeeringConnection connection =
        new VpcPeeringConnection(
            "connection",
            vpc1.getId(),
            ImmutableList.of(vpc1Prefix),
            vpc2.getId(),
            ImmutableList.of(vpc2Prefix));

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(
            ImmutableMap.of(
                Vpc.nodeName(vpc1.getId()), vpc1Cfg, Vpc.nodeName(vpc2.getId()), vpc2Cfg));

    Map<String, Region> regions =
        ImmutableMap.of(
            "r1",
            Region.builder("r1")
                .setVpcs(ImmutableMap.of(vpc1.getId(), vpc1, vpc2.getId(), vpc2))
                .build());

    connection.createConnection(regions, awsConfiguration, new Warnings());

    String vrfName = Vpc.vrfNameForLink(connection.getId());

    assertThat(vpc1Cfg, hasVrf(vrfName, Matchers.any(Vrf.class)));
    assertThat(vpc2Cfg, hasVrf(vrfName, Matchers.any(Vrf.class)));

    assertThat(
        vpc1Cfg.getVrfs().get(vrfName).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    vpc2Prefix,
                    Utils.getInterfaceIp(
                        vpc2Cfg, suffixedInterfaceName(vpc1Cfg, connection.getId()))))));
    assertThat(
        vpc2Cfg.getVrfs().get(vrfName).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    vpc1Prefix,
                    Utils.getInterfaceIp(
                        vpc1Cfg, suffixedInterfaceName(vpc2Cfg, connection.getId()))))));
  }
}
