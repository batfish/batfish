package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.minesweeper.ConfigAtomicPredicates;

/**
 * Builds a {@link BDDFactory}'s canonical {@link BDDRoute} (the {@code O} block of variables)
 * together with any number of additional fresh blocks of non-prefix variables, each disjoint from
 * {@code O}'s and from each other's and all mutually interleaved with it (variable {@code v} of
 * {@code O} immediately followed by its counterpart in every extra block), all in a single
 * constructor call. Interleaving matches the pattern {@link
 * org.batfish.common.bdd.BDDPacket#allocatePrimedBDDInteger} uses for primed/unprimed variable
 * pairs, and is required for tractability: pairwise-comparing two blocks (e.g. via {@link
 * BDDRoute#equalsRelation}) builds one BDD level per bit-pair, and if a bit and its counterpart sit
 * at opposite ends of the variable order, every level in between must be represented, giving a
 * relation exponential in the block width instead of linear.
 *
 * <p>Every extra block shares {@code O}'s destination prefix/prefix-length variables rather than
 * getting a fresh copy: no route policy can ever change a route's destination (there is no {@code
 * setPrefix} on {@link BDDRoute}), so every accepted path's output already agrees with its input on
 * those bits, and relating "two copies, then constrain equal" would just be relating a variable to
 * itself at extra BDD-node cost. Optionally, this class can also reserve a block of {@link
 * #numSharedBits} shared the same way, for caller-defined state that isn't part of {@link BDDRoute}
 * at all (e.g. tracking which snapshot a route's underlying config came from) but that every route
 * block should be able to refer to identically. The caller owns what those bits mean -- this class
 * only guarantees indices {@code [0, numSharedBits())} are reserved and untouched by every {@link
 * BDDRoute} block, and builds nothing over them itself.
 *
 * <p>Building {@code O} and its extra blocks together, in one call, matters because interleaving
 * requires that {@code factory} hold exactly one previously-built {@link BDDRoute} (the one being
 * interleaved with) and nothing else -- building them as separate steps would leave a window in
 * which that precondition could be silently violated (e.g. by other code allocating factory
 * variables in between, or by building the extra blocks twice).
 */
public final class BDDRouteFactory {
  private final BDDFactory _factory;
  private final BDDRoute _originalRoute;
  private final ImmutableList<BDDRoute> _extraBlocks;
  private final int _numSharedBits;

  /**
   * Like {@link #BDDRouteFactory(BDDFactory, ConfigAtomicPredicates, int, int)}, with no shared
   * bits.
   */
  public BDDRouteFactory(ConfigAtomicPredicates aps, int numExtraBlocks) {
    this(aps, numExtraBlocks, 0);
  }

  /**
   * Like {@link #BDDRouteFactory(BDDFactory, ConfigAtomicPredicates, int, int)}, in a fresh
   * factory.
   */
  public BDDRouteFactory(ConfigAtomicPredicates aps, int numExtraBlocks, int numSharedBits) {
    this(JFactory.init(100000, 10000), aps, numExtraBlocks, numSharedBits);
  }

  /**
   * Like {@link #BDDRouteFactory(BDDFactory, ConfigAtomicPredicates, int, int)}, with no shared
   * bits.
   */
  public BDDRouteFactory(BDDFactory factory, ConfigAtomicPredicates aps, int numExtraBlocks) {
    this(factory, aps, numExtraBlocks, 0);
  }

  /**
   * Builds {@code factory}'s canonical {@link BDDRoute} plus {@code numExtraBlocks} additional
   * fresh blocks, all interleaved together, plus (at the very front, before even the
   * prefix/prefix-length variables) {@code numSharedBits} reserved bits for the caller's own use --
   * see {@link #numSharedBits}.
   *
   * @param factory must not yet hold any {@link BDDRoute} variables.
   */
  public BDDRouteFactory(
      BDDFactory factory, ConfigAtomicPredicates aps, int numExtraBlocks, int numSharedBits) {
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
    _originalRoute = makeAt(factory, aps, prefixStartIdx, prefixStartIdx + PREFIX_WIDTH);
    _extraBlocks =
        numExtraBlocks == 0
            ? ImmutableList.of()
            : ImmutableList.copyOf(makeFreshBlocks(factory, aps, numExtraBlocks, prefixStartIdx));
  }

  private static final int PREFIX_WIDTH = BDDRoute.PREFIX_LENGTH_BITS + BDDRoute.PREFIX_BITS;

  public BDDFactory getFactory() {
    return _factory;
  }

  /**
   * The canonical {@link BDDRoute} of input variables (the {@code O} block). A route of pure
   * identity-variable formulas -- see {@link BDDRoute#touchedFields} -- shared by reference with
   * every other {@link BDDRouteFactory} accessor and with {@link TransferBDD#getOriginalRoute} (if
   * built from the same {@code factory}/{@code aps}); callers must treat it as read-only (e.g. via
   * {@link BDDRoute#deepCopy} before mutating) rather than setting fields on it directly, since
   * doing so would corrupt every other holder's view of the identity block.
   */
  public BDDRoute originalRoute() {
    return _originalRoute;
  }

