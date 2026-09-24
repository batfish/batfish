package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for Juniper SNMP filter-interfaces parsing. */
public final class JuniperSnmpFilterInterfacesTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /** Tests that all variations of Juniper SNMP filter-interfaces syntax parse correctly. */
  @Test
  public void testSnmpFilterInterfaces() throws IOException {
    String hostname = "snmp-filter-interfaces-multiple";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfish(_folder, hostname);
    ParseVendorConfigurationAnswerElement pvcae =
        batfish.loadParseVendorConfigurationAnswerElement(batfish.getSnapshot());

    assertThat(pvcae.getParseStatus().get(filename), equalTo(ParseStatus.PASSED));
  }
}
