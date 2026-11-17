package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getDefaultRoutingInstance;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosBgpErrorToleranceTest {

  private static final String HOSTNAME = "bgp-error-tolerance-options";

  @TempDir public File _folder;

  @Test
  void testErrorTolerance() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    RoutingInstance routingInstance = getDefaultRoutingInstance(batfish, HOSTNAME);
    BgpGroup limited = routingInstance.getNamedBgpGroups().get("LIMITED");
    BgpGroup unlimited = routingInstance.getNamedBgpGroups().get("UNLIMITED");

    assertThat(routingInstance.getMasterBgpGroup().getErrorTolerance(), equalTo(true));
    assertThat(limited.getErrorTolerance(), equalTo(true));
    assertThat(limited.getMalformedRouteLimit(), equalTo(20L));
    assertThat(limited.getMalformedUpdateLogInterval(), equalTo(300));
    assertThat(unlimited.getErrorTolerance(), equalTo(true));
    assertThat(unlimited.getNoMalformedRouteLimit(), equalTo(true));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
  }
}
