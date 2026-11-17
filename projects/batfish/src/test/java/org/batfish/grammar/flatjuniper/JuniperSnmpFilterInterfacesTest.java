package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for Juniper SNMP filter-interfaces parsing. */
final class JuniperSnmpFilterInterfacesTest {

  @TempDir public File _folder;

  /** Tests that all variations of Juniper SNMP filter-interfaces syntax parse correctly. */
  @Test
  void testSnmpFilterInterfaces() throws IOException {
    String hostname = "snmp-filter-interfaces-multiple";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfish(_folder, hostname);
    ParseVendorConfigurationAnswerElement pvcae =
        batfish.loadParseVendorConfigurationAnswerElement(batfish.getSnapshot());

    assertThat(pvcae.getParseStatus().get(filename), equalTo(ParseStatus.PASSED));
  }
}
