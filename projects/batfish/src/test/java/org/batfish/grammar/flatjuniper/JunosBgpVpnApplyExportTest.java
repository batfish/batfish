package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getNamedBgpGroup;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.NamedBgpGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosBgpVpnApplyExportTest {

  private static final String HOSTNAME = "bgp-vpn-apply-export";

  @TempDir public File _folder;

  @Test
  void testVpnApplyExport() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    NamedBgpGroup group = getNamedBgpGroup(batfish, HOSTNAME, "VPN-PEERS");

    assertThat(group.getVpnApplyExport(), equalTo(true));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("vpn-apply-export")));
  }
}
