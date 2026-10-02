package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosChassisPicModeTest {

  private static final String HOSTNAME = "chassis-pic-mode";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndWarning() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(jc.getMasterLogicalSystem().getChassisPicSpeeds(), hasEntry("0/1", 100E9));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("pic-mode 100G")));
  }
}
