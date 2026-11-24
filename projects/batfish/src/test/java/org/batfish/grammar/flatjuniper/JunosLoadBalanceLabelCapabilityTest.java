package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.File;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosLoadBalanceLabelCapabilityTest {

  @TempDir public File _folder;

  @Test
  void testLoadBalanceLabelCapability() {
    JuniperConfiguration enabled =
        parseJuniperConfig(_folder, "forwarding-options-load-balance-label-capability");
    JuniperConfiguration disabled =
        parseJuniperConfig(_folder, "forwarding-options-no-load-balance-label-capability");

    assertThat(enabled.getWarnings().getParseWarnings(), empty());
    assertThat(disabled.getWarnings().getParseWarnings(), empty());
  }
}
