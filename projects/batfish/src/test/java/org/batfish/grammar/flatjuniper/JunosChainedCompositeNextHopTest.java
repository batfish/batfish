package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.File;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosChainedCompositeNextHopTest {

  private static final String HOSTNAME = "forwarding-table-chained-composite-next-hop";

  @TempDir public File _folder;

  @Test
  void testChainedCompositeNextHop() {
    JuniperConfiguration config = parseJuniperConfig(_folder, HOSTNAME);

    assertThat(config.getWarnings().getParseWarnings(), empty());
  }
}
