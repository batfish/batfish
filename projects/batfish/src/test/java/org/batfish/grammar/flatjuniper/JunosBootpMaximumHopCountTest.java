package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.IOException;
import org.batfish.main.Batfish;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosBootpMaximumHopCountTest {

  private static final String HOSTNAME = "junos-bootp-maximum-hop-count";

  @TempDir public File _folder;

  @Test
  void testMaximumHopCount() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);

    assertThat(batfish.loadConfigurations(batfish.getSnapshot()), hasKey(HOSTNAME));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
  }
}