  /**
   * The {@code i}-th extra fresh identity block requested via the constructor's {@code
   * numExtraBlocks}. Same read-only contract as {@link #originalRoute}: this is the live,
   * shared-by-reference block, not a copy -- mutate a {@link BDDRoute#deepCopy} of it instead.
   */
  public BDDRoute identityBlock(int i) {
    return _extraBlocks.get(i);
  }

  /**
   * Every extra fresh identity block requested via the constructor's {@code numExtraBlocks}, in
   * order. Same read-only contract as {@link #identityBlock}.
   */
  public ImmutableList<BDDRoute> identityBlocks() {
    return _extraBlocks;
  }

  /**
   * The number of BDD variables reserved at indices {@code [0, numSharedBits())}, for
   * caller-defined state that isn't part of {@link BDDRoute} at all -- this class only reserves the
   * indices and builds nothing over them. Unlike {@link #block}, there is only one copy of these
   * variables -- every block ({@link #originalRoute} and every extra block) refers to the exact
   * same variables, the same way every block shares {@code O}'s prefix/prefix-length. Fits state
   * that isn't compared or rewritten per-block the way a route attribute is (e.g. which snapshot a
   * route's underlying config came from is a property of the route, not of which symbolic variable
   * block currently represents it) -- the caller is free to wrap {@code getFactory().ithVar(i)} for
   * {@code i} in this range in whatever type fits (a single flag, a {@link
   * org.batfish.common.bdd.MutableBDDInteger}, ...).
   */
  public int numSharedBits() {
    return _numSharedBits;
  }

  /**
   * Builds {@code numBlocks} fresh blocks of non-prefix BDD variables, each disjoint from {@code
   * factory}'s existing non-prefix variables and from each other's, all mutually interleaved with
   * them (but every block sharing the same prefix/prefix-length variables, at {@code
   * prefixStartIdx} -- see this class's javadoc).
   *
   * <p>Precondition: {@code factory} must not yet hold any variables beyond {@code [0,
   * prefixStartIdx + PREFIX_WIDTH)} -- the optional shared bits, then a single, previously
   * constructed {@code BDDRoute} at {@code prefixStartIdx}.
   */
  private static ImmutableList<BDDRoute> makeFreshBlocks(
      BDDFactory factory, ConfigAtomicPredicates aps, int numBlocks, int prefixStartIdx) {
    // Precondition: a single canonical BDDRoute already exists, with prefix/prefixLength at
    // [prefixStartIdx, fixedWidth). Every block built here re-derives those same variables at that
    // fixed index rather than allocating a fresh copy; interleaving/width math below is over the
    // OTHER fields only.
    int fixedWidth = prefixStartIdx + PREFIX_WIDTH;
    int width = factory.varNum() - fixedWidth;
    ImmutableList.Builder<BDDRoute> blocks = ImmutableList.builderWithExpectedSize(numBlocks);
    for (int b = 1; b <= numBlocks; b++) {
      blocks.add(makeAt(factory, aps, prefixStartIdx, fixedWidth + b * width));
    }
    interleaveWithFreshBlocks(factory, fixedWidth, width, numBlocks);
    return blocks.build();
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
   * Reorders {@code factory}'s variables so that each of the {@code width} non-prefix variables
   * starting at {@code fixedWidth} (the pre-existing block) is immediately followed by its
   * counterpart(s) in the {@code numFreshBlocks} fresh blocks of {@code width} variables each that
   * were just appended after it (contiguously, at indices {@code [fixedWidth + width, fixedWidth +
   * (1+numFreshBlocks)*width)}), preserving the pre-existing block's relative order otherwise (and
   * leaving the shared bits and prefix/prefix-length variables at the very front, before {@code
   * fixedWidth}, untouched). Generalizes the pairwise interleave to {@code numFreshBlocks + 1}
   * mutually-adjacent variables per position, needed for tractability whenever a computation
   * relates more than two of the domains at once (see this class's javadoc for why non-adjacent
   * variables blow up).
   */
  private static void interleaveWithFreshBlocks(
      BDDFactory factory, int fixedWidth, int width, int numFreshBlocks) {
    int[] oldOrder = factory.getVarOrder();
    int[] newOrder = new int[fixedWidth + (1 + numFreshBlocks) * width];
    int pos = 0;
    for (int var : oldOrder) {
      if (var < fixedWidth) {
        newOrder[pos++] = var;
      } else if (var < fixedWidth + width) {
        newOrder[pos++] = var;
        for (int b = 1; b <= numFreshBlocks; b++) {
          newOrder[pos++] = fixedWidth + b * width + (var - fixedWidth);
        }
      }
    }
    factory.setVarOrder(newOrder);
  }
}
