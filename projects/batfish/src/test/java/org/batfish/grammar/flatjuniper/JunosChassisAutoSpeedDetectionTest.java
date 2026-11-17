package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;

import java.io.File;
import java.io.IOException;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosChassisAutoSpeedDetectionTest {

  private static final String HOSTNAME = "chassis-auto-speed-detection";

  @TempDir public File _folder;

  @Test
  void testExtractionAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(jc.getMasterLogicalSystem().getChassisFpcAutoSpeedDetection(), hasEntry(0, false));
    assertThat(jc.getMasterLogicalSystem().getChassisFpcAutoSpeedDetection(), hasEntry(1, true));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("auto-speed-detection disable"), isTodo("auto-speed-detection enable")));
  }
}
