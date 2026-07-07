package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.minesweeper.ConfigAtomicPredicates;

/**
 * Builds {@code numRoutes} {@link BDDRoute}s of identity-variable formulas over a {@link
 * BDDFactory}, each disjoint from every other's and all mutually interleaved (variable {@code v} of
 * one route immediately followed by its counterpart in every other route), all in a single
 * constructor call. Interleaving matches the pattern {@link
 * org.batfish.common.bdd.BDDPacket#allocatePrimedBDDInteger} uses for primed/unprimed variable
 * pairs, and is required for tractability: pairwise-comparing two routes (e.g. via {@link
 * BDDRoute#equalsRelation}) builds one BDD level per bit-pair, and if a bit and its counterpart sit
 * at opposite ends of the variable order, every level in between must be represented, giving a
 * relation exponential in the route width instead of linear.
 *
 * <p>By convention (not a mechanical distinction -- every route this class builds is otherwise
 * interchangeable), {@link #identityRoute} index {@code 0} is the one a caller should also hand to
 * {@link TransferBDD#TransferBDD(BDDFactory, BDDRoute, ConfigAtomicPredicates)} to reuse as that
 * analysis' own canonical route, per that constructor's contract.
 *
 * <p>Every route shares the literal same destination prefix/prefix-length variables rather than
 * each getting a fresh copy: no route policy can ever change a route's destination (there is no
 * {@code setPrefix} on {@link BDDRoute}), so every accepted path's output already agrees with its
 * input on those bits, and relating "two copies, then constrain equal" would just be relating a
 * variable to itself at extra BDD-node cost. Optionally, this class can also reserve a block of
 * {@link #numSharedBits} shared the same way, for caller-defined state that isn't part of {@link
 * BDDRoute} at all (e.g. tracking which snapshot a route's underlying config came from) but that
 * every route should be able to refer to identically. The caller owns what those bits mean -- this
 * class only guarantees indices {@code [0, numSharedBits())} are reserved and untouched by every
 * {@link BDDRoute}, and builds nothing over them itself.
 *
 * <p>Building every route together, in one call, matters because interleaving requires that each
 * new route be built while {@code factory} holds only the previously-built routes from this same
 * call and nothing else -- building them as separate steps would leave a window in which that
 * precondition could be silently violated (e.g. by other code allocating factory variables in
 * between, or by building a route twice).
 */
public final class BDDRouteFactory {
  private final BDDFactory _factory;
  private final ImmutableList<BDDRoute> _routes;
  private final int _numSharedBits;

  /**
   * Like {@link #BDDRouteFactory(BDDFactory, ConfigAtomicPredicates, int, int)}, with no shared
   * bits.
   */
  public BDDRouteFactory(ConfigAtomicPredicates aps, int numRoutes) {
    this(aps, numRoutes, 0);
  }

  /**
   * Like {@link #BDDRouteFactory(BDDFactory, ConfigAtomicPredicates, int, int)}, in a fresh
   * factory.
   */
  public BDDRouteFactory(ConfigAtomicPredicates aps, int numRoutes, int numSharedBits) {
    this(JFactory.init(100000, 10000), aps, numRoutes, numSharedBits);
  }

  /**
   * Like {@link #BDDRouteFactory(BDDFactory, ConfigAtomicPredicates, int, int)}, with no shared
   * bits.
   */
  public BDDRouteFactory(BDDFactory factory, ConfigAtomicPredicates aps, int numRoutes) {
    this(factory, aps, numRoutes, 0);
  }

  /**
   * Builds {@code numRoutes} mutually-interleaved {@link BDDRoute}s of identity-variable formulas,
   * plus (at the very front, before even the prefix/prefix-length variables) {@code numSharedBits}
   * reserved bits for the caller's own use -- see {@link #numSharedBits}.
   *
   * @param factory must not yet hold any {@link BDDRoute} variables.
   */
  public BDDRouteFactory(
      BDDFactory factory, ConfigAtomicPredicates aps, int numRoutes, int numSharedBits) {
    checkArgument(numRoutes >= 1, "numRoutes must be at least 1, got %s", numRoutes);
    _factory = factory;
    // The shared bits, if any, go at the very front (indices [0, numSharedBits)), ahead of even
    // the prefix/prefix-length variables -- see numSharedBits()'s doc. Reserving them first, before
    // building any BDDRoute, means prefixStartIdx below is fixed once and for all for this
    // factory's lifetime.
    if (numSharedBits > 0) {
      factory.setVarNum(numSharedBits);
    }
    _numSharedBits = numSharedBits;
    int prefixStartIdx = numSharedBits;
    _routes = makeRoutes(factory, aps, numRoutes, prefixStartIdx);
  }

  private static final int PREFIX_WIDTH = BDDRoute.PREFIX_LENGTH_BITS + BDDRoute.PREFIX_BITS;

