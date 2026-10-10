package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Test;

/** Tests of {@link IncrementMetricByIgpCost}. */
public final class IncrementMetricByIgpCostTest {

  private static Environment.Builder environment() {
    Configuration c =
        new NetworkFactory()
            .configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    return Environment.builder(c)
        .setDirection(Direction.OUT)
        .setOriginalRoute(
            StaticRoute.testBuilder()
                .setNetwork(Prefix.parse("10.0.0.0/24"))
                .setNextHopIp(Ip.parse("1.1.1.1"))
                .setMetric(5L)
                .build());
  }

  @Test
  public void testEvaluateWithoutIgpCost() {
    assertThat(IncrementMetricByIgpCost.instance().evaluate(environment().build()), equalTo(5L));
    assertThat(
        IncrementMetricByIgpCost.instance()
            .evaluate(environment().setIgpCostToNextHop(() -> null).build()),
        equalTo(5L));
  }

  @Test
  public void testEvaluateAddsIgpCost() {
    assertThat(
        IncrementMetricByIgpCost.instance()
            .evaluate(environment().setIgpCostToNextHop(() -> 20L).build()),
        equalTo(25L));
  }

  @Test
  public void testEvaluateClips() {
    assertThat(
        IncrementMetricByIgpCost.instance()
            .evaluate(environment().setIgpCostToNextHop(() -> 0xFFFFFFFFL).build()),
        equalTo(0xFFFFFFFFL));
  }

  @Test
  public void testJsonSerialization() {
    LongExpr expr = IncrementMetricByIgpCost.instance();
    assertThat(BatfishObjectMapper.clone(expr, LongExpr.class), equalTo(expr));
  }
}
