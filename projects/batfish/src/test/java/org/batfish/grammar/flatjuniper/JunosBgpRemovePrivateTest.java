package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.routing_policy.statement.Statements.RemovePrivateAs;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getDefaultRoutingInstance;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
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
    BgpGroup allReplace = routingInstance.getNamedBgpGroups().get("ALL-REPLACE");
    BgpGroup nearest = routingInstance.getNamedBgpGroups().get("NEAREST");
    BgpGroup supported = routingInstance.getNamedBgpGroups().get("SUPPORTED");

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
            isTodo("remove-private all"),
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
  }
}
