package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DOMAIN_STATUS_LIST;
import static org.batfish.representation.aws.matchers.ElasticsearchDomainMatchers.hasAvailable;
import static org.batfish.representation.aws.matchers.ElasticsearchDomainMatchers.hasId;
import static org.batfish.representation.aws.matchers.ElasticsearchDomainMatchers.hasSecurityGroups;
import static org.batfish.representation.aws.matchers.ElasticsearchDomainMatchers.hasSubnets;
import static org.batfish.representation.aws.matchers.ElasticsearchDomainMatchers.hasVpcId;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ElasticsearchDomainTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  private StaticRoute.Builder _staticRouteBuilder;

  @Before
  public void setup() {
    _staticRouteBuilder =
        StaticRoute.builder()
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
            .setNetwork(Prefix.ZERO);
  }

  public Map<String, Configuration> loadAwsConfigurations() throws IOException {
    List<String> awsFilenames =
        ImmutableList.of(
            "ElasticsearchDomains.json",
            "SecurityGroups.json",
            "Subnets.json",
            "Vpcs.json",
            "NetworkAcls.json",
            "NetworkInterfaces.json",
            "Reservations.json");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsText("org/batfish/representation/aws/test", awsFilenames)
                .build(),
            _folder);
    return batfish.loadConfigurations();
  }

  @Test
  public void testEsSubnetEdge() throws IOException {
    Map<String, Configuration> configurations = loadAwsConfigurations();
    Topology topology = CommonUtil.synthesizeTopology(configurations);

    // check that ES instance is a neighbor of both  subnets in which its interfaces are
    assertThat(
        topology.getEdges(),
        hasItem(
            new Edge(
                new NodeInterfacePair("subnet-073b8061", "subnet-073b8061"),
                new NodeInterfacePair("es-domain", "es-domain-subnet-073b8061"))));
    assertThat(
        topology.getEdges(),
        hasItem(
            new Edge(
                new NodeInterfacePair("subnet-1f315846", "subnet-1f315846"),
                new NodeInterfacePair("es-domain", "es-domain-subnet-1f315846"))));
  }

  @Test
  public void testUniqueIps() throws IOException {
    Map<String, Configuration> configurations = loadAwsConfigurations();

    // check that  IPs are unique for all the interfaces
    List<Ip> ipsAsList =
        configurations
            .values()
            .stream()
            .map(Configuration::getInterfaces)
            .map(Map::values)
            .flatMap(Collection::stream)
            .map(Interface::getAllAddresses)
            .flatMap(Collection::stream)
            .map(InterfaceAddress::getIp)
            .collect(ImmutableList.toImmutableList());
    Set<Ip> ipsAsSet = ImmutableSet.copyOf(ipsAsList);
    assertThat(ipsAsList, hasSize(ipsAsSet.size()));
  }

  @Test
  public void testDefaultRoute() throws IOException {
    Map<String, Configuration> configurations = loadAwsConfigurations();
    StaticRoute defaultRoute1 = _staticRouteBuilder.setNextHopIp(new Ip("172.31.0.1")).build();
    StaticRoute defaultRoute2 = _staticRouteBuilder.setNextHopIp(new Ip("192.168.2.17")).build();

    // checking that both default routes exist(to both the subnets) in RDS instance
    assertThat(configurations, hasKey("es-domain"));
    assertThat(
        configurations.get("es-domain").getDefaultVrf().getStaticRoutes(),
        containsInAnyOrder(defaultRoute1, defaultRoute2));
  }

  @Test
  public void testElasticsearchDomain() throws JSONException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/ElasticsearchDomainTest.json");

    JSONObject jObj = new JSONObject(text);
    JSONArray esArray = jObj.getJSONArray(JSON_KEY_DOMAIN_STATUS_LIST);
    List<ElasticsearchDomain> esList = new LinkedList<>();
    for (int i = 0; i < esArray.length(); i++) {
      esList.add(new ElasticsearchDomain(esArray.getJSONObject(i), null));
    }

    // checking the count of ES instance initialized
    assertThat(esList, hasSize(1));

    ElasticsearchDomain elasticsearchDomain = esList.get(0);

    // checking the attributes of this ES instance
    assertThat(elasticsearchDomain, hasId("es-domain"));
    assertThat(elasticsearchDomain, hasAvailable(true));
    assertThat(elasticsearchDomain, hasVpcId("vpc-b390fad5"));
    assertThat(elasticsearchDomain, hasSubnets(ImmutableList.of("subnet-7044ff16")));
    assertThat(elasticsearchDomain, hasSecurityGroups(ImmutableList.of("sg-55510831")));
  }
}
