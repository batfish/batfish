package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosLoadBalanceLabelCapabilityTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testLoadBalanceLabelCapability() {
    JuniperConfiguration enabled =
        parseJuniperConfig(_folder, "forwarding-options-load-balance-label-capability");
    JuniperConfiguration disabled =
        parseJuniperConfig(_folder, "forwarding-options-no-load-balance-label-capability");

    assertThat(enabled.getWarnings().getParseWarnings(), empty());
    assertThat(disabled.getWarnings().getParseWarnings(), empty());
  }
}
