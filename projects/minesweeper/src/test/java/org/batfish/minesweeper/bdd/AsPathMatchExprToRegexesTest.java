package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.bdd.AsPathMatchExprToRegexes.ASSUMED_MAX_AS_PATH_LENGTH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Range;
import java.util.Set;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.datamodel.routing_policy.as_path.HasAsPathLength;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.ConfigAtomicPredicatesTestUtils;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.bdd.TransferBDD.Context;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link AsPathMatchExprToRegexes}. */
public class AsPathMatchExprToRegexesTest {
  private static final String HOSTNAME = "hostname";
  private static final String POLICY_NAME = "policy";
  private Configuration _baseConfig;
  private CommunitySetMatchExprToBDD.Arg _arg;
  private AsPathMatchExprToRegexes _matchExprToRegexes;

  private static final String ASPATH1 = " 40$";
  private static final String ASPATH2 = "^$";
  private SymbolicAsPathRegex _asPath1Regex;
  private SymbolicAsPathRegex _asPath2Regex;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    IBatfish batfish =
        new TransferBDDTest.MockBatfish(ImmutableSortedMap.of(HOSTNAME, _baseConfig));

    ConfigAtomicPredicates configAPs =
        ConfigAtomicPredicatesTestUtils.forDevice(
            batfish, batfish.getSnapshot(), HOSTNAME, null, ImmutableSet.of(ASPATH1, ASPATH2));
    TransferBDD transferBDD = new TransferBDD(configAPs);
    BDDRoute bddRoute = new BDDRoute(transferBDD.getFactory(), configAPs);
    RoutingPolicy policy =
        nf.routingPolicyBuilder().setOwner(_baseConfig).setName(POLICY_NAME).build();
    _arg = new CommunitySetMatchExprToBDD.Arg(transferBDD, bddRoute, Context.forPolicy(policy));
    _matchExprToRegexes = new AsPathMatchExprToRegexes();

    _asPath1Regex = new SymbolicAsPathRegex(ASPATH1);
    _asPath2Regex = new SymbolicAsPathRegex(ASPATH2);
  }

  @Test
  public void testVisitAsPathMatchAny() {
    assertTrue(AsPathMatchAny.of(ImmutableList.of()).accept(_matchExprToRegexes, _arg).isEmpty());

    Set<SymbolicAsPathRegex> result =
        AsPathMatchAny.of(
                ImmutableList.of(AsPathMatchRegex.of(ASPATH1), AsPathMatchRegex.of(ASPATH2)))
            .accept(_matchExprToRegexes, _arg);
    assertEquals(ImmutableSet.of(_asPath1Regex, _asPath2Regex), result);
  }

  @Test
  public void testAsPathMatchExprReference() {
    String name = "name";

    _baseConfig.setAsPathMatchExprs(ImmutableMap.of(name, AsPathMatchRegex.of(ASPATH1)));

    AsPathMatchExprReference reference = AsPathMatchExprReference.of(name);

    assertEquals(ImmutableSet.of(_asPath1Regex), reference.accept(_matchExprToRegexes, _arg));
  }

  @Test
  public void testAsPathMatchRegex() {
    assertEquals(
        ImmutableSet.of(_asPath1Regex),
        AsPathMatchRegex.of(ASPATH1).accept(_matchExprToRegexes, _arg));
  }

  @Test
  public void testAsSetsMatchingRanges() {
    AsSetsMatchingRanges expr =
        AsSetsMatchingRanges.of(false, true, ImmutableList.of(Range.closed(11L, 14L)));
    assertEquals(
        ImmutableSet.of(new SymbolicAsPathRegex(expr)), expr.accept(_matchExprToRegexes, _arg));
  }

  @Test
  public void testHasAsPathLength() {
    // EQ is invalid
    HasAsPathLength eq10 =
        HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(10)));
    assertThrows(UnsupportedOperationException.class, () -> eq10.accept(_matchExprToRegexes, _arg));

    // Boundary conditions that evaluate to false (or crash, in EQ's case).
    for (HasAsPathLength expr :
        ImmutableList.of(
            HasAsPathLength.of(new IntComparison(IntComparator.GE, new LiteralInt(1))),
            HasAsPathLength.of(new IntComparison(IntComparator.GT, new LiteralInt(0))),
            HasAsPathLength.of(
                new IntComparison(IntComparator.LT, new LiteralInt(ASSUMED_MAX_AS_PATH_LENGTH))),
            HasAsPathLength.of(
                new IntComparison(
                    IntComparator.LE, new LiteralInt(ASSUMED_MAX_AS_PATH_LENGTH - 1))))) {
      assertThat(expr.accept(_matchExprToRegexes, _arg), empty());
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
      assertThat(
          expr.accept(_matchExprToRegexes, _arg), contains(SymbolicAsPathRegex.ALL_AS_PATHS));
    }
  }
}
