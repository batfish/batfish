package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.Interface.InterfaceType.PPP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPpInterfaceTest {

  private static final String HOSTNAME = "junos-pp-interface";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testPpInterface() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(jc.getMasterLogicalSystem().getInterfaces().get("pp0").getType(), equalTo(PPP));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(configuration, hasInterface("pp0", isActive(false)));
  }
}
