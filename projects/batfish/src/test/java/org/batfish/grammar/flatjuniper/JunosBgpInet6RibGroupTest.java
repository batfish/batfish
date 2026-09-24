package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getNamedBgpGroup;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.RIB_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_FAMILY_INET6_UNICAST_RIB_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_FAMILY_INET_UNICAST_RIB_GROUP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.NamedBgpGroup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpInet6RibGroupTest {

  private static final String HOSTNAME = "bgp-inet6-rib-group";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testInet6RibGroup() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    NamedBgpGroup group = getNamedBgpGroup(batfish, HOSTNAME, "PEERS");

    assertThat(group.getRibGroup(), equalTo("IPV4-RIBS"));
    assertThat(group.getRibGroup6(), equalTo("IPV6-RIBS"));
    assertThat(getParseWarnings(batfish, HOSTNAME), contains(isTodo("rib-group IPV6-RIBS")));

    Configuration config = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .get(Ip.parse("198.51.100.2"))
            .getAppliedRibGroup()
            .getName(),
        equalTo("IPV4-RIBS"));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, RIB_GROUP, "IPV4-RIBS", BGP_FAMILY_INET_UNICAST_RIB_GROUP));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, RIB_GROUP, "IPV6-RIBS", BGP_FAMILY_INET6_UNICAST_RIB_GROUP));
  }
}
