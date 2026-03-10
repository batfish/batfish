package org.batfish.grammar.flatjuniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for Juniper SNMP filter-interfaces parsing. */
public final class JuniperSnmpFilterInterfacesTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        java.util.Arrays.stream(configurationNames)
            .map(s -> "org/batfish/grammar/juniper/testconfigs/" + s)
            .toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  /** Tests that all variations of Juniper SNMP filter-interfaces syntax parse correctly. */
  @Test
  public void testSnmpFilterInterfaces() throws IOException {
    String hostname = "snmp-filter-interfaces-multiple";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ParseVendorConfigurationAnswerElement pvcae =
        batfish.loadParseVendorConfigurationAnswerElement(batfish.getSnapshot());

    assertThat(pvcae.getParseStatus().get(filename), equalTo(ParseStatus.PASSED));
  }
}
