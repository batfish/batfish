package org.batfish.datamodel.routing_policy.as_path;

import static org.batfish.datamodel.Bgpv4Route.testBuilder;
import static org.batfish.datamodel.routing_policy.expr.IntComparator.EQ;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link MatchAsPath}. */
public final class MatchAsPathTest {

  @Test
  public void testEvaluate() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();

    MatchAsPath expr =
        MatchAsPath.of(
            InputAsPath.instance(), HasAsPathLength.of(new IntComparison(EQ, new LiteralInt(1))));

    // expect Result(true) when route has AsPath and match evaluates to true
    assertTrue(
        expr.evaluate(
                Environment.builder(c)
                    .setOriginalRoute(
                        testBuilder()
                            .setAsPath(AsPath.ofSingletonAsSets(5L))
                            .setNetwork(Prefix.ZERO)
                            .build())
                    .build())
            .getBooleanValue());

    // expect Result(false) when route has AsPath and match evaluates to false
    assertFalse(
        expr.evaluate(
                Environment.builder(c)
                    .setOriginalRoute(
                        testBuilder()
                            .setAsPath(AsPath.ofSingletonAsSets(5L, 6L))
                            .setNetwork(Prefix.ZERO)
                            .build())
                    .build())
            .getBooleanValue());

    // expect Result(false) when route has no AsPath
    assertFalse(
        expr.evaluate(
                Environment.builder(c)
                    .setOriginalRoute(new ConnectedRoute(Prefix.ZERO, "i1"))
                    .build())
            .getBooleanValue());
  }

  @Test
  public void testJavaSerialization() {
    MatchAsPath obj =
        MatchAsPath.of(
            InputAsPath.instance(), AsSetsMatchingRanges.of(false, false, ImmutableList.of()));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    MatchAsPath obj =
        MatchAsPath.of(
            InputAsPath.instance(), AsSetsMatchingRanges.of(false, false, ImmutableList.of()));
    assertThat(BatfishObjectMapper.clone(obj, MatchAsPath.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    MatchAsPath obj =
        MatchAsPath.of(
            InputAsPath.instance(), AsSetsMatchingRanges.of(false, false, ImmutableList.of()));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            MatchAsPath.of(
                InputAsPath.instance(), AsSetsMatchingRanges.of(false, false, ImmutableList.of())))
        .addEqualityGroup(
            MatchAsPath.of(
                DedupedAsPath.of(InputAsPath.instance()),
                AsSetsMatchingRanges.of(false, false, ImmutableList.of())))
        .addEqualityGroup(
            MatchAsPath.of(
                InputAsPath.instance(), AsSetsMatchingRanges.of(true, false, ImmutableList.of())))
        .testEquals();
  }
}
