package org.batfish.representation.aws;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestSubnet;
import static org.batfish.representation.aws.AwsLocationInfoUtils.INSTANCE_INTERFACE_LINK_LOCATION_INFO;
import static org.batfish.representation.aws.AwsLocationInfoUtils.instanceInterfaceLocationInfo;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;
import static org.batfish.representation.aws.VpcEndpointInterface.getNodeId;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Test;

public class VpcEndpointInterfaceTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id", "service", "vpc", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of()),
            new VpcEndpointInterface(
                "id", "service", "vpc", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "other",
                "service",
                "vpc",
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id", "other", "vpc", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id",
                "service",
                "other",
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id",
                "service",
                "vpc",
                ImmutableList.of("other"),
                ImmutableList.of(),
                ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id",
                "service",
                "vpc",
                ImmutableList.of(),
                ImmutableList.of("other"),
                ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id",
                "service",
                "vpc",
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableMap.of("tag", "tag")))
        .testEquals();
  }

  @Test
  public void testToConfigurationNodes() {
    VpcEndpointInterface vpcEndpointInterface =
        new VpcEndpointInterface(
            "endpoint",
            "service",
            "vpc",
            ImmutableList.of("net1", "net2"),
            ImmutableList.of("sub1", "sub2"),
            ImmutableMap.of(TAG_NAME, "human"));
    List<Configuration> configurations =
        vpcEndpointInterface.toConfigurationNodes(
            new ConvertedConfiguration(), Region.builder("r1").build(), new Warnings());
    assertThat(
        configurations.stream()
            .map(Configuration::getHostname)
            .collect(ImmutableList.toImmutableList()),
        equalTo(
            ImmutableList.of(
                getNodeId("sub1", vpcEndpointInterface.getId()),
                getNodeId("sub2", vpcEndpointInterface.getId()))));
  }

  @Test
  public void testToConfigurationNode() {
    Subnet subnet = getTestSubnet(Prefix.parse("1.1.1.0/24"), "subnet", "vpc");
    NetworkInterface networkInterface =
        new NetworkInterface(
            "networkInterface",
            subnet.getId(),
            "vpc",
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("1.1.1.1"), null)),
            "desc",
            null,
            ImmutableMap.of());
    VpcEndpointInterface vpcEndpointInterface =
        new VpcEndpointInterface(
            "endpoint",
            "service",
            "vpc",
            ImmutableList.of(networkInterface.getId()),
            ImmutableList.of(subnet.getId()),
            ImmutableMap.of(TAG_NAME, "human"));

    Region region =
        Region.builder("r1")
            .setNetworkInterfaces(ImmutableMap.of(networkInterface.getId(), networkInterface))
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .build();

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();
    Warnings warnings = new Warnings(true, true, true);
    Configuration cfg =
        vpcEndpointInterface.toConfigurationNode(
            subnet.getId(), awsConfiguration, region, warnings);

    assertTrue(warnings.isEmpty());

    Interface viIface = getOnlyElement(cfg.getAllInterfaces().values());

    assertThat(
        cfg,
        allOf(
            hasConfigurationFormat(ConfigurationFormat.AWS),
            hasHostname(getNodeId(subnet.getId(), vpcEndpointInterface.getId())),
            hasDeviceModel(DeviceModel.AWS_VPC_ENDPOINT_INTERFACE)));
    assertThat(cfg.getHumanName(), equalTo("human"));
    assertThat(cfg.getVendorFamily().getAws().getRegion(), equalTo(region.getName()));
    assertThat(cfg.getVendorFamily().getAws().getVpcId(), equalTo("vpc"));
    assertThat(cfg.getVendorFamily().getAws().getSubnetId(), equalTo(subnet.getId()));

    assertThat(viIface.getDescription(), equalTo("desc"));
    assertThat(viIface.getPrimaryNetwork(), equalTo(Prefix.create(Ip.parse("1.1.1.1"), 24)));
    assertThat(
        awsConfiguration.getLayer1Edges(),
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(
                    cfg.getHostname(),
                    viIface.getName(),
                    Subnet.nodeName(subnet.getId()),
                    Subnet.instancesInterfaceName(subnet.getId())),
                new Layer1Edge(
                    Subnet.nodeName(subnet.getId()),
                    Subnet.instancesInterfaceName(subnet.getId()),
                    cfg.getHostname(),
                    viIface.getName()))));

    Map<Location, LocationInfo> locationInfo = cfg.getLocationInfo();
    assertEquals(
        instanceInterfaceLocationInfo(viIface), locationInfo.get(interfaceLocation(viIface)));
    assertEquals(
        INSTANCE_INTERFACE_LINK_LOCATION_INFO, locationInfo.get(interfaceLinkLocation(viIface)));
  }
}
