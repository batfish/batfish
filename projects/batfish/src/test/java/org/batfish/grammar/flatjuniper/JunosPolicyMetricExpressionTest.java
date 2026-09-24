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
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.PolicyStatement;
import org.batfish.representation.juniper.PsThenMetricExpression;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyMetricExpressionTest {

  private static final String HOSTNAME = "policy-metric-expression";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    PolicyStatement policy =
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("METRIC-EXPRESSION");

    assertThat(
        policy.getTerms().get("METRIC-NEGATIVE").getThens().getAllThens(),
        contains(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC,
                2L,
                -50L)));
    assertThat(
        policy.getTerms().get("METRIC2-POSITIVE").getThens().getAllThens(),
        contains(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC2,
                1L,
                50L)));
    assertThat(
        policy.getTerms().get("TARGET-METRIC2").getThens().getAllThens(),
        contains(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC2,
                PsThenMetricExpression.Source.METRIC,
                3L,
                0L)));
    assertThat(
        policy.getTerms().get("TARGET-METRIC2-NEGATIVE").getThens().getAllThens(),
        contains(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC2,
                PsThenMetricExpression.Source.METRIC2,
                4L,
                -10L)));
    assertThat(
        policy.getTerms().get("OFFSET-ONLY").getThens().getAllThens(),
        contains(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC2,
                1L,
                -5L)));
    assertThat(
        policy.getTerms().get("COMBINED").getThens().getAllThens(),
        contains(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC, 1L, 0L),
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC2,
                1L,
                0L)));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("expression metric multiplier 2 offset -50"),
            isTodo("expression metric2 multiplier 1 offset 50"),
            isTodo("expression metric multiplier 3"),
            isTodo("expression metric2 multiplier 4 offset - 10"),
            isTodo("expression metric2 offset -5"),
            isTodo("expression metric multiplier 1"),
            isTodo("expression metric2 multiplier 1")));

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("METRIC-EXPRESSION"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
