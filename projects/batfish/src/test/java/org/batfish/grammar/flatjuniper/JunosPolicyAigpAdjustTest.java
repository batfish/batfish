package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import java.math.BigInteger;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.PolicyStatement;
import org.batfish.representation.juniper.PsThenAigpAdjust;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyAigpAdjustTest {

  private static final String HOSTNAME = "policy-aigp-adjust";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    PolicyStatement policy =
        juniperConfiguration.getMasterLogicalSystem().getPolicyStatements().get("AIGP-ADJUST");

    assertThat(
        policy.getTerms().get("ADD-DISTANCE").getThens().getAllThens(),
        contains(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.ADD, null)));
    assertThat(
        policy.getTerms().get("DIVIDE-VALUE").getThens().getAllThens(),
        contains(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.DIVIDE, BigInteger.valueOf(2L))));
    assertThat(
        policy.getTerms().get("MULTIPLY-VALUE").getThens().getAllThens(),
        contains(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.MULTIPLY, BigInteger.valueOf(3L))));
    assertThat(
        policy.getTerms().get("SUBTRACT-VALUE").getThens().getAllThens(),
        contains(
            new PsThenAigpAdjust(PsThenAigpAdjust.Operator.SUBTRACT, BigInteger.valueOf(900000L))));
    assertThat(
        policy.getTerms().get("ADD-MAX").getThens().getAllThens(),
        contains(
            new PsThenAigpAdjust(
                PsThenAigpAdjust.Operator.ADD, new BigInteger("18446744073709551615"))));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("aigp-adjust add distance-to-protocol-nexthop"),
            isTodo("aigp-adjust divide 2"),
            isTodo("aigp-adjust multiply 3"),
            isTodo("aigp-adjust subtract 900000"),
            isTodo("aigp-adjust add 18446744073709551615")));

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("AIGP-ADJUST"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
