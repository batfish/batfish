package org.batfish.minesweeper.bdd;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.minesweeper.ConfigAtomicPredicates;

/**
 * Various utility methods for working with the results of the symbolic routing analysis {@link
 * org.batfish.minesweeper.bdd.TransferBDD}.
 */
public class TransferBDDUtils {

  /**
   * Produces a BDD representing the weakest precondition of (part of) a routing policy, represented
   * by a set of paths that result from symbolic routing analysis, relative to a given
   * postcondition, which is a predicate on routes. Logically, the weakest precondition is the
   * weakest predicate on input routes that ensures that they are permitted by the given set of
   * paths and yield a route that satisfies the given postcondition. This is a standard notion from
   * program verification (see Dijkstra, A Discipline of Programming, 1976).
   *
   * @param paths symbolic representation of the execution paths through a routing policy
   * @param postcondition the postcondition in some form
   * @param tbdd an object containing the state of the symbolic route analysis that produced the
   *     paths
   * @param postconditionToBDD a function that converts the postcondition and a path to a BDD
   *     representing the constraint that the path's output routes satisfy the postcondition; this
   *     function must return a new BDD object, so that we can avoid memory leaks without destroying
   *     or modifying BDDs in the caller's context
   * @return the weakest precondition as a BDD
   */
  public static <T> BDD weakestPrecondition(
      List<TransferReturn> paths,
      T postcondition,
      TransferBDD tbdd,
      BiFunction<T, TransferReturn, BDD> postconditionToBDD) {

    // collect all accepting paths
    Stream<TransferReturn> permits = paths.stream().filter(TransferReturn::getAccepted);
    // compute the weakest precondition for each path
    Stream<BDD> pathWPs =
        permits.map(path -> weakestPreconditionForPath(path, postcondition, postconditionToBDD));

    // return the disjunction of the per-path weakest preconditions
    return tbdd.getFactory().orAllAndFree(pathWPs.toList());
  }

  /**
   * Produces a BDD representing the set of input routes that are denied by the given (part of) a
   * routing policy, represented by a set of paths that result from symbolic routing analysis.
   *
   * @param paths symbolic representation of the execution paths through a routing policy
   * @param tbdd an object containing the state of the symbolic route analysis that produced the
   *     paths
   * @return a BDD representing the denied input routes
   */
  public static BDD deniedRoutes(List<TransferReturn> paths, TransferBDD tbdd) {
    Stream<TransferReturn> denies = paths.stream().filter(p -> !p.getAccepted());

    return tbdd.getFactory().orAll(denies.map(TransferReturn::getInputConstraints).toList());
  }

  /**
   * Creates a pairing from each BDD variable in the symbolic routing analysis to the corresponding
   * BDD in the given symbolic route. This is useful in particular for chaining together the results
   * of the symbolic routing analysis using {@link net.sf.javabdd.BDD#veccompose(BDDPairing)}.
   *
   * @param route the BDDRoute
   * @param tbdd the routing analysis object that produced the given route
   */
  public static BDDPairing makeRoutePairing(BDDRoute route, TransferBDD tbdd) {
    BDDFactory factory = tbdd.getFactory();
    ConfigAtomicPredicates configAPs = tbdd.getConfigAtomicPredicates();

    // create a fresh BDDRoute to pair with the given one
    BDDRoute freshRoute = new BDDRoute(factory, configAPs);
    BDDPairing pairing = factory.makePair();

    route.augmentPairing(freshRoute, pairing);

    return pairing;
  }

  /**
   * Produces a BDD representing the weakest precondition of a single path that results from
   * symbolic routing analysis, relative to a given postcondition, which is a predicate on routes.
   * Logically, the weakest precondition is the weakest predicate on input routes that ensures that
   * they are permitted by the path and yield a route that satisfies the given postcondition.
   *
   * @param path symbolic representation of an execution path through a routing policy
   * @param postcondition the postcondition in some form
   * @param postconditionToBDD a function that converts the postcondition and a path to a BDD
   *     representing the constraint that the path's output routes satisfy the postcondition; this
   *     function must return a new BDD object, so that we can avoid memory leaks without destroying
   *     or modifying BDDs in the caller's context
   * @return the weakest precondition as a BDD
   */
  @VisibleForTesting
  static <T> BDD weakestPreconditionForPath(
      TransferReturn path, T postcondition, BiFunction<T, TransferReturn, BDD> postconditionToBDD) {

    return postconditionToBDD.apply(postcondition, path).andEq(path.getInputConstraints());
  }
}
