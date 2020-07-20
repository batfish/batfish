package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestVpc;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.VpcEndpointGateway.SERVICE_PREFIX_FILTER;
import static org.batfish.representation.aws.VpcEndpointGateway.computeServicePrefixFilter;
import static org.batfish.representation.aws.VpcEndpointGateway.humanName;
import static org.batfish.representation.aws.VpcEndpointGateway.serviceInterfaceName;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import java.util.Comparator;
import java.util.stream.Stream;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.vendor_family.AwsFamily;
import org.batfish.specifier.LocationInfo;
import org.junit.Test;

public class VpcEndpointGatewayTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VpcEndpointGateway("id", "service", "vpc", ImmutableMap.of()),
            new VpcEndpointGateway("id", "service", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("other", "service", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("id", "other", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("id", "service", "other", ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointGateway("id", "service", "vpc", ImmutableMap.of("tag", "tag")))
        .testEquals();
  }

  @Test
  public void testToConfiguration() {
    Prefix vpcPrefix = Prefix.parse("10.10.10.0/24");
    Vpc vpc = getTestVpc("vpc", ImmutableSet.of(vpcPrefix));

    VpcEndpointGateway vpceGateway =
        new VpcEndpointGateway(
            "vpce-gw", "service", vpc.getId(), ImmutableMap.of(TAG_NAME, "humanName"));

    Prefix servicePrefix = Prefix.parse("1.1.1.1/32");
    IpSpace servicePrefixSpace =
        IpWildcardSetIpSpace.builder()
            .including(ImmutableList.of(IpWildcard.create(servicePrefix)))
            .build();
    PrefixList prefixList =
        new PrefixList("pl", ImmutableList.of(servicePrefix), vpceGateway.getServiceName());

    Region region =
        Region.builder("region")
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setVpcEndpoints(ImmutableMap.of(vpceGateway.getId(), vpceGateway))
            .setPrefixLists(ImmutableMap.of(prefixList.getId(), prefixList))
            .build();

    Configuration vpcConfig =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableList.of(vpcConfig));

    Configuration vpceGwConfig =
        vpceGateway.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(vpceGwConfig, hasDeviceModel(DeviceModel.AWS_VPC_ENDPOINT_GATEWAY));
    assertThat(vpceGwConfig.getHumanName(), equalTo("humanName"));
    AwsFamily family = vpceGwConfig.getVendorFamily().getAws();
    assertThat(family, notNullValue());
    assertThat(family.getRegion(), equalTo(region.getName()));
    assertThat(family.getVpcId(), equalTo(vpc.getId()));

    // gateway should have interfaces to the service and the vpc
    assertThat(
        vpceGwConfig.getAllInterfaces().values().stream()
            .map(Interface::getName)
            .collect(ImmutableList.toImmutableList()),
        equalTo(
            ImmutableList.of(
                serviceInterfaceName(vpceGateway.getServiceName()),
                Utils.interfaceNameToRemote(vpcConfig))));
    Interface serviceInterface =
        vpceGwConfig.getAllInterfaces().get(serviceInterfaceName(vpceGateway.getServiceName()));
    Interface vpcInterface =
        vpceGwConfig
            .getAllInterfaces()
            .get(serviceInterfaceName(Utils.interfaceNameToRemote(vpcConfig)));

    // static routes exist to the VPC prefix and to the service
    assertThat(
        vpceGwConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            Stream.concat(
                    Stream.of(
                        toStaticRoute(
                            vpcPrefix, Utils.interfaceNameToRemote(vpcConfig), LINK_LOCAL_IP)),
                    Stream.of(toStaticRoute(servicePrefix, serviceInterface.getName())))
                .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()))));

    // check the filter on vpc interface
    assertTrue(vpceGwConfig.getIpAccessLists().containsKey(SERVICE_PREFIX_FILTER));
    assertThat(
        vpcInterface.getIncomingFilter(), equalTo(computeServicePrefixFilter(servicePrefixSpace)));

    // test for location info
    assertThat(
        vpceGwConfig.getLocationInfo().get(interfaceLinkLocation(serviceInterface)),
        equalTo(new LocationInfo(true, servicePrefixSpace, LINK_LOCAL_IP.toIpSpace())));
  }

  @Test
  public void testComputeServicePrefixFilter() {
    Ip serviceIp = Ip.parse("1.1.1.1");
    Ip nonServiceIp = Ip.parse("6.6.6.6");
    IpAccessList servicePrefixFilter = computeServicePrefixFilter(serviceIp.toIpSpace());
    assertThat(
        servicePrefixFilter
            .filter(
                Flow.builder().setDstIp(serviceIp).setIngressNode("aa").build(),
                null,
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.PERMIT));
    assertThat(
        servicePrefixFilter
            .filter(
                Flow.builder().setDstIp(nonServiceIp).setIngressNode("aa").build(),
                null,
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testHumanName() {
    assertThat(humanName(ImmutableMap.of(TAG_NAME, "tag"), "sname"), equalTo("tag"));
    assertThat(humanName(ImmutableMap.of(), "sname"), equalTo("sname"));
  }
}
