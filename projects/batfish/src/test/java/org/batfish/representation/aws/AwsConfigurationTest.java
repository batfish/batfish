package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.representation.aws.AwsConfiguration.AWS_SERVICES_FACING_INTERFACE_NAME;
import static org.batfish.representation.aws.AwsConfiguration.AWS_SERVICES_GATEWAY_NODE_NAME;
import static org.batfish.representation.aws.AwsConfiguration.BACKBONE_EXPORT_POLICY_NAME;
import static org.batfish.representation.aws.AwsConfiguration.BACKBONE_FACING_INTERFACE_NAME;
import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.AwsConfiguration.generateAwsServicesGateway;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.specifier.LocationInfo;
import org.junit.Test;

public class AwsConfigurationTest {

  /** Test that the AWS services gateway node is always created */
  @Test
  public void testToVendorConfigurations_awsServicesGatewayNode() {
    AwsConfiguration awsConfiguration = new AwsConfiguration();
    List<Configuration> c = awsConfiguration.toVendorIndependentConfigurations();
    assertThat(c, contains(hasHostname(AWS_SERVICES_GATEWAY_NODE_NAME)));
  }

  @Test
  public void testGenerateAwsServicesGateway() {
    Configuration cfg = generateAwsServicesGateway();

    assertThat(cfg.getDeviceModel(), equalTo(DeviceModel.AWS_SERVICES_GATEWAY));

    // interfaces are present with firewall sessions
    assertThat(
        cfg.getAllInterfaces().keySet(),
        containsInAnyOrder(BACKBONE_FACING_INTERFACE_NAME, AWS_SERVICES_FACING_INTERFACE_NAME));
    assertThat(
        cfg.getAllInterfaces()
            .get(AWS_SERVICES_FACING_INTERFACE_NAME)
            .getFirewallSessionInterfaceInfo(),
        notNullValue());
    assertThat(
        cfg.getAllInterfaces()
            .get(BACKBONE_FACING_INTERFACE_NAME)
            .getFirewallSessionInterfaceInfo(),
        notNullValue());

    // static routes are defined
    Set<Prefix> awsServicesPrefixes =
        Sets.difference(
            ImmutableSet.copyOf(AwsPrefixes.getPrefixes(AwsPrefixes.SERVICE_AMAZON)),
            ImmutableSet.copyOf(AwsPrefixes.getPrefixes(AwsPrefixes.SERVICE_EC2)));
    assertThat(
        cfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            awsServicesPrefixes.stream()
                .map(prefix -> toStaticRoute(prefix, AWS_SERVICES_FACING_INTERFACE_NAME))
                .collect(Collectors.toSet())));

    // location info is defined
    assertThat(
        cfg.getLocationInfo()
            .get(
                interfaceLinkLocation(
                    cfg.getAllInterfaces().get(AWS_SERVICES_FACING_INTERFACE_NAME))),
        equalTo(
            new LocationInfo(
                true,
                IpWildcardSetIpSpace.builder()
                    .including(
                        awsServicesPrefixes.stream()
                            .map(IpWildcard::create)
                            .collect(Collectors.toList()))
                    .build(),
                // using LINK_LOCAL_IP gets us EXITS_NETWORK as disposition for service prefixes
                LINK_LOCAL_IP.toIpSpace())));

    // bgp policy has been created
    assertTrue(cfg.getRoutingPolicies().containsKey(BACKBONE_EXPORT_POLICY_NAME));
  }
}
