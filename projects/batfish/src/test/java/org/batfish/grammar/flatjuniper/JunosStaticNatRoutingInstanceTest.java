package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_STATIC_RULE_THEN_ROUTING_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.Nat;
import org.batfish.representation.juniper.NatRuleSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosStaticNatRoutingInstanceTest {

  private static final String HOSTNAME = "junos-static-nat-routing-instance";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testStaticNatRoutingInstance() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    Nat nat = jc.getMasterLogicalSystem().getNatStatic();
    NatRuleSet ruleSet = nat.getRuleSets().get("STATIC");

    assertThat(ruleSet.getRules().get(0).getStaticNatRoutingInstance(), equalTo("VRF"));
    assertThat(ruleSet.getRules().get(1).getStaticNatRoutingInstance(), equalTo("MISSING"));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("routing-instance VRF"), isTodo("routing-instance MISSING")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ROUTING_INSTANCE, "VRF", NAT_STATIC_RULE_THEN_ROUTING_INSTANCE));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, ROUTING_INSTANCE, "MISSING", NAT_STATIC_RULE_THEN_ROUTING_INSTANCE));
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }
}
