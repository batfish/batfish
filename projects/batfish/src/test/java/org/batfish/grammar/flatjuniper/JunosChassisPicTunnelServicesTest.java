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

final class JunosChassisPicTunnelServicesTest {

  private static final String HOSTNAME = "chassis-pic-tunnel-services";

  @TempDir public File _folder;

  @Test
  void testExtractionAndWarning() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        jc.getMasterLogicalSystem().getChassisPicTunnelServiceBandwidths(), hasEntry("0/1", 10E9));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("tunnel-services bandwidth 10g"), isTodo("tunnel-services")));
  }
}
