package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.ACCESS_PROFILE;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.DOT1X_AUTHENTICATION_PROFILE;
import static org.batfish.representation.juniper.JuniperStructureUsage.DOT1X_AUTHENTICATOR_INTERFACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosDot1xTest {

  private static final String HOSTNAME = "junos-dot1x";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDot1x() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;

    assertThat(ccae, hasDefinedStructure(filename, ACCESS_PROFILE, "RADIUS"));
    assertThat(
        ccae,
        hasReferencedStructure(filename, ACCESS_PROFILE, "RADIUS", DOT1X_AUTHENTICATION_PROFILE));
    assertThat(
        ccae,
        hasReferencedStructure(filename, INTERFACE, "ge-0/0/0.0", DOT1X_AUTHENTICATOR_INTERFACE));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("authentication-profile-name RADIUS"),
            isTodo("interface ge-0/0/0.0 authentication-order dot1x"),
            isTodo("interface ge-0/0/0.0 authentication-order mac-radius"),
            isTodo("interface ge-0/0/0.0 mac-radius authentication-protocol pap"),
            isTodo("interface ge-0/0/0.0 reauthentication 3600"),
            isTodo("interface ge-0/0/0.0 server-fail vlan-name fallback"),
            isTodo("interface ge-0/0/0.0 server-reject-vlan rejected"),
            isTodo("interface ge-0/0/0.0 server-timeout 30"),
            isTodo("interface ge-0/0/0.0 supplicant multiple"),
            isTodo("interface ge-0/0/0.0 transmit-period 30")));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
