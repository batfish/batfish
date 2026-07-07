package org.batfish.minesweeper.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

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
  public void testOriginalRouteIsCanonical() {
    // With no extra blocks requested, originalRoute() must be the same as building a BDDRoute
    // directly against a fresh factory for the same aps.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 0);
    BDDRoute direct = new BDDRoute(routeFactory.getFactory(), CONFIG_APS);
    assertThat(routeFactory.originalRoute(), equalTo(direct));
  }

  @Test
  public void testBlocksAreMutuallyInterleaved() {
    // The extra blocks (and the original route) must be pairwise interleaved, not contiguous:
    // each of origin's non-prefix variables' level must be immediately followed, in the factory's
    // variable order, by its counterpart in first and then in second.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 2);
    BDDRoute origin = routeFactory.originalRoute();
    BDDRoute first = routeFactory.identityBlock(0);
    BDDRoute second = routeFactory.identityBlock(1);

    int[] originLevels = sortedLevels(origin.mutableSupport());
    int[] firstLevels = sortedLevels(first.mutableSupport());
    int[] secondLevels = sortedLevels(second.mutableSupport());
    assertThat(firstLevels.length, equalTo(originLevels.length));
    assertThat(secondLevels.length, equalTo(originLevels.length));
    for (int i = 0; i < originLevels.length; i++) {
      assertThat(firstLevels[i], equalTo(originLevels[i] + 1));
      assertThat(secondLevels[i], equalTo(originLevels[i] + 2));
    }

    // A blown-up (non-interleaved) relation over hundreds of bits would have astronomically more
    // nodes than this; a generous but finite bound catches a regression without being brittle.
    BDD originVsFirst = origin.equalsRelation(first);
    BDD originVsSecond = origin.equalsRelation(second);
    BDD firstVsSecond = first.equalsRelation(second);
    assertThat(originVsFirst.nodeCount(), lessThan(10_000));
    assertThat(originVsSecond.nodeCount(), lessThan(10_000));
    assertThat(firstVsSecond.nodeCount(), lessThan(10_000));

    assertThat(routeFactory.identityBlocks(), equalTo(ImmutableList.of(first, second)));
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
    BDDRouteFactory routeFactory = new BDDRouteFactory(factory, CONFIG_APS, 2);
    assertThat(routeFactory.getFactory(), equalTo(factory));
    assertThat(routeFactory.numSharedBits(), equalTo(0));
    assertThat(routeFactory.identityBlocks().size(), equalTo(2));
  }

  @Test
  public void testTransferBDDCanReuseOriginalRoute() {
    // A caller that needs both a TransferBDD and fresh blocks interleaved with its canonical route
    // must build the canonical route once, via BDDRouteFactory, and hand it to the
    // TransferBDD(factory, originalRoute, aps) overload -- which stores originalRoute as-is --
    // rather than to TransferBDD(factory, aps), which builds its OWN second canonical route that
    // BDDRouteFactory's extra blocks are not interleaved with (a hazard distinct from object
    // equality: for the same aps, that second route happens to allocate the identical variable
    // range and so is .equals() to routeFactory's, since BDDRoute equality is purely structural --
    // it is the interleaving, not the identity, that TransferBDD(factory, aps) would get wrong).
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 1);
    TransferBDD tbdd =
        new TransferBDD(routeFactory.getFactory(), routeFactory.originalRoute(), CONFIG_APS);
    assertThat(tbdd.getOriginalRoute(), equalTo(routeFactory.originalRoute()));
  }

  @Test
  public void testSharedBitsAreReservedAndDisjointFromEveryBlock() {
    // numSharedBits() only reserves indices [0, numSharedBits()) -- it's the caller's job to build
    // something over them, e.g. a MutableBDDInteger. Those reserved variables sit at the very front
    // of the var order; the shared prefix/prefix-length variables (which support() includes, and
    // every block shares the SAME instance of, unlike its own non-prefix variables) come right
    // after, so every block's support -- not just its own non-prefix portion -- has its lowest
    // (top) level exactly at numSharedBits(), never lower: the reserved range is never touched.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 2, 4);
    assertThat(routeFactory.numSharedBits(), equalTo(4));

    assertThat(routeFactory.originalRoute().support().level(), equalTo(4));
    assertThat(routeFactory.identityBlock(0).support().level(), equalTo(4));
    assertThat(routeFactory.identityBlock(1).support().level(), equalTo(4));
  }

  @Test
  public void testSharedBitsDoNotPerturbInterleaving() {
    // Requesting shared bits must not change the interleaving/width math for the route blocks:
    // each of origin's non-prefix variables' level must still be immediately followed by its
    // counterpart in first and then in second, exactly as without shared bits.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 2, 4);
    BDDRoute origin = routeFactory.originalRoute();
    BDDRoute first = routeFactory.identityBlock(0);
    BDDRoute second = routeFactory.identityBlock(1);

    int[] originLevels = sortedLevels(origin.mutableSupport());
    int[] firstLevels = sortedLevels(first.mutableSupport());
    int[] secondLevels = sortedLevels(second.mutableSupport());
    assertThat(firstLevels.length, equalTo(originLevels.length));
    assertThat(secondLevels.length, equalTo(originLevels.length));
    for (int i = 0; i < originLevels.length; i++) {
      assertThat(firstLevels[i], equalTo(originLevels[i] + 1));
      assertThat(secondLevels[i], equalTo(originLevels[i] + 2));
    }

    BDD originVsFirst = origin.equalsRelation(first);
    BDD firstVsSecond = first.equalsRelation(second);
    assertThat(originVsFirst.nodeCount(), lessThan(10_000));
    assertThat(firstVsSecond.nodeCount(), lessThan(10_000));
  }

  @Test
  public void testNoSharedBitsRequestedIsZero() {
    // With no shared bits requested, numSharedBits() is 0, and the original route is allocated
    // exactly as if BDDRouteFactory had no shared-bits support at all.
    BDDRouteFactory routeFactory = new BDDRouteFactory(CONFIG_APS, 0);
    assertThat(routeFactory.numSharedBits(), equalTo(0));
    BDDRoute direct = new BDDRoute(routeFactory.getFactory(), CONFIG_APS);
    assertThat(routeFactory.originalRoute(), equalTo(direct));
  }
}
