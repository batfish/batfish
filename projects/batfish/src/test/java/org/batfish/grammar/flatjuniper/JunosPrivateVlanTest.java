package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.VLAN;
import static org.batfish.representation.juniper.JuniperStructureUsage.VLAN_ISOLATED_VLAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.Vlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JunosPrivateVlanTest {

  private static final String HOSTNAME = "junos-private-vlan";

  @TempDir public File _folder;

  @Test
  void testPrivateVlan() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    Vlan isolated = jc.getMasterLogicalSystem().getNamedVlans().get("ISOLATED");
    Vlan primary = jc.getMasterLogicalSystem().getNamedVlans().get("PRIMARY");

    assertThat(isolated.getPrivateVlanIsolated(), is(true));
    assertThat(primary.getIsolatedVlan(), equalTo("ISOLATED"));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("private-vlan isolated"), isTodo("isolated-vlan ISOLATED")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae, hasReferencedStructure("configs/" + HOSTNAME, VLAN, "ISOLATED", VLAN_ISOLATED_VLAN));
  }
}
