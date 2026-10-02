package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasBandwidth;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosChassisNumberOfSubPortsTest {

  private static final String HOSTNAME = "chassis-number-of-sub-ports";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndWarning() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(jc.getMasterLogicalSystem().getChassisPortSubPortCounts(), hasEntry("0/0/8", 4));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(c, hasInterface("et-0/0/8:0", hasBandwidth(25E9)));
    assertThat(c, hasInterface("et-0/0/8:0.0", hasBandwidth(25E9)));
    assertThat(c, hasInterface("et-0/0/8:3", hasBandwidth(25E9)));
    assertThat(c, hasInterface("et-0/0/8:3.0", hasBandwidth(25E9)));
  }
}
