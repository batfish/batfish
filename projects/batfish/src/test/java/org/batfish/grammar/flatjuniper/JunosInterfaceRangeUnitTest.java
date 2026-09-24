package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceRangeUnitTest {

  private static final String HOSTNAME = "junos-interface-range-unit";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testInterfaceRangeUnit() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    Configuration config = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    assertThat(config.getAllInterfaces(), hasKey("ge-0/0/0.0"));
    Interface inherited = config.getAllInterfaces().get("ge-0/0/0.0");
    assertThat(inherited.getDescription(), equalTo("RANGE_UNIT"));
    assertThat(inherited.getSwitchportMode(), equalTo(SwitchportMode.ACCESS));
    assertThat(inherited.getAccessVlan(), equalTo(100));

    assertThat(config.getAllInterfaces(), hasKey("ge-0/0/1.0"));
    Interface overridden = config.getAllInterfaces().get("ge-0/0/1.0");
    assertThat(overridden.getDescription(), equalTo("EXPLICIT"));
    assertThat(overridden.getSwitchportMode(), equalTo(SwitchportMode.ACCESS));
    assertThat(overridden.getAccessVlan(), equalTo(100));

    assertThat(config.getAllInterfaces(), hasKey("ge-0/0/2.0"));
    assertThat(config.getAllInterfaces().get("ge-0/0/2.0").getDescription(), equalTo("RESERVED"));
  }
}
