package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getDefaultRoutingInstance;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpPathSelectionTest {

  private static final String HOSTNAME = "bgp-path-selection";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testPathSelection() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    RoutingInstance routingInstance = getDefaultRoutingInstance(batfish, HOSTNAME);

    assertThat(routingInstance.getBgpAlwaysCompareMed(), equalTo(true));
    assertThat(routingInstance.getBgpExternalRouterId(), equalTo(true));
    assertThat(routingInstance.getBgpMedPlusIgp(), equalTo(true));
    assertThat(routingInstance.getBgpMedPlusIgpIgpMultiplier(), equalTo(10));
    assertThat(routingInstance.getBgpMedPlusIgpMedMultiplier(), equalTo(20));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("med-plus-igp"),
            isTodo("med-plus-igp igp-multiplier 10"),
            isTodo("med-plus-igp med-multiplier 20"),
            hasComment("Expected BGP path-selection multiplier in range 1-1000, but got '0'")));

    Configuration config = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        config.getDefaultVrf().getBgpProcess().getTieBreaker(), equalTo(BgpTieBreaker.ROUTER_ID));
  }
}