  public BDDFactory getFactory() {
    return _factory;
  }

  /**
   * The {@code i}-th of the {@code numRoutes} mutually-interleaved identity-variable {@link
   * BDDRoute}s requested via the constructor -- see {@link BDDRoute#touchedFields} for what an
   * identity-variable route means. This is the live, shared-by-reference route, not a copy; callers
   * must treat it as read-only -- mutate a {@link #mutableRoute} of it instead, since setting
   * fields directly would corrupt every other holder's view of the identity route.
   */
  public BDDRoute identityRoute(int i) {
    return _routes.get(i);
  }

  /** Every identity route requested via the constructor's {@code numRoutes}, in order. */
  public ImmutableList<BDDRoute> identityRoutes() {
    return _routes;
  }

  /**
   * A fresh {@link BDDRoute#deepCopy} of {@link #identityRoute}, safe for the caller to mutate
   * (e.g. via its {@code setXxx} methods) without affecting the shared identity route or any other
   * caller's copy of it.
   */
  public BDDRoute mutableRoute(int i) {
    return _routes.get(i).deepCopy();
  }

  /**
   * The number of BDD variables reserved at indices {@code [0, numSharedBits())}, for
   * caller-defined state that isn't part of {@link BDDRoute} at all -- this class only reserves the
   * indices and builds nothing over them. Unlike {@link #identityRoute}, there is only one copy of
   * these variables -- every route refers to the exact same variables, the same way every route
   * shares the same prefix/prefix-length. Fits state that isn't compared or rewritten per-route the
   * way a route attribute is (e.g. which snapshot a route's underlying config came from is a
   * property of the route, not of which symbolic variable route currently represents it) -- the
   * caller is free to wrap {@code getFactory().ithVar(i)} for {@code i} in this range in whatever
   * type fits (a single flag, a {@link org.batfish.common.bdd.MutableBDDInteger}, ...).
   */
  public int numSharedBits() {
    return _numSharedBits;
  }

  /**
   * Builds {@code numRoutes} mutually-interleaved {@link BDDRoute}s, each disjoint from every
   * other's non-prefix variables, all sharing the same prefix/prefix-length variables starting at
   * {@code prefixStartIdx}.
   */
  private static ImmutableList<BDDRoute> makeRoutes(
      BDDFactory factory, ConfigAtomicPredicates aps, int numRoutes, int prefixStartIdx) {
    int fixedWidth = prefixStartIdx + PREFIX_WIDTH;
    int width = BDDRoute.numNonPrefixVars(aps);
    ImmutableList.Builder<BDDRoute> routes = ImmutableList.builderWithExpectedSize(numRoutes);
    for (int r = 0; r < numRoutes; r++) {
      routes.add(makeAt(factory, aps, prefixStartIdx, fixedWidth + r * width));
    }
    interleave(factory, fixedWidth, width, numRoutes);
    return routes.build();
  }

  private static BDDRoute makeAt(
      BDDFactory factory, ConfigAtomicPredicates aps, int prefixStartIdx, int startIdx) {
    return new BDDRoute(
        factory,
        aps.getStandardCommunityAtomicPredicates().getNumAtomicPredicates()
            + aps.getNonStandardCommunityLiterals().size(),
        aps.getAsPathRegexAtomicPredicates().getNumAtomicPredicates(),
        aps.getNextHopInterfaces().size(),
        aps.getPeerAddresses().size(),
        aps.getSourceVrfs().size(),
        aps.getTracks().size(),
        aps.getTunnelEncapsulationAttributes(),
        prefixStartIdx,
        startIdx);
  }

  /**
   * Reorders {@code factory}'s variables so that each of the {@code width} non-prefix variables of
   * every one of the {@code numRoutes} routes (built contiguously at indices {@code [fixedWidth,
   * fixedWidth + numRoutes*width)}) is immediately followed by its counterpart(s) in every other
   * route (preserving each route's relative order otherwise, and leaving the shared prefix/prefix-
   * length variables at the very front, before {@code fixedWidth}, untouched). Needed for
   * tractability whenever a computation relates more than two of the routes at once (see this
   * class's javadoc for why non-adjacent variables blow up).
   */
  private static void interleave(BDDFactory factory, int fixedWidth, int width, int numRoutes) {
    int[] oldOrder = factory.getVarOrder();
    int[] newOrder = new int[fixedWidth + numRoutes * width];
    int pos = 0;
    for (int var : oldOrder) {
      if (var < fixedWidth) {
        newOrder[pos++] = var;
      } else if (var < fixedWidth + width) {
        for (int r = 0; r < numRoutes; r++) {
          newOrder[pos++] = fixedWidth + r * width + (var - fixedWidth);
        }
      }
    }
    factory.setVarOrder(newOrder);
  }
}
