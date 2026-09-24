package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.Interface.InterfaceType.PHYSICAL;
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

public final class JunosSxeInterfaceTest {

  private static final String HOSTNAME = "junos-sxe-interface";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testSxeInterface() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    assertThat(
        org.batfish.representation.juniper.Interface.getInterfaceTypeByName("sxe-0/0/0"),
        equalTo(PHYSICAL));

    Configuration config = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(config.getAllInterfaces(), hasKey("sxe-0/0/0"));
    Interface physical = config.getAllInterfaces().get("sxe-0/0/0");
    assertThat(physical.getDescription(), equalTo("internal-facing"));
    assertThat(physical.getBandwidth(), equalTo(1E10));
    assertThat(physical.getMtu(), equalTo(9192));

    assertThat(config.getAllInterfaces(), hasKey("sxe-0/0/0.0"));
    Interface unit = config.getAllInterfaces().get("sxe-0/0/0.0");
    assertThat(unit.getSwitchportMode(), equalTo(SwitchportMode.TRUNK));
  }
}
