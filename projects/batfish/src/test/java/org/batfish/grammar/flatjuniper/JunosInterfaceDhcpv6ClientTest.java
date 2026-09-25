package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceDhcpv6ClientTest {

  private static final String HOSTNAME = "junos-interface-dhcpv6-client";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDhcpv6Client() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
  }
}
