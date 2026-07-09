package org.batfish.minesweeper.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.junit.Test;

/** Tests for {@link BDDRouteFactory}. */
public class BDDRouteFactoryTest {
  private static final ConfigAtomicPredicates CONFIG_APS =
      new ConfigAtomicPredicates(
          ImmutableList.of(),
          ImmutableSet.of(CommunityVar.from("30:30"), CommunityVar.from("40:40")),
          ImmutableSet.of());

  @Test
  public void testZeroRoutesRejected() {
    assertThrows(IllegalArgumentException.class, () -> new BDDRouteFactory(CONFIG_APS, 0));
  }

  @Test
  public void testSingleRouteIsCanonical() {
    // With a single route requested, identityRoute(0) must be the same as building a BDDRoute
    // directly against a fresh factory for the same aps.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 1);
    BDDRoute direct = new BDDRoute(routeFactory.getFactory(), CONFIG_APS);
    assertThat(routeFactory.identityRoute(0), equalTo(direct));
  }

  @Test
  public void testRoutesAreMutuallyInterleaved() {
    // The routes must be pairwise interleaved, not contiguous: each of the first route's
    // non-prefix variables' level must be immediately followed, in the factory's variable order,
    // by its counterpart in the second and then in the third.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 3);
    BDDRoute first = routeFactory.identityRoute(0);
    BDDRoute second = routeFactory.identityRoute(1);
    BDDRoute third = routeFactory.identityRoute(2);

    int[] firstLevels = sortedLevels(first.mutableSupport());
    int[] secondLevels = sortedLevels(second.mutableSupport());
    int[] thirdLevels = sortedLevels(third.mutableSupport());
    assertThat(secondLevels.length, equalTo(firstLevels.length));
    assertThat(thirdLevels.length, equalTo(firstLevels.length));
    for (int i = 0; i < firstLevels.length; i++) {
      assertThat(secondLevels[i], equalTo(firstLevels[i] + 1));
      assertThat(thirdLevels[i], equalTo(firstLevels[i] + 2));
    }

    // A blown-up (non-interleaved) relation over hundreds of bits would have astronomically more
    // nodes than this; a generous but finite bound catches a regression without being brittle.
    BDD firstVsSecond = first.equalsRelation(second);
    BDD firstVsThird = first.equalsRelation(third);
    BDD secondVsThird = second.equalsRelation(third);
    assertThat(firstVsSecond.nodeCount(), lessThan(10_000));
    assertThat(firstVsThird.nodeCount(), lessThan(10_000));
    assertThat(secondVsThird.nodeCount(), lessThan(10_000));

    assertThat(routeFactory.identityRoutes(), equalTo(ImmutableList.of(first, second, third)));
  }

  /**
   * The factory-order levels of every BDD variable in {@code support}'s support, sorted ascending.
   */
  private static int[] sortedLevels(BDD support) {
    int[] vars = support.scanSet();
    BDDFactory factory = support.getFactory();
    int[] levels = new int[vars.length];
    for (int i = 0; i < vars.length; i++) {
      levels[i] = factory.var2Level(vars[i]);
    }
    Arrays.sort(levels);
    return levels;
  }

  @Test
  public void testExplicitFactoryWithNoSharedBits() {
    // The (BDDFactory, ConfigAtomicPredicates, int) overload -- a caller-supplied factory, no
    // shared bits -- must build the same shape as passing numSharedBits=0 explicitly.
    BDDFactory factory = JFactory.init(100000, 10000);
    BDDRouteFactory routeFactory = new BDDRouteFactory(factory, CONFIG_APS, 3);
    assertThat(routeFactory.getFactory(), equalTo(factory));
    assertThat(routeFactory.numSharedBits(), equalTo(0));
    assertThat(routeFactory.identityRoutes().size(), equalTo(3));
  }

