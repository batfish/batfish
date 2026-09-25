package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.batfish.representation.juniper.SecurityForwardingOptions.Family.INET;
import static org.batfish.representation.juniper.SecurityForwardingOptions.Family.INET6;
import static org.batfish.representation.juniper.SecurityForwardingOptions.Family.ISO;
import static org.batfish.representation.juniper.SecurityForwardingOptions.Family.MPLS;
import static org.batfish.representation.juniper.SecurityForwardingOptions.Mode.DROP;
import static org.batfish.representation.juniper.SecurityForwardingOptions.Mode.FLOW_BASED;
import static org.batfish.representation.juniper.SecurityForwardingOptions.Mode.PACKET_BASED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;

import java.io.IOException;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.SecurityForwardingOptions.Family;
import org.batfish.representation.juniper.SecurityForwardingOptions.Mode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosSecurityForwardingOptionsTest {

  private static final String HOSTNAME = "security-forwarding-options";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testNoConversionWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }

  @Test
  public void testSecurityForwardingOptions() {
    JuniperConfiguration config = parseJuniperConfig(_folder, HOSTNAME);
    Map<Family, Mode> modes =
        config.getMasterLogicalSystem().getSecurityForwardingOptions().getFamilyModes();

    assertThat(modes, hasEntry(INET, PACKET_BASED));
    assertThat(modes, hasEntry(INET6, PACKET_BASED));
    assertThat(modes, hasEntry(ISO, PACKET_BASED));
    assertThat(modes, hasEntry(MPLS, FLOW_BASED));
    assertThat(
        config.getWarnings().getParseWarnings(),
        containsInAnyOrder(
            isTodo("family inet mode packet-based"), isTodo("family inet6 mode packet-based")));
  }

  @Test
  public void testDropMode() {
    JuniperConfiguration config = parseJuniperConfig(_folder, "security-forwarding-options-drop");

    assertThat(
        config.getMasterLogicalSystem().getSecurityForwardingOptions().getFamilyModes(),
        hasEntry(INET, DROP));
    assertThat(
        config.getWarnings().getParseWarnings(),
        containsInAnyOrder(isTodo("family inet mode drop")));
  }
}
