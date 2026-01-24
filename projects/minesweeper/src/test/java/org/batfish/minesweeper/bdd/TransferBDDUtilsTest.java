package org.batfish.minesweeper.bdd;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.junit.Before;
import org.junit.Test;

public class TransferBDDUtilsTest {

  private TransferBDD _tbdd;
  private BDDRoute _freshRoute;

  @Before
  public void setup() {
    ConfigAtomicPredicates configAPs =
        new ConfigAtomicPredicates(
            ImmutableList.of(),
            ImmutableSet.of(CommunityVar.from("30:30"), CommunityVar.from("40:40")),
            ImmutableSet.of());
    _tbdd = new TransferBDD(configAPs);
    _freshRoute = new BDDRoute(_tbdd.getFactory(), configAPs);
  }

  @Test
  public void testWeakestPrecondition() {

    BDDFactory factory = _tbdd.getFactory();

    // no paths
    List<TransferReturn> paths = ImmutableList.of();
    assertEquals(
        TransferBDDUtils.weakestPrecondition(paths, 0, _tbdd, (post, path) -> factory.one()),
        factory.zero());

    // the WP is the identity function
    paths = ImmutableList.of(new TransferReturn(_freshRoute, factory.one(), true));
    assertEquals(
        TransferBDDUtils.weakestPrecondition(
            paths, 0, _tbdd, (post, path) -> path.getOutputRoute().getLocalPref().value(300)),
        _freshRoute.getLocalPref().value(300));

    // ignore denying paths
    paths = ImmutableList.of(new TransferReturn(_freshRoute, factory.one(), false));
    assertEquals(
        TransferBDDUtils.weakestPrecondition(
            paths, 0, _tbdd, (post, path) -> path.getOutputRoute().getLocalPref().value(300)),
        factory.zero());

    // disjoin the conditions on multiple paths
    paths =
        ImmutableList.of(
            new TransferReturn(_freshRoute, _freshRoute.getCommunityAtomicPredicates()[1], true),
            new TransferReturn(_freshRoute, _freshRoute.getCommunityAtomicPredicates()[2], true));
    assertEquals(
        TransferBDDUtils.weakestPrecondition(
            paths, 0, _tbdd, (post, path) -> path.getOutputRoute().getLocalPref().value(300)),
        _freshRoute
            .getLocalPref()
            .value(300)
            .and(
                _freshRoute.getCommunityAtomicPredicates()[1].or(
                    _freshRoute.getCommunityAtomicPredicates()[2])));

    // with state updates
    BDDRoute o1 = new BDDRoute(_freshRoute);
    o1.getLocalPref().setValue(300);
    BDDRoute o2 = new BDDRoute(_freshRoute);
    o2.getLocalPref().setValue(500);
    paths =
        ImmutableList.of(
            // this path sets the local pref to 300, so that condition should not be part of the WP
            new TransferReturn(o1, _freshRoute.getCommunityAtomicPredicates()[1], true),
            // this path sets the local pref to 500 so it is not a feasible path for the given
            // postcondition
            new TransferReturn(o2, _freshRoute.getCommunityAtomicPredicates()[2], true));
    assertEquals(
        TransferBDDUtils.weakestPrecondition(
            paths, 0, _tbdd, (post, path) -> path.getOutputRoute().getLocalPref().value(300)),
        _freshRoute.getCommunityAtomicPredicates()[1]);
  }

  @Test
  public void testDeniedRoutes() {
    BDDFactory factory = _tbdd.getFactory();

    // no paths
    List<TransferReturn> paths = ImmutableList.of();
    assertEquals(TransferBDDUtils.deniedRoutes(paths, _tbdd), factory.zero());

    // no denying paths
    paths = ImmutableList.of(new TransferReturn(_freshRoute, factory.one(), true));
    assertEquals(TransferBDDUtils.deniedRoutes(paths, _tbdd), factory.zero());

    // single denying path
    paths = ImmutableList.of(new TransferReturn(_freshRoute, factory.one(), false));
    assertEquals(TransferBDDUtils.deniedRoutes(paths, _tbdd), factory.one());

    // multiple denying paths
    paths =
        ImmutableList.of(
            new TransferReturn(_freshRoute, _freshRoute.getCommunityAtomicPredicates()[1], false),
            new TransferReturn(_freshRoute, _freshRoute.getCommunityAtomicPredicates()[2], false));
    assertEquals(
        TransferBDDUtils.deniedRoutes(paths, _tbdd),
        _freshRoute.getCommunityAtomicPredicates()[1].or(
            _freshRoute.getCommunityAtomicPredicates()[2]));

    // mixed accepting and denying paths - only denying paths should be included
    paths =
        ImmutableList.of(
            new TransferReturn(_freshRoute, _freshRoute.getCommunityAtomicPredicates()[1], true),
            new TransferReturn(_freshRoute, _freshRoute.getCommunityAtomicPredicates()[2], false));
    assertEquals(
        TransferBDDUtils.deniedRoutes(paths, _tbdd), _freshRoute.getCommunityAtomicPredicates()[2]);
  }

  @Test
  public void testMakeRoutePairing() {
    // Test 1: The pairing is the identity function
    BDDRoute route1 = new BDDRoute(_freshRoute);
    BDDPairing pairing1 = TransferBDDUtils.makeRoutePairing(route1, _tbdd);
    BDD bdd1 = route1.getCommunityAtomicPredicates()[0];
    assertEquals(bdd1.veccompose(pairing1), bdd1);

    // Test 2: The pairing is unrelated to the BDD
    BDDRoute route2 = new BDDRoute(_freshRoute);
    BDD[] commAPs = route2.getCommunityAtomicPredicates();
    commAPs[1] = commAPs[0].or(commAPs[1]);
    BDDPairing pairing2 = TransferBDDUtils.makeRoutePairing(route2, _tbdd);
    BDD bdd2 = route2.getLocalPref().support();
    assertEquals(bdd2.veccompose(pairing2), bdd2);

    // Test 3: The pairing affects the BDD
    BDD bdd3 = commAPs[0].and(commAPs[1]);
    assertEquals(bdd3.veccompose(pairing2), commAPs[0]);
  }

  @Test
  public void testWeakestPreconditionForPathBDDConsumption() {
    BDDFactory factory = _tbdd.getFactory();

    // make sure the postcondition is not consumed by the function
    TransferReturn path = new TransferReturn(_freshRoute, factory.one(), true);
    BDD postCond = factory.ithVar(0).and(factory.nithVar(1));
    TransferBDDUtils.weakestPreconditionForPath(path, postCond, (post, p) -> post);
    try {
      var unused = postCond.toString();
    } catch (BDDException e) {
      // the postcondition was consumed
      fail();
    }
  }
}
