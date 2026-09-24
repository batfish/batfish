package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.STATIC_ROUTE_NEXT_HOP_INTERFACE;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV6_UNICAST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.NextHop;
import org.batfish.representation.juniper.StaticRouteV6;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosQualifiedNextHopInterfaceTest {

  private static final String HOSTNAME = "qualified-next-hop-interface";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionReferencesAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration vendorConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    Prefix6 prefix6 = Prefix6.parse("2001:db8:2::/64");
    StaticRouteV6 route6 =
        vendorConfiguration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV6_UNICAST)
            .getStaticRoutesV6()
            .get(prefix6);
    NextHop nextHop6 = new NextHop(Ip6.parse("fe80::1"), "ge-0/0/1.0");

    assertThat(route6.getQualifiedNextHops(), hasKey(nextHop6));
    assertThat(route6.getQualifiedNextHops().get(nextHop6).getPreference(), equalTo(170));

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    StaticRoute route =
        configuration.getDefaultVrf().getStaticRoutes().stream()
            .filter(r -> r.getNetwork().equals(Prefix.parse("203.0.113.0/24")))
            .findFirst()
            .orElseThrow();
    assertThat(
        route.getNextHop(), equalTo(NextHopInterface.of("ge-0/0/0.0", Ip.parse("192.0.2.2"))));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, INTERFACE, "ge-0/0/0.0", STATIC_ROUTE_NEXT_HOP_INTERFACE));
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, INTERFACE, "ge-0/0/1.0", STATIC_ROUTE_NEXT_HOP_INTERFACE));
  }
}
