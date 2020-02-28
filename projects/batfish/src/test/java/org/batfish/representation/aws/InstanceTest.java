package org.batfish.representation.aws;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceType;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.representation.aws.AwsLocationInfoUtils.INSTANCE_INTERFACE_LINK_LOCATION_INFO;
import static org.batfish.representation.aws.AwsLocationInfoUtils.instanceInterfaceLocationInfo;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_INSTANCES;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RESERVATIONS;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
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
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Instance.Status;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Test;

/** Test for {@link Instance} */
public class InstanceTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/InstanceTest.json");

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
        new Subnet(Prefix.parse("10.10.10.10/24"), "subnet", vpcId, "zone", ImmutableMap.of());

    NetworkInterface networkInterface =
        new NetworkInterface(
            "interface",
            subnet.getId(),
            vpcId,
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.10.10.10"), null)),
            "desc",
            null);

    Instance instance =
        Instance.builder()
            .setInstanceId("instance")
            .setNetworkInterfaces(ImmutableList.of(networkInterface.getNetworkInterfaceId()))
            .setTags(ImmutableMap.of("MADEUPTAG", "noval", TAG_NAME, "UserVisibleName!"))
            .build();

    Region region =
        Region.builder("test")
            .setInstances(ImmutableMap.of(instance.getId(), instance))
            .setNetworkInterfaces(
                ImmutableMap.of(networkInterface.getNetworkInterfaceId(), networkInterface))
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
  }
}
