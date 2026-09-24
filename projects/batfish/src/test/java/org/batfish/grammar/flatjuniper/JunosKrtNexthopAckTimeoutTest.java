package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosKrtNexthopAckTimeoutTest {

  private static final String HOSTNAME = "forwarding-table-krt-nexthop-ack-timeout";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testKrtNexthopAckTimeout() {
    JuniperConfiguration config = parseJuniperConfig(_folder, HOSTNAME);

    assertThat(config.getWarnings().getParseWarnings(), empty());
  }
}
