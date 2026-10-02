package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosChassisMaximumEcmpTest {

  private static final String HOSTNAME = "chassis-maximum-ecmp";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testParseWarning() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);

    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("maximum-ecmp 64")));
  }
}
