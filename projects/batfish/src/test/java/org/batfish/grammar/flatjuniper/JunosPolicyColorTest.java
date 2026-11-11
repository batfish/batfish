package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
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
import org.batfish.representation.juniper.PsFromColor;
import org.batfish.representation.juniper.PsFromColor2;
import org.batfish.representation.juniper.PsFroms;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyColorTest {

  private static final String HOSTNAME = "policy-statement-color";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static Bgpv4Route route(long color, long color2) {
    return Bgpv4Route.testBuilder()
        .setNetwork(Prefix.parse("10.0.0.0/8"))
        .setOriginatorIp(Ip.ZERO)
        .setOriginType(OriginType.IGP)
        .setProtocol(RoutingProtocol.BGP)
        .setColor(color)
        .setColor2(color2)
        .build();
  }

  private Configuration config() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    return batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
  }

  private static boolean accepts(RoutingPolicy policy, Bgpv4Route route) {
    return policy.process(route, route.toBuilder(), Direction.IN);
  }

  @Test
  public void testExtraction() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    PsFroms color =
        jc.getMasterLogicalSystem()
            .getPolicyStatements()
            .get("MATCH-COLOR")
            .getTerms()
            .get("T")
            .getFroms();
    PsFroms color2 =
        jc.getMasterLogicalSystem()
            .getPolicyStatements()
            .get("MATCH-COLOR2")
            .getTerms()
            .get("T")
            .getFroms();
    assertThat(color.getFromColor(), equalTo(new PsFromColor(10)));
    assertThat(color2.getFromColor2(), equalTo(new PsFromColor2(20)));
    // Only `then color add` remains unsupported.
    assertThat(
        getParseWarnings(batfish, HOSTNAME).stream().map(w -> w.getText()).toList(),
        contains(containsString("color add 5")));
  }

  @Test
  public void testThenColorSetsBoth() throws IOException {
    RoutingPolicy set = config().getRoutingPolicies().get("SET-COLOR");
    Bgpv4Route.Builder out = route(0, 0).toBuilder();
    assertThat(set.process(route(0, 0), out, Direction.IN), equalTo(true));
    assertThat(out.build().getColor(), equalTo(10L));
    assertThat(out.build().getColor2(), equalTo(20L));
  }

  @Test
  public void testFromColor() throws IOException {
    RoutingPolicy match = config().getRoutingPolicies().get("MATCH-COLOR");
    assertThat(accepts(match, route(10, 0)), equalTo(true));
    assertThat(accepts(match, route(9, 0)), equalTo(false));
    // color2 does not satisfy `from color`.
    assertThat(accepts(match, route(0, 10)), equalTo(false));
  }

  @Test
  public void testFromColor2() throws IOException {
    RoutingPolicy match = config().getRoutingPolicies().get("MATCH-COLOR2");
    assertThat(accepts(match, route(0, 20)), equalTo(true));
    assertThat(accepts(match, route(20, 0)), equalTo(false));
  }
}
