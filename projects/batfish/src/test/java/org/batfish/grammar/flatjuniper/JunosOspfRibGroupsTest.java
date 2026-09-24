package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.RIB_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.OSPF3_RIB_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.OSPF_RIB_GROUP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosOspfRibGroupsTest {

  private static final String HOSTNAME = "ospf-rib-groups";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndReferences() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getOspf3RibGroups()
            .get("inet6"),
        equalTo("OSPF3-RIB-GROUP"));
    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getOspfRibGroups()
            .get("inet"),
        equalTo("INET-RIB-GROUP"));
    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getOspfRibGroups()
            .get("inet3"),
        equalTo("INET3-RIB-GROUP"));
    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getRoutingInstances()
            .get("CLIENT")
            .getOspfRibGroups()
            .get("inet"),
        equalTo("LEGACY-RIB-GROUP"));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("rib-groups inet INET-RIB-GROUP"),
            isTodo("rib-groups inet3 INET3-RIB-GROUP"),
            isTodo("rib-group OSPF3-RIB-GROUP"),
            isTodo("rib-group LEGACY-RIB-GROUP")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure("configs/" + HOSTNAME, RIB_GROUP, "INET-RIB-GROUP", OSPF_RIB_GROUP));
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, RIB_GROUP, "INET3-RIB-GROUP", OSPF_RIB_GROUP));
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, RIB_GROUP, "LEGACY-RIB-GROUP", OSPF_RIB_GROUP));
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, RIB_GROUP, "OSPF3-RIB-GROUP", OSPF3_RIB_GROUP));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
