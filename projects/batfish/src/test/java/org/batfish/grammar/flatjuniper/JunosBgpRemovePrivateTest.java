package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.routing_policy.statement.Statements.RemoveLeadingPrivateAs;
import static org.batfish.datamodel.routing_policy.statement.Statements.RemovePrivateAs;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getDefaultRoutingInstance;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpRemovePrivateTest {

  private static final String HOSTNAME = "bgp-remove-private-options";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testRemovePrivate() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    RoutingInstance routingInstance = getDefaultRoutingInstance(batfish, HOSTNAME);
    BgpGroup basic = routingInstance.getNamedBgpGroups().get("BASIC");
    BgpGroup allReplace = routingInstance.getNamedBgpGroups().get("ALL-REPLACE");
    BgpGroup nearest = routingInstance.getNamedBgpGroups().get("NEAREST");
    BgpGroup supported = routingInstance.getNamedBgpGroups().get("SUPPORTED");

    assertThat(basic.getRemovePrivate(), equalTo(true));
    assertThat(allReplace.getRemovePrivate(), equalTo(true));
    assertThat(allReplace.getRemovePrivateAll(), equalTo(true));
    assertThat(allReplace.getRemovePrivateReplace(), equalTo(true));
    assertThat(allReplace.getRemovePrivateNearest(), equalTo(false));
    assertThat(allReplace.getRemovePrivateNoPeerLoopCheck(), equalTo(false));
    assertThat(nearest.getRemovePrivate(), equalTo(true));
    assertThat(nearest.getRemovePrivateAll(), equalTo(true));
    assertThat(nearest.getRemovePrivateReplace(), equalTo(true));
    assertThat(nearest.getRemovePrivateNearest(), equalTo(true));
    assertThat(nearest.getRemovePrivateNoPeerLoopCheck(), equalTo(true));
    assertThat(supported.isRemovePrivateAllNoPeerLoopCheck(), equalTo(true));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("remove-private replace"),
            isTodo("remove-private all replace nearest no-peer-loop-check")));

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        configuration
            .getRoutingPolicies()
            .get(generatedBgpPeerExportPolicyName("default", "192.0.2.2/32"))
            .getStatements(),
        hasItem(RemovePrivateAs.toStaticStatement()));
    assertThat(
        configuration
            .getRoutingPolicies()
            .get(generatedBgpPeerExportPolicyName("default", "198.51.100.2/32"))
            .getStatements(),
        not(hasItem(RemovePrivateAs.toStaticStatement())));
    assertThat(basic.isRemovePrivateBare(), equalTo(true));
    assertThat(
        configuration
            .getRoutingPolicies()
            .get(generatedBgpPeerExportPolicyName("default", "192.0.2.4/32"))
            .getStatements(),
        hasItem(RemoveLeadingPrivateAs.toStaticStatement()));
    assertThat(
        configuration
            .getRoutingPolicies()
            .get(generatedBgpPeerExportPolicyName("default", "198.51.100.2/32"))
            .getStatements(),
        not(hasItem(RemoveLeadingPrivateAs.toStaticStatement())));
  }

  @Test
  public void testRemovePrivateBeforeExportPolicy() throws IOException {
    String hostname = "bgp-remove-private-prepend";
    Batfish batfish = getBatfish(_folder, hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    RoutingPolicy export =
        c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName("default", "192.0.2.2/32"));
    Bgpv4Route route =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setAsPath(AsPath.ofSingletonAsSets(65100L))
            .build();
    BgpSessionProperties session =
        BgpSessionProperties.builder()
            .setLocalAs(65000L)
            .setRemoteAs(65000L)
            .setLocalIp(Ip.parse("192.0.2.1"))
            .setRemoteIp(Ip.parse("192.0.2.2"))
            .build();
    Bgpv4Route.Builder out = route.toBuilder();

    assertTrue(export.processBgpRoute(route, out, session, Direction.OUT, null));
    // The private AS is removed before the export policy prepends it again.
    assertThat(out.getAsPath(), equalTo(AsPath.ofSingletonAsSets(65100L)));
  }
}
