package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasBandwidth;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosChassisChannelSpeedTest {

  private static final String HOSTNAME = "chassis-channel-speed";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(jc.getMasterLogicalSystem().getChassisPortSpeeds(), hasEntry("0/0/8", 10E9));
    assertThat(jc.getMasterLogicalSystem().getChassisPortSpeeds(), hasEntry("0/0/40", 25E9));
    assertThat(jc.getMasterLogicalSystem().getChassisPortSpeeds(), hasEntry("0/0/41", 25E9));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("channel-speed disable-auto-speed-detection"),
            isTodo("channel-speed disable-auto-speed-detection")));

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(c, hasInterface("xe-0/0/8", hasBandwidth(10E9)));
    assertThat(c, hasInterface("xe-0/0/8.0", hasBandwidth(10E9)));
    assertThat(c, hasInterface("et-0/0/40", hasBandwidth(25E9)));
    assertThat(c, hasInterface("et-0/0/41", hasBandwidth(25E9)));
  }
}
