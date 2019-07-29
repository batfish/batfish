package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SUBNETS;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link Subnet} */
public class SubnetTest {

  private List<Subnet> _subnetList;

  @Before
  public void setup() throws IOException {
    JsonNode json =
        BatfishObjectMapper.mapper()
            .readTree(CommonUtil.readResource("org/batfish/representation/aws/SubnetTest.json"));
    _subnetList =
        BatfishObjectMapper.mapper()
            .convertValue(json.get(JSON_KEY_SUBNETS), new TypeReference<List<Subnet>>() {});
  }

  @Test
  public void testDeserialization() {
    assertThat(
        _subnetList,
        equalTo(ImmutableList.of(new Subnet(Prefix.parse("172.31.0.0/20"), "subnet-1", "vpc-1"))));
  }

  @Test
  public void testGetNextIp() {
    Subnet subnet = _subnetList.get(0);
    assertThat(subnet.getNextIp(), equalTo(Ip.parse("172.31.0.2")));
    assertThat(subnet.getNextIp(), equalTo(Ip.parse("172.31.0.3")));
    assertThat(subnet.getNextIp(), equalTo(Ip.parse("172.31.0.4")));
  }

  @Test
  public void testToConfigurationNode() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Ip privateIp = Ip.parse("10.10.10.10");
    Prefix privatePrefix = Prefix.create(privateIp, 24);
    Ip publicIp = Ip.parse("1.1.1.1");

    NetworkInterface ni =
        new NetworkInterface(
            "ni",
            "subnet",
            vpc.getId(),
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, privateIp, publicIp)),
            "desc",
            null);

    Subnet subnet = new Subnet(privatePrefix, "subnet", vpc.getId());

    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setNetworkInterfaces(ImmutableMap.of(ni.getId(), ni))
            .setNetworkAcls(
                ImmutableMap.of(
                    "acl",
                    new NetworkAcl(
                        "acl",
                        vpc.getId(),
                        ImmutableList.of(new NetworkAclAssociation(subnet.getId())),
                        ImmutableList.of())))
            .build();

    AwsConfiguration awsConfiguration =
        new AwsConfiguration(
            ImmutableMap.of(region.getName(), region),
            ImmutableMap.of(vpcConfig.getHostname(), vpcConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());

    // subnet should have interfaces to the instances and vpc
    assertThat(
        subnetCfg.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(subnet.getId(), vpc.getId())));

    // the vpc should have gotten an interface pointed to the subnet
    assertThat(
        subnetCfg.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(subnet.getId(), vpc.getId())));

    // vpc facing interface should have the right address
    assertThat(
        subnetCfg.getAllInterfaces().get(vpc.getId()).getConcreteAddress().getPrefix(),
        equalTo(vpcConfig.getAllInterfaces().get(subnet.getId()).getConcreteAddress().getPrefix()));

    // instance facing interface should have the right address
    assertThat(
        subnetCfg.getAllInterfaces().get(subnet.getId()).getConcreteAddress().getPrefix(),
        equalTo(privatePrefix));

    // the vpc router should have a static route to the subnet and to public IPs
    assertThat(
        vpcConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    privatePrefix,
                    subnetCfg.getAllInterfaces().get(vpc.getId()).getConcreteAddress().getIp()),
                toStaticRoute(
                    publicIp,
                    subnetCfg.getAllInterfaces().get(vpc.getId()).getConcreteAddress().getIp()))));

    // the subnet router should have a default route to the VPC and the public ip toward instances
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    Prefix.ZERO,
                    vpcConfig.getAllInterfaces().get(subnet.getId()).getConcreteAddress().getIp()),
                toStaticRoute(publicIp, subnetCfg.getAllInterfaces().get(subnet.getId())))));
  }
}
