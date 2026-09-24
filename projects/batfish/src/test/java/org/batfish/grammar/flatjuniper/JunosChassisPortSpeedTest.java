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

public final class JunosChassisPortSpeedTest {

  private static final String HOSTNAME = "chassis-port-speed";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(jc.getMasterLogicalSystem().getChassisPortSpeeds(), hasEntry("0/0/8", 1E9));
    assertThat(jc.getMasterLogicalSystem().getChassisPortSpeeds(), hasEntry("0/0/40", 40E9));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(c, hasInterface("ge-0/0/8", hasBandwidth(1E9)));
    assertThat(c, hasInterface("ge-0/0/8.0", hasBandwidth(1E9)));
    assertThat(c, hasInterface("et-0/0/40", hasBandwidth(40E9)));
    assertThat(c, hasInterface("et-0/0/40.7", hasBandwidth(40E9)));
  }
}