  @Test
  public void testTransferBDDCanReuseIdentityRoute() {
    // A caller that needs both a TransferBDD and other routes interleaved with its canonical route
    // must build the canonical route once, via BDDRouteFactory, and hand it to the
    // TransferBDD(factory, originalRoute, aps) overload -- which stores it as-is -- rather than to
    // TransferBDD(factory, aps), which builds its OWN second canonical route that
    // BDDRouteFactory's other routes are not interleaved with (a hazard distinct from object
    // equality: for the same aps, that second route happens to allocate the identical variable
    // range and so is .equals() to routeFactory's, since BDDRoute equality is purely structural --
    // it is the interleaving, not the identity, that TransferBDD(factory, aps) would get wrong).
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 2);
    TransferBDD tbdd =
        new TransferBDD(routeFactory.getFactory(), routeFactory.identityRoute(0), CONFIG_APS);
    assertThat(tbdd.getOriginalRoute(), equalTo(routeFactory.identityRoute(0)));
  }

  @Test
  public void testMutableRouteIsIndependentCopy() {
    // mutableRoute(i) must be a genuinely independent deep copy: mutating it must not affect
    // identityRoute(i) or any other mutableRoute(i) call's result.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 1);
    BDDRoute identity = routeFactory.identityRoute(0);
    BDDRoute mutableA = routeFactory.mutableRoute(0);
    BDDRoute mutableB = routeFactory.mutableRoute(0);
    assertThat(mutableA, equalTo(identity));

    mutableA.getLocalPref().setValue(100);
    assertThat(mutableA, not(equalTo(identity)));
    assertThat(mutableB, equalTo(identity));
    assertThat(routeFactory.identityRoute(0), equalTo(identity));
  }

  @Test
  public void testSharedBitsAreReservedAndDisjointFromEveryRoute() {
    // numSharedBits() only reserves indices [0, numSharedBits()) -- it's the caller's job to build
    // something over them, e.g. a MutableBDDInteger. Those reserved variables sit at the very front
    // of the var order; the shared prefix/prefix-length variables (which support() includes, and
    // every route shares the SAME instance of, unlike its own non-prefix variables) come right
    // after, so every route's support -- not just its own non-prefix portion -- has its lowest
    // (top) level exactly at numSharedBits(), never lower: the reserved range is never touched.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 2, 4);
    assertThat(routeFactory.numSharedBits(), equalTo(4));

    assertThat(routeFactory.identityRoute(0).support().level(), equalTo(4));
    assertThat(routeFactory.identityRoute(1).support().level(), equalTo(4));
  }

  @Test
  public void testSharedBitsDoNotPerturbInterleaving() {
    // Requesting shared bits must not change the interleaving/width math for the routes: each of
    // the first route's non-prefix variables' level must still be immediately followed by its
    // counterpart in the second, exactly as without shared bits.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 2, 4);
    BDDRoute first = routeFactory.identityRoute(0);
    BDDRoute second = routeFactory.identityRoute(1);

    int[] firstLevels = sortedLevels(first.mutableSupport());
    int[] secondLevels = sortedLevels(second.mutableSupport());
    assertThat(secondLevels.length, equalTo(firstLevels.length));
    for (int i = 0; i < firstLevels.length; i++) {
      assertThat(secondLevels[i], equalTo(firstLevels[i] + 1));
    }

    BDD firstVsSecond = first.equalsRelation(second);
    assertThat(firstVsSecond.nodeCount(), lessThan(10_000));
  }

  @Test
  public void testNoSharedBitsRequestedIsZero() {
    // With no shared bits requested, numSharedBits() is 0, and the first route is allocated
    // exactly as if BDDRouteFactory had no shared-bits support at all.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 1);
    assertThat(routeFactory.numSharedBits(), equalTo(0));
    BDDRoute direct = new BDDRoute(routeFactory.getFactory(), CONFIG_APS);
    assertThat(routeFactory.identityRoute(0), equalTo(direct));
  }
}
