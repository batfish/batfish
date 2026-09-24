package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV4_UNICAST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.NextHop;
import org.batfish.representation.juniper.QualifiedNextHop;
import org.batfish.representation.juniper.StaticRoute;
import org.batfish.representation.juniper.StaticRouteV4;
import org.batfish.representation.juniper.StaticRouteV6;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosStaticRouteBfdTest {

  private static final String HOSTNAME = "junos-static-route-bfd";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testStaticRouteBfd() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());

    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    StaticRouteV4 route =
        jc.getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV4_UNICAST)
            .getStaticRoutes()
            .get(Prefix.parse("192.0.2.0/24"));
    assertBfd(route, 255, 255000, 300, 400, 5, 500, 10);

    StaticRouteV4 inheritedRoute =
        jc.getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV4_UNICAST)
            .getStaticRoutes()
            .get(Prefix.parse("203.0.113.0/24"));
    assertBfd(inheritedRoute, 1, 0, 1, 1, 1, 1, 1);

    StaticRouteV4 qualifiedRoute =
        jc.getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV4_UNICAST)
            .getStaticRoutes()
            .get(Prefix.parse("198.51.100.0/24"));
    QualifiedNextHop qualifiedNextHop =
        qualifiedRoute.getQualifiedNextHops().get(new NextHop(Ip.parse("192.0.2.2")));
    assertBfd(qualifiedNextHop, 3, 1000, 600, 700, 4, 800, 9);

    StaticRouteV6 route6 =
        jc.getMasterLogicalSystem()
            .getRoutingInstances()
            .get("EDGE")
            .getRibs()
            .get("EDGE.inet6.0")
            .getStaticRoutesV6()
            .get(Prefix6.parse("2001:db8::/64"));
    assertThat(route6.getBfdLivenessDetectionMinimumInterval(), equalTo(900));
    assertThat(route6.getBfdLivenessDetectionNoAdaptation(), equalTo(true));

    assertThat(getParseWarnings(batfish, HOSTNAME), hasSize(32));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        hasItems(
            hasComment("Expected BFD detection-time threshold in range 1-255, but got '0'"),
            hasComment("Expected BFD holddown-interval in range 0-255000, but got '255001'"),
            hasComment("Expected BFD minimum-interval in range 1-255000, but got '0'"),
            isTodo("bfd-liveness-detection detection-time threshold 255"),
            isTodo("bfd-liveness-detection transmit-interval minimum-interval 500"),
            isTodo("bfd-liveness-detection transmit-interval threshold 10")));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        everyItem(
            anyOf(
                isTodo(),
                hasComment("Expected BFD detection-time threshold in range 1-255, but got '0'"),
                hasComment("Expected BFD holddown-interval in range 0-255000, but got '255001'"),
                hasComment("Expected BFD minimum-interval in range 1-255000, but got '0'"))));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault("configs/" + HOSTNAME, new Warnings()).getRedFlagWarnings(),
        empty());
  }

  private static void assertBfd(
      QualifiedNextHop nextHop,
      int detectionTimeThreshold,
      int holddownInterval,
      int minimumInterval,
      int minimumReceiveInterval,
      int multiplier,
      int transmitIntervalMinimumInterval,
      int transmitIntervalThreshold) {
    assertThat(
        nextHop.getBfdLivenessDetectionDetectionTimeThreshold(), equalTo(detectionTimeThreshold));
    assertThat(nextHop.getBfdLivenessDetectionHolddownInterval(), equalTo(holddownInterval));
    assertThat(nextHop.getBfdLivenessDetectionMinimumInterval(), equalTo(minimumInterval));
    assertThat(
        nextHop.getBfdLivenessDetectionMinimumReceiveInterval(), equalTo(minimumReceiveInterval));
    assertThat(nextHop.getBfdLivenessDetectionMultiplier(), equalTo(multiplier));
    assertThat(nextHop.getBfdLivenessDetectionNoAdaptation(), equalTo(true));
    assertThat(
        nextHop.getBfdLivenessDetectionTransmitIntervalMinimumInterval(),
        equalTo(transmitIntervalMinimumInterval));
    assertThat(
        nextHop.getBfdLivenessDetectionTransmitIntervalThreshold(),
        equalTo(transmitIntervalThreshold));
  }

  private static void assertBfd(
      StaticRoute<?> route,
      int detectionTimeThreshold,
      int holddownInterval,
      int minimumInterval,
      int minimumReceiveInterval,
      int multiplier,
      int transmitIntervalMinimumInterval,
      int transmitIntervalThreshold) {
    assertThat(
        route.getBfdLivenessDetectionDetectionTimeThreshold(), equalTo(detectionTimeThreshold));
    assertThat(route.getBfdLivenessDetectionHolddownInterval(), equalTo(holddownInterval));
    assertThat(route.getBfdLivenessDetectionMinimumInterval(), equalTo(minimumInterval));
    assertThat(
        route.getBfdLivenessDetectionMinimumReceiveInterval(), equalTo(minimumReceiveInterval));
    assertThat(route.getBfdLivenessDetectionMultiplier(), equalTo(multiplier));
    assertThat(route.getBfdLivenessDetectionNoAdaptation(), equalTo(true));
    assertThat(
        route.getBfdLivenessDetectionTransmitIntervalMinimumInterval(),
        equalTo(transmitIntervalMinimumInterval));
    assertThat(
        route.getBfdLivenessDetectionTransmitIntervalThreshold(),
        equalTo(transmitIntervalThreshold));
  }
}
