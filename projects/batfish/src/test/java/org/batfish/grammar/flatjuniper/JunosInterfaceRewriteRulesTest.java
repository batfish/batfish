package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.ClassOfServiceInterface.RewriteRuleType.DSCP;
import static org.batfish.representation.juniper.ClassOfServiceInterface.RewriteRuleType.DSCP_IPV6;
import static org.batfish.representation.juniper.ClassOfServiceInterface.RewriteRuleType.EXP;
import static org.batfish.representation.juniper.ClassOfServiceInterface.RewriteRuleType.IEEE_802_1;
import static org.batfish.representation.juniper.ClassOfServiceInterface.RewriteRuleType.INET_PRECEDENCE;
import static org.batfish.representation.juniper.JuniperStructureType.CLASS_OF_SERVICE_REWRITE_RULE;
import static org.batfish.representation.juniper.JuniperStructureUsage.CLASS_OF_SERVICE_INTERFACES_REWRITE_RULES_DSCP;
import static org.batfish.representation.juniper.JuniperStructureUsage.CLASS_OF_SERVICE_INTERFACES_REWRITE_RULES_EXP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.ClassOfServiceInterface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInterfaceRewriteRulesTest {

  private static final String HOSTNAME = "interface-rewrite-rules";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testInterfaceRewriteRules() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(configuration.getMasterLogicalSystem().getClassOfServiceInterfaces(), hasKey("all"));
    ClassOfServiceInterface all =
        configuration.getMasterLogicalSystem().getClassOfServiceInterfaces().get("all");
    assertThat(all.getRewriteRules().get(DSCP).getName(), equalTo("WRITE-DSCP"));
    assertThat(all.getRewriteRules().get(DSCP_IPV6).getName(), equalTo("WRITE-DSCP6"));
    assertThat(all.getRewriteRules().get(EXP).getName(), equalTo("WRITE-EXP"));
    assertThat(all.getRewriteRules().get(EXP).getProtocol(), equalTo("mpls-inet-both"));
    assertThat(all.getRewriteRules().get(IEEE_802_1).getName(), equalTo("WRITE-DOT1P"));
    assertThat(all.getRewriteRules().get(INET_PRECEDENCE).getName(), equalTo("WRITE-PRECEDENCE"));
    ClassOfServiceInterface concrete =
        configuration.getMasterLogicalSystem().getClassOfServiceInterfaces().get("ae0");
    assertThat(concrete.getRewriteRules().get(DSCP).getName(), equalTo("WRITE-DSCP"));
    ClassOfServiceInterface wildcard =
        configuration.getMasterLogicalSystem().getClassOfServiceInterfaces().get("et-*");
    assertThat(wildcard.getRewriteRules().get(EXP).getName(), equalTo("WRITE-EXP"));

    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("rewrite-rules dscp WRITE-DSCP"),
            isTodo("rewrite-rules dscp-ipv6 WRITE-DSCP6"),
            isTodo("rewrite-rules exp WRITE-EXP protocol mpls-inet-both"),
            isTodo("rewrite-rules ieee-802.1 WRITE-DOT1P"),
            isTodo("rewrite-rules inet-precedence WRITE-PRECEDENCE"),
            isTodo("rewrite-rules dscp WRITE-DSCP"),
            isTodo("rewrite-rules exp WRITE-EXP")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae,
        hasReferencedStructure(
            filename,
            CLASS_OF_SERVICE_REWRITE_RULE,
            "WRITE-DSCP",
            CLASS_OF_SERVICE_INTERFACES_REWRITE_RULES_DSCP));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename,
            CLASS_OF_SERVICE_REWRITE_RULE,
            "WRITE-EXP",
            CLASS_OF_SERVICE_INTERFACES_REWRITE_RULES_EXP));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
