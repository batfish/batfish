package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public final class PsThenMetricExpressionTest {

  @Test
  public void testConversion() {
    PsThenMetricExpression expression =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC2, 2L, -50L);
    List<Statement> statements = new ArrayList<>();
    expression.applyTo(
        statements,
        new JuniperConfiguration(),
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("host")
            .build(),
        new Warnings());
    assertThat(expression.getMultiplier(), equalTo(2L));
    assertThat(expression.getOffset(), equalTo(-50L));
    assertThat(statements, empty());
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC,
                2L,
                -50L),
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC,
                2L,
                -50L))
        .addEqualityGroup(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC2,
                PsThenMetricExpression.Source.METRIC,
                2L,
                -50L))
        .addEqualityGroup(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC2,
                2L,
                -50L))
        .addEqualityGroup(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC,
                3L,
                -50L))
        .addEqualityGroup(
            new PsThenMetricExpression(
                PsThenMetricExpression.Target.METRIC,
                PsThenMetricExpression.Source.METRIC,
                2L,
                -40L))
        .testEquals();
  }

  @Test
  public void testMergeSourcesAndReplaceSource() {
    PsThens thens = new PsThens();
    PsThenMetricExpression metric =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC, 2L, 0L);
    PsThenMetricExpression metric2 =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC2, 1L, 0L);
    assertThat(thens.addPsThen(metric), empty());
    assertThat(thens.addPsThen(metric2), empty());
    assertThat(thens.getAllThens(), contains(metric, metric2));

    PsThenMetricExpression replacement =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC, 3L, 0L);
    assertThat(thens.addPsThen(replacement), contains("metric expression metric"));
    assertThat(thens.getAllThens(), contains(replacement, metric2));

    PsThenMetricExpression replacement2 =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC2, 4L, 0L);
    assertThat(thens.addPsThen(replacement2), contains("metric expression metric2"));
    assertThat(thens.addPsThen(replacement2), contains("metric expression metric2 (dedup)"));
    assertThat(thens.getAllThens(), contains(replacement, replacement2));
  }

  @Test
  public void testMergeSourcesInCanonicalOrder() {
    PsThens thens = new PsThens();
    PsThenMetricExpression metric2 =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC2, 1L, 0L);
    PsThenMetricExpression metric =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC, 2L, 0L);
    assertThat(thens.addPsThen(metric2), empty());
    assertThat(thens.addPsThen(metric), empty());
    assertThat(thens.getAllThens(), contains(metric, metric2));
  }

  @Test
  public void testExpressionReplacesLiteralMetric2() {
    PsThens thens = new PsThens();
    thens.addPsThen(new PsThenMetric2(10L, PsThenMetric2.Operator.SET));
    PsThenMetricExpression expression =
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC2, PsThenMetricExpression.Source.METRIC2, 2L, 0L);
    assertThat(thens.addPsThen(expression), contains("metric2"));
    assertThat(thens.getAllThens(), contains(expression));
  }

  @Test
  public void testLiteralMetricReplacesExpression() {
    PsThens thens = new PsThens();
    thens.addPsThen(
        new PsThenMetricExpression(
            PsThenMetricExpression.Target.METRIC, PsThenMetricExpression.Source.METRIC, 2L, 0L));
    PsThenMetric literal = new PsThenMetric(10L, PsThenMetric.Operator.SET);
    assertThat(thens.addPsThen(literal), contains("metric"));
    assertThat(thens.getAllThens(), contains(literal));
  }
}
