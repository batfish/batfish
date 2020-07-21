package org.batfish.representation.aws;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceType;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestSubnet;
import static org.batfish.representation.aws.AwsLocationInfoUtils.INSTANCE_INTERFACE_LINK_LOCATION_INFO;
import static org.batfish.representation.aws.AwsLocationInfoUtils.instanceInterfaceLocationInfo;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_INSTANCES;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RESERVATIONS;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.representation.aws.Instance.Status;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Test;

/** Test for {@link Instance} */
public class InstanceTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/InstanceTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode reservationArray = (ArrayNode) json.get(JSON_KEY_RESERVATIONS);
    List<Instance> instances = new LinkedList<>();

    for (int index = 0; index < reservationArray.size(); index++) {
      ArrayNode instanceArray = (ArrayNode) reservationArray.get(index).get(JSON_KEY_INSTANCES);
      for (int instIndex = 0; instIndex < instanceArray.size(); instIndex++) {
        instances.add(
            BatfishObjectMapper.mapper()
                .convertValue(instanceArray.get(instIndex), Instance.class));
      }
    }

    assertThat(
        instances,
        equalTo(
            ImmutableList.of(
                new Instance(
                    "i-08e529f98f5659289",
                    "vpc-815775e7",
                    "subnet-9a0c48fc",
                    ImmutableList.of("sg-adcd87d0"),
                    ImmutableList.of("eni-a9d44c8a"),
                    Ip.parse("10.100.1.20"),
                    ImmutableMap.of("Name", "fin-app-02"),
                    Status.PENDING),
                new Instance(
                    "i-05f467abe21e9b883",
                    "vpc-815775e7",
                    "subnet-9a0c48fc",
                    ImmutableList.of("sg-adcd87d0"),
                    ImmutableList.of("eni-bd11cc9d"),
                    Ip.parse("10.100.1.71"),
                    ImmutableMap.of(
                        "purpose",
                        "solarwinds",
                        "tag",
                        "GNS3-BGP-Test",
                        "creator",
                        "ratul",
                        "Name",
                        "shutdown"),
                    Status.STOPPED))));
  }

  @Test
  public void testToConfigurationNode() {
    String vpcId = "vpc";

    Subnet subnet =
        new Subnet(
            Prefix.parse("10.10.10.10/24"),
            "ownerId",
            "subnetArn",
            "subnet",
            vpcId,
            "zone",
            ImmutableMap.of());

    NetworkInterface networkInterface =
        new NetworkInterface(
            "interface",
            subnet.getId(),
            vpcId,
            ImmutableList.of(),
            ImmutableList.of(
                new PrivateIpAddress(true, Ip.parse("10.10.10.10"), Ip.parse("3.3.3.3"))),
            "desc",
            null,
            ImmutableMap.of());

    Instance instance =
        Instance.builder()
            .setInstanceId("instance")
            .setNetworkInterfaces(ImmutableList.of(networkInterface.getId()))
            .setTags(ImmutableMap.of("MADEUPTAG", "noval", TAG_NAME, "UserVisibleName!"))
            .build();

    Region region =
        Region.builder("test")
            .setInstances(ImmutableMap.of(instance.getId(), instance))
            .setNetworkInterfaces(ImmutableMap.of(networkInterface.getId(), networkInterface))
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .build();

    Warnings warnings = new Warnings();
    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();
    Configuration configuration = instance.toConfigurationNode(awsConfiguration, region, warnings);

    Interface configInterface = getOnlyElement(configuration.getAllInterfaces().values());

    assertTrue(warnings.isEmpty());
    assertThat(
        configuration,
        allOf(
            hasConfigurationFormat(ConfigurationFormat.AWS),
            hasHostname(instance.getId()),
            hasDeviceModel(DeviceModel.AWS_EC2_INSTANCE),
            hasDeviceType(DeviceType.HOST)));
    assertThat(configuration.getHumanName(), equalTo("UserVisibleName!"));
    assertThat(configInterface.getDescription(), equalTo("desc"));
    assertThat(
        configInterface.getPrimaryNetwork(), equalTo(Prefix.create(Ip.parse("10.10.10.10"), 24)));
    assertThat(
        awsConfiguration.getLayer1Edges(),
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(
                    instance.getId(),
                    configInterface.getName(),
                    Subnet.nodeName(subnet.getId()),
                    Subnet.instancesInterfaceName(subnet.getId())),
                new Layer1Edge(
                    Subnet.nodeName(subnet.getId()),
                    Subnet.instancesInterfaceName(subnet.getId()),
                    instance.getId(),
                    configInterface.getName()))));

    Map<Location, LocationInfo> locationInfo = configuration.getLocationInfo();
    assertEquals(
        instanceInterfaceLocationInfo(configInterface),
        locationInfo.get(interfaceLocation(configInterface)));
    assertEquals(
        INSTANCE_INTERFACE_LINK_LOCATION_INFO,
        locationInfo.get(interfaceLinkLocation(configInterface)));

    // Check that the public Ip refbook is added -- we test contents in testADdPublicIpRefBook
    assertTrue(
        configuration
            .getGeneratedReferenceBooks()
            .containsKey(
                GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.PublicIps)));
  }

  @Test
  public void testToConfigurationNode_multipleInterfaces() {
    String vpcId = "vpc";

    Subnet subnet = getTestSubnet(Prefix.parse("10.10.10.10/24"), "subnet", vpcId);
    Subnet subnet2 = getTestSubnet(Prefix.parse("10.10.20.10/24"), "subnet2", vpcId);

    NetworkInterface networkInterface =
        new NetworkInterface(
            "interface",
            subnet.getId(),
            vpcId,
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.10.10.10"), null)),
            "desc",
            null,
            ImmutableMap.of());

    NetworkInterface networkInterface20 =
        new NetworkInterface(
            "interface2_0",
            subnet2.getId(),
            vpcId,
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.10.20.10"), null)),
            "desc",
            null,
            ImmutableMap.of());

    NetworkInterface networkInterface21 =
        new NetworkInterface(
            "interface2_1",
            subnet2.getId(),
            vpcId,
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.10.20.11"), null)),
            "desc",
            null,
            ImmutableMap.of());

    Instance instance =
        Instance.builder()
            .setInstanceId("instance")
            .setNetworkInterfaces(
                ImmutableList.of(
                    networkInterface.getId(),
                    networkInterface20.getId(),
                    networkInterface21.getId()))
            .build();

    Region region =
        Region.builder("test")
            .setInstances(ImmutableMap.of(instance.getId(), instance))
            .setNetworkInterfaces(
                ImmutableMap.of(
                    networkInterface.getId(),
                    networkInterface,
                    networkInterface20.getId(),
                    networkInterface20,
                    networkInterface21.getId(),
                    networkInterface21))
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet, subnet2.getId(), subnet2))
            .build();

    Warnings warnings = new Warnings();
    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();
    Configuration configuration = instance.toConfigurationNode(awsConfiguration, region, warnings);

    assertTrue(warnings.isEmpty());

    assertThat(
        configuration.getAllInterfaces().values().stream()
            .map(Interface::getName)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(
            ImmutableSet.of(
                networkInterface.getId(), networkInterface20.getId(), networkInterface21.getId())));

    assertThat(
        awsConfiguration.getLayer1Edges(),
        hasItems(
            new Layer1Edge(
                instance.getId(),
                networkInterface.getId(),
                Subnet.nodeName(subnet.getId()),
                Subnet.instancesInterfaceName(subnet.getId())),
            new Layer1Edge(
                instance.getId(),
                networkInterface20.getId(),
                Subnet.nodeName(subnet2.getId()),
                Subnet.instancesInterfaceName(subnet2.getId())),
            new Layer1Edge(
                instance.getId(),
                networkInterface21.getId(),
                Subnet.nodeName(subnet2.getId()),
                Subnet.instancesInterfaceName(subnet2.getId()))));
  }
}
