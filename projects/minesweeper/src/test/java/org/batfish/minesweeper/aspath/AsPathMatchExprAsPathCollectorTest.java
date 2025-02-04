package org.batfish.minesweeper.aspath;

import static org.batfish.minesweeper.bdd.AsPathMatchExprToRegexes.ASSUMED_MAX_AS_PATH_LENGTH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.datamodel.routing_policy.as_path.HasAsPathLength;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link org.batfish.minesweeper.aspath.AsPathMatchExprAsPathCollector}. */
public class AsPathMatchExprAsPathCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private AsPathMatchExprAsPathCollector _collector;

  private static final String ASPATH1 = " 40$";
  private static final String ASPATH2 = "^$";

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new AsPathMatchExprAsPathCollector();
  }

  @Test
  public void testVisitAsPathMatchAny() {
    assertEquals(
        ImmutableSet.of(), AsPathMatchAny.of(ImmutableList.of()).accept(_collector, _baseConfig));

    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1), new SymbolicAsPathRegex(ASPATH2)),
        AsPathMatchAny.of(
                ImmutableList.of(AsPathMatchRegex.of(ASPATH1), AsPathMatchRegex.of(ASPATH2)))
            .accept(_collector, _baseConfig));
  }

  @Test
  public void testAsPathMatchExprReference() {
    String name = "name";

    _baseConfig.setAsPathMatchExprs(ImmutableMap.of(name, AsPathMatchRegex.of(ASPATH1)));

    AsPathMatchExprReference reference = AsPathMatchExprReference.of(name);

    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1)),
        reference.accept(_collector, _baseConfig));
  }

  @Test
  public void testAsPathMatchRegex() {
    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(ASPATH1)),
        AsPathMatchRegex.of(ASPATH1).accept(_collector, _baseConfig));
  }

  @Test
  public void testAsSetsMatchingRanges() {
    AsSetsMatchingRanges expr =
        AsSetsMatchingRanges.of(false, true, ImmutableList.of(Range.closed(11L, 14L)));
    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(expr)), expr.accept(_collector, _baseConfig));
  }

  @Test
  public void testHasAsPathLength() {
    // Boundary conditions that evaluate to false (or crash, in EQ's case).
    for (HasAsPathLength expr :
        ImmutableList.of(
            HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(10))),
            HasAsPathLength.of(new IntComparison(IntComparator.GE, new LiteralInt(1))),
            HasAsPathLength.of(new IntComparison(IntComparator.GT, new LiteralInt(0))),
            HasAsPathLength.of(
                new IntComparison(IntComparator.LT, new LiteralInt(ASSUMED_MAX_AS_PATH_LENGTH))),
            HasAsPathLength.of(
                new IntComparison(
                    IntComparator.LE, new LiteralInt(ASSUMED_MAX_AS_PATH_LENGTH - 1))))) {
      assertThat(expr.accept(_collector, _baseConfig), empty());
    }

    // Boundary conditions that evaluate to true.
    for (HasAsPathLength expr :
        ImmutableList.of(
            HasAsPathLength.of(new IntComparison(IntComparator.GE, new LiteralInt(0))),
            HasAsPathLength.of(new IntComparison(IntComparator.GT, new LiteralInt(-1))),
            HasAsPathLength.of(
                new IntComparison(
                    IntComparator.LT, new LiteralInt(ASSUMED_MAX_AS_PATH_LENGTH + 1))),
            HasAsPathLength.of(
                new IntComparison(IntComparator.LE, new LiteralInt(ASSUMED_MAX_AS_PATH_LENGTH))))) {
      assertThat(expr.accept(_collector, _baseConfig), contains(SymbolicAsPathRegex.ALL_AS_PATHS));
    }
  }
}
