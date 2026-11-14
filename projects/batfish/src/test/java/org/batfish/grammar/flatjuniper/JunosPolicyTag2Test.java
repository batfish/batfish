package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.PsFromTag2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyTag2Test {

  private static final String HOSTNAME = "policy-statement-tag2";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static Bgpv4Route route(long tag2) {
    return Bgpv4Route.testBuilder()
        .setNetwork(Prefix.parse("10.0.0.0/8"))
        .setOriginatorIp(Ip.ZERO)
        .setOriginType(OriginType.IGP)
        .setProtocol(RoutingProtocol.BGP)
        .setTag2(tag2)
        .build();
  }

  @Test
  public void testExtraction() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("MATCH-TAG2")
            .getTerms()
            .get("T")
            .getFroms()
            .getFromTag2s(),
        contains(new PsFromTag2(10)));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
  }

  @Test
  public void testConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);

    // `then tag2` sets it.
    RoutingPolicy set = c.getRoutingPolicies().get("SET-TAG2");
    Bgpv4Route.Builder out = route(0).toBuilder();
    assertThat(set.process(route(0), out, Direction.IN), equalTo(true));
    assertThat(out.build().getTag2(), equalTo(10L));

    // `from tag2` matches it.
    RoutingPolicy match = c.getRoutingPolicies().get("MATCH-TAG2");
    assertThat(match.process(route(10), route(10).toBuilder(), Direction.IN), equalTo(true));
    assertThat(match.process(route(9), route(9).toBuilder(), Direction.IN), equalTo(false));
  }
}
