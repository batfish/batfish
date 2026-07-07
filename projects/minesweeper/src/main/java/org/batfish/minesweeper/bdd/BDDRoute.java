package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.minesweeper.bdd.BDDDomain.numBits;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.IDeepCopy;
import org.batfish.minesweeper.OspfType;

/**
 * A collection of attributes describing a route advertisement, used for symbolic route analysis.
 */
public final class BDDRoute implements IDeepCopy<BDDRoute> {

  /*
   * For each bit i of a route announcement we have both a BDD variable vi, which
   * represents the ith bit of the announcement, and a BDD fi (f stands for formula), representing
   * the conditions under which bit i is set to 1. Initially each fi is simply vi. The {@link
   * TransferBDD} class takes a BDDRoute and a route policy and produces a new BDDRoute whose fi
   * BDDs represent all possible output announcements from that route policy and the conditions
   * under which they occur, in terms of the vi variables (which still represent the bits of the
   * original input announcement).
   *
   * Since most fields of the route announcement are integers, for example local preference,
   * there is a {@link BDDInteger} helper class that uses this encoding to represent a
   * symbolic integer, along with integer-specific operations.
   */

  private static List<OspfType> allMetricTypes;

  static {
    allMetricTypes = new ArrayList<>();
    allMetricTypes.add(OspfType.O);
    allMetricTypes.add(OspfType.OIA);
    allMetricTypes.add(OspfType.E1);
    allMetricTypes.add(OspfType.E2);
  }

  // need 6 bits for prefix length because there are 33 possible values, 0 - 32
  static final int PREFIX_LENGTH_BITS = 6;
  static final int PREFIX_BITS = 32;

  private final BDDFactory _factory;

  private MutableBDDInteger _adminDist;

  private Map<Integer, String> _bitNames;

  /**
   * Each community atomic predicate (see class Graph) is allocated both a BDD variable and a BDD,
   * as in the general encoding described above. This array maintains the BDDs. The nth BDD
   * indicates the conditions under which a community matching the nth atomic predicate exists in
   * the route.
   */
  private BDD[] _communityAtomicPredicates;

  /**
   * Unlike the case for communities, where each route contains a set of communities, exactly one
   * AS-path atomic predicate will be true for any concrete route. Therefore, we use a {@link
   * BDDDomain}, which encodes the mutual exclusion constraint implicitly and also uses only a
   * logarithmic number of BDD variables in the number of atomic predicates.
   */
  private BDDDomain<Integer> _asPathRegexAtomicPredicates;

  // for now we only track the cluster list's length, not its contents;
  // that is all that is needed to support matching on the length
  private MutableBDDInteger _clusterListLength;

  private MutableBDDInteger _localPref;

  private MutableBDDInteger _med;

  // we use a BDDInteger to track the constraints on the next-hop IP, but we also track a few
  // additional pieces of information that are needed to properly account for the next-hop:  a
  // "type" that accounts for actions such as when a route map discards the next-hop, and a flag
  // indicating whether the route map explicitly updated the next hop

  private MutableBDDInteger _nextHop;

  public enum NextHopType {
    BGP_PEER_ADDRESS,
    DISCARDED,
    IP,
    SELF
  }

  private NextHopType _nextHopType;

  // was the next-hop explicitly set?
  private boolean _nextHopSet;

  private BDDDomain<OriginType> _originType;

  private BDDDomain<OspfType> _ospfMetric;

  private final MutableBDDInteger _prefix;

  private final MutableBDDInteger _prefixLength;

  /**
   * A sequence of AS numbers that is prepended to the original AS-path. The use of a fully concrete
   * value here is sufficient to accurately represent the effects of a single execution path through
   * a route map, since any single path encounters a fixed set of AS-path prepend statements. Hence
   * this representation is sufficient to support {@link TransferBDD#computePaths}, which produces
   * one BDDRoute per execution path. However, this representation precludes the use of a BDDRoute
   * to accurately represent the effects of multiple execution paths, unless those paths prepend the
   * same exact sequence of ASes to the AS-path.
   */
  private @Nonnull List<Long> _prependedASes;

  private final BDDDomain<RoutingProtocol> _protocolHistory;

  /**
   * Represents the route's optional next-hop interface name. See {@link
   * org.batfish.datamodel.routing_policy.expr.MatchInterface}.
   */
  private final BDDDomain<Integer> _nextHopInterfaces;

  /**
   * Represents the address of the peer in the current BGP session. See {@link
   * org.batfish.datamodel.routing_policy.expr.MatchPeerAddress}.
   */
  private final BDDDomain<Integer> _peerAddress;

  /**
   * Represents the optional source VRF from which the route was sent. See {@link
   * org.batfish.datamodel.routing_policy.expr.MatchSourceVrf}.
   */
  private final BDDDomain<Integer> _sourceVrfs;

  /**
   * Represents the optional {@link org.batfish.datamodel.bgp.TunnelEncapsulationAttribute} on the
   * route.
   */
  private @Nonnull BDDTunnelEncapsulationAttribute _tunnelEncapsulationAttribute;

  private MutableBDDInteger _tag;

  /**
   * Contains a BDD variable for each "track" that may be encountered along the path. See {@link
   * org.batfish.datamodel.routing_policy.expr.TrackSucceeded}.
   */
  private BDD[] _tracks;

  private MutableBDDInteger _weight;

  // whether the analysis encountered an unsupported route-policy
  // statement/expression
  private boolean _unsupported;

  /**
   * Every {@link Field} of this route: every {@link Attribute} plus every community/track cell.
   * Fixed once and for all at construction, since {@link #_communityAtomicPredicates}/{@link
   * #_tracks}' lengths never change afterward.
   */
  private final ImmutableSet<Field> _allFields;

  /**
   * The routing protocols allowed in a BGP route announcement (see {@link
   * org.batfish.datamodel.BgpRoute}).
   */
  public static final Set<RoutingProtocol> ALL_BGP_PROTOCOLS =
      ImmutableSet.of(RoutingProtocol.AGGREGATE, RoutingProtocol.BGP, RoutingProtocol.IBGP);

  /**
   * The number of non-prefix BDD variables a {@link BDDRoute} for {@code aps} needs -- i.e. how
   * many contiguous indices {@code startIdx} must leave room for in the constructor below. Does NOT
   * include the shared prefix/prefix-length block, since that is never per-route (see {@link
   * BDDRouteFactory}).
   */
  static int numNonPrefixVars(ConfigAtomicPredicates aps) {
    return numNonPrefixVars(
        aps.getStandardCommunityAtomicPredicates().getNumAtomicPredicates()
            + aps.getNonStandardCommunityLiterals().size(),
        aps.getAsPathRegexAtomicPredicates().getNumAtomicPredicates(),
        aps.getNextHopInterfaces().size(),
        aps.getPeerAddresses().size(),
        aps.getSourceVrfs().size(),
        aps.getTracks().size(),
        aps.getTunnelEncapsulationAttributes());
  }

  private static int numNonPrefixVars(
      int numCommAtomicPredicates,
      int numAsPathRegexAtomicPredicates,
      int numNextHopInterfaces,
      int numPeerAddresses,
      int numSourceVrfs,
      int numTracks,
      List<TunnelEncapsulationAttribute> tunnelEncapsulationAttributes) {
    int bitsToRepresentAdmin =
        LongMath.log2(AbstractRoute.MAX_ADMIN_DISTANCE, RoundingMode.CEILING);
    // or else we need to do tricks in the BDDInteger.
    assert LongMath.isPowerOfTwo(1L + AbstractRoute.MAX_ADMIN_DISTANCE);
    return
    // med, nextHop, tag, localPref, clusterListLength (32 bits each; prefix is separate,
    // allocated/re-derived at prefixStartIdx instead)
    32 * 5
        + 16
        + bitsToRepresentAdmin
        + numCommAtomicPredicates
        + numBits(numAsPathRegexAtomicPredicates)
        // we track one extra value for the next-hop interfaces and source VRFs, to represent
        // "none", since these are optional parts of a route
        + numBits(numNextHopInterfaces + 1)
        + numBits(numSourceVrfs + 1)
        // we track one extra value for the peer address to represent the case where the
        // address is something other than the ones that are specifically matched upon in
        // route policies
        + numBits(numPeerAddresses + 1)
        + numTracks
        + BDDTunnelEncapsulationAttribute.numBitsFor(tunnelEncapsulationAttributes)
        + numBits(OriginType.values().length)
        + numBits(RoutingProtocol.values().length)
        + numBits(allMetricTypes.size());
  }

  /**
   * A constructor that obtains the number of atomic predicates for community and AS-path regexes
   * from a given {@link ConfigAtomicPredicates} object.
   */
  public BDDRoute(BDDFactory factory, ConfigAtomicPredicates aps) {
    this(
        factory,
        aps.getStandardCommunityAtomicPredicates().getNumAtomicPredicates()
            + aps.getNonStandardCommunityLiterals().size(),
        aps.getAsPathRegexAtomicPredicates().getNumAtomicPredicates(),
        aps.getNextHopInterfaces().size(),
        aps.getPeerAddresses().size(),
        aps.getSourceVrfs().size(),
        aps.getTracks().size(),
        aps.getTunnelEncapsulationAttributes());
  }

  /**
   * Creates a collection of BDD variables representing the various attributes of a control plane
   * advertisement. Each atomic predicate created to represent community regexes will be allocated a
   * BDD variable and a BDD, and similarly for the atomic predicates for AS-path regexes, so the
   * number of such atomic predicates is provided.
   */
  @VisibleForTesting
  BDDRoute(
      BDDFactory factory,
      int numCommAtomicPredicates,
      int numAsPathRegexAtomicPredicates,
      int numNextHopInterfaces,
      int numPeerAddresses,
      int numSourceVrfs,
      int numTracks,
      List<TunnelEncapsulationAttribute> tunnelEncapsulationAttributes) {
    this(
        factory,
        numCommAtomicPredicates,
        numAsPathRegexAtomicPredicates,
        numNextHopInterfaces,
        numPeerAddresses,
        numSourceVrfs,
        numTracks,
        tunnelEncapsulationAttributes,
        /* prefixStartIdx= */ 0,
        /* startIdx= */ PREFIX_LENGTH_BITS + PREFIX_BITS);
  }

  /**
   * Like the constructor above, but the destination prefix/prefix-length are allocated starting at
   * BDD variable index {@code prefixStartIdx}, and the "other" (non-prefix) attribute formulas at
   * {@code startIdx}, rather than both being implicitly at index 0. Every {@link BDDRoute} built in
   * the same factory must be given the same {@code prefixStartIdx} (whether freshly allocating
   * those variables there, for the very first {@link BDDRoute} built in a factory, or re-deriving
   * the ones already allocated there by an earlier one), so every {@link BDDRoute} sharing a
   * factory shares the literal same prefix/prefix-length variables: no route policy can ever change
   * a route's destination, so there is nothing to distinguish per block. Used by {@link
   * BDDRouteFactory} to build additional, disjoint (except for prefix/prefix-length) routes in the
   * same factory, and optionally to offset {@code prefixStartIdx} itself past a block of
   * caller-defined shared bits allocated before it.
   */
  BDDRoute(
      BDDFactory factory,
      int numCommAtomicPredicates,
      int numAsPathRegexAtomicPredicates,
      int numNextHopInterfaces,
      int numPeerAddresses,
      int numSourceVrfs,
      int numTracks,
      List<TunnelEncapsulationAttribute> tunnelEncapsulationAttributes,
      int prefixStartIdx,
      int startIdx) {
    checkArgument(prefixStartIdx >= 0, "prefixStartIdx must be non-negative");
    checkArgument(
        startIdx >= prefixStartIdx + PREFIX_LENGTH_BITS + PREFIX_BITS,
        "startIdx must be at or past the end of the prefix/prefix-length block");
    _factory = factory;

    int bitsToRepresentAdmin =
        LongMath.log2(AbstractRoute.MAX_ADMIN_DISTANCE, RoundingMode.CEILING);
    // or else we need to do tricks in the BDDInteger.
    assert LongMath.isPowerOfTwo(1L + AbstractRoute.MAX_ADMIN_DISTANCE);
    // startIdx is always at or past prefixStartIdx + PREFIX_LENGTH_BITS + PREFIX_BITS (every
    // caller places the OTHER fields after the prefix/prefix-length block), so sizing off startIdx
    // alone already accounts for the prefix/prefix-length variables too.
    int numNeeded =
        startIdx
            + numNonPrefixVars(
                numCommAtomicPredicates,
                numAsPathRegexAtomicPredicates,
                numNextHopInterfaces,
                numPeerAddresses,
                numSourceVrfs,
                numTracks,
                tunnelEncapsulationAttributes);
    if (factory.varNum() < numNeeded) {
      factory.setVarNum(numNeeded);
    }
    _bitNames = new HashMap<>();

    // No route policy can ever change a route's destination (there is no setPrefix on this
    // class), so every BDDRoute built in this factory is given the SAME prefixStartIdx, whether
    // this is the very first one (freshly allocating the variables there) or a later one
    // re-deriving them -- either way this is the same call, just possibly a repeat of an earlier
    // one at that index. See BDDRouteFactory. startIdx (where the OTHER fields begin) is separate
    // and always its own fresh allocation.
    _prefixLength =
        MutableBDDInteger.makeFromIndex(factory, PREFIX_LENGTH_BITS, prefixStartIdx, true);
    addBitNames("pfxLen", PREFIX_LENGTH_BITS, prefixStartIdx, true);
    _prefix =
        MutableBDDInteger.makeFromIndex(
            factory, PREFIX_BITS, prefixStartIdx + PREFIX_LENGTH_BITS, true);
    addBitNames("pfx", PREFIX_BITS, prefixStartIdx + PREFIX_LENGTH_BITS, true);
    int idx = startIdx;
    // Initialize one BDD per community atomic predicate, each of which has a corresponding
    // BDD variable
    _communityAtomicPredicates = new BDD[numCommAtomicPredicates];
    for (int i = 0; i < numCommAtomicPredicates; i++) {
      _communityAtomicPredicates[i] = factory.ithVar(idx);
      _bitNames.put(idx, "community atomic predicate " + i);
      idx++;
    }
    _protocolHistory =
        new BDDDomain<>(factory, ImmutableList.copyOf(RoutingProtocol.values()), idx);
    int len = _protocolHistory.getInteger().size();
    addBitNames("proto", len, idx, false);
    idx += len;
    _originType = new BDDDomain<>(factory, ImmutableList.copyOf(OriginType.values()), idx);
    len = _originType.getInteger().size();
    addBitNames("origin", len, idx, false);
    idx += len;
    // Initialize integer values
    _med = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("med", 32, idx, false);
    idx += 32;
    _nextHop = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("nextHop", 32, idx, false);
    idx += 32;
    _nextHopSet = false;
    _nextHopType = NextHopType.IP;
    _tag = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("tag", 32, idx, false);
    idx += 32;
    _weight = MutableBDDInteger.makeFromIndex(factory, 16, idx, false);
    addBitNames("weight", 16, idx, false);
    idx += 16;
    _adminDist = MutableBDDInteger.makeFromIndex(factory, bitsToRepresentAdmin, idx, false);
    addBitNames("ad", bitsToRepresentAdmin, idx, false);
    idx += bitsToRepresentAdmin;
    _localPref = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("lp", 32, idx, false);
    idx += 32;
    _clusterListLength = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("clusterListLength", 32, idx, false);
    idx += 32;

    _asPathRegexAtomicPredicates =
        new BDDDomain<>(
            factory,
            IntStream.range(0, numAsPathRegexAtomicPredicates).boxed().collect(Collectors.toList()),
            idx);
    len = _asPathRegexAtomicPredicates.getInteger().size();
    addBitNames("as-path atomic predicates", len, idx, false);
    idx += len;

    // we use the value -1 below to denote that an optional value is not there, or (in the case of
    // peer addresses) that the address is something other than one of the ones being tracked
    _nextHopInterfaces =
        new BDDDomain<>(
            factory,
            IntStream.range(-1, numNextHopInterfaces).boxed().collect(Collectors.toList()),
            idx);
    len = _nextHopInterfaces.getInteger().size();
    addBitNames("next-hop interfaces", len, idx, false);
    idx += len;
    _sourceVrfs =
        new BDDDomain<>(
            factory, IntStream.range(-1, numSourceVrfs).boxed().collect(Collectors.toList()), idx);
    len = _sourceVrfs.getInteger().size();
    addBitNames("source VRFs", len, idx, false);
    idx += len;

    _peerAddress =
        new BDDDomain<>(
            factory,
            IntStream.range(-1, numPeerAddresses).boxed().collect(Collectors.toList()),
            idx);
    len = _peerAddress.getInteger().size();
    addBitNames("peer address", len, idx, false);
    idx += len;

    // Initialize one BDD per tracked name, each of which has a corresponding BDD variable
    _tracks = new BDD[numTracks];
    for (int i = 0; i < numTracks; i++) {
      _tracks[i] = factory.ithVar(idx);
      _bitNames.put(idx, "track " + i);
      idx++;
    }

    // Tunnel encapsulation attribute is an optional singleton
    _tunnelEncapsulationAttribute =
        BDDTunnelEncapsulationAttribute.create(factory, idx, tunnelEncapsulationAttributes);
    len = _tunnelEncapsulationAttribute.getNumBits();
    addBitNames("tunnel encapsulation attributes", len, idx, false);
    idx += len;

    // Initialize OSPF type
    _ospfMetric = new BDDDomain<>(factory, allMetricTypes, idx);
    len = _ospfMetric.getInteger().size();
    addBitNames("ospfMetric", len, idx, false);
    idx += len;

    _prependedASes = new ArrayList<>();

    // Initially there are no unsupported statements encountered
    _unsupported = false;

    _allFields = computeAllFields(numCommAtomicPredicates, numTracks);

    assert idx != 0; // unnecessary, but needed to avoid unused comment
  }

  /** Every {@link Field} of a route with this many community/track cells. */
  private static ImmutableSet<Field> computeAllFields(int numCommAtomicPredicates, int numTracks) {
    ImmutableSet.Builder<Field> fields = ImmutableSet.builder();
    fields.add(Attribute.values());
    for (int i = 0; i < numCommAtomicPredicates; i++) {
      fields.add(community(i));
    }
    for (int i = 0; i < numTracks; i++) {
      fields.add(track(i));
    }
    return fields.build();
  }

  /** A copy constructor. Every field of the copy is independently owned. */
  public BDDRoute(BDDRoute other) {
    _factory = other._factory;

    _asPathRegexAtomicPredicates = new BDDDomain<>(other._asPathRegexAtomicPredicates);
    _clusterListLength = new MutableBDDInteger(other._clusterListLength);
    _communityAtomicPredicates = new BDD[other._communityAtomicPredicates.length];
    for (int i = 0; i < _communityAtomicPredicates.length; i++) {
      _communityAtomicPredicates[i] = other._communityAtomicPredicates[i].id();
    }
    _prefixLength = new MutableBDDInteger(other._prefixLength);
    _prefix = new MutableBDDInteger(other._prefix);
    _nextHop = new MutableBDDInteger(other._nextHop);
    _nextHopSet = other._nextHopSet;
    _nextHopType = other._nextHopType;
    _adminDist = new MutableBDDInteger(other._adminDist);
    _med = new MutableBDDInteger(other._med);
    _tag = new MutableBDDInteger(other._tag);
    _weight = new MutableBDDInteger(other._weight);
    _localPref = new MutableBDDInteger(other._localPref);
    _protocolHistory = new BDDDomain<>(other._protocolHistory);
    _originType = new BDDDomain<>(other._originType);
    _ospfMetric = new BDDDomain<>(other._ospfMetric);
    _bitNames = other._bitNames;
    _prependedASes = new ArrayList<>(other._prependedASes);
    _nextHopInterfaces = new BDDDomain<>(other._nextHopInterfaces);
    _peerAddress = new BDDDomain<>(other._peerAddress);
    _sourceVrfs = new BDDDomain<>(other._sourceVrfs);
    _tracks = new BDD[other._tracks.length];
    for (int i = 0; i < _tracks.length; i++) {
      _tracks[i] = other._tracks[i].id();
    }
    _tunnelEncapsulationAttribute =
        BDDTunnelEncapsulationAttribute.copyOf(other._tunnelEncapsulationAttribute);
    _unsupported = other._unsupported;
    _allFields = other._allFields;
  }

  /*
   * Constructs a new BDDRoute by restricting the given one to conform to the predicate pred.
   *
   * @param pred the predicate used to restrict the output routes
   * @param route a symbolic route represented as a transformation on a set of routes (input).
   */
  public BDDRoute(BDD pred, BDDRoute route) {
    _factory = route._factory;

    // Create a fresh array for community atomic predicates
    BDD[] communityAtomicPredicates = new BDD[route._communityAtomicPredicates.length];
    // Intersect each atomic predicate with pred.
    for (int i = 0; i < communityAtomicPredicates.length; i++) {
      communityAtomicPredicates[i] = route._communityAtomicPredicates[i].and(pred);
    }

    _asPathRegexAtomicPredicates = new BDDDomain<>(pred, route._asPathRegexAtomicPredicates);
    _clusterListLength = route.getClusterListLength().and(pred);
    _communityAtomicPredicates = communityAtomicPredicates;
    _prefixLength = route._prefixLength.and(pred);
    _prefix = route.getPrefix().and(pred);
    _nextHop = route.getNextHop().and(pred);
    _adminDist = route.getAdminDist().and(pred);
    _med = route.getMed().and(pred);
    _tag = route.getTag().and(pred);
    _localPref = route.getLocalPref().and(pred);
    _weight = route.getWeight().and(pred);
    _protocolHistory = new BDDDomain<>(pred, route.getProtocolHistory());
    _originType = new BDDDomain<>(pred, route.getOriginType());
    _ospfMetric = new BDDDomain<>(pred, route.getOspfMetric());
    _bitNames = route._bitNames;
    _nextHopSet = route.getNextHopSet();
    _nextHopType = route.getNextHopType();
    _unsupported = route.getUnsupported();
    _prependedASes = new ArrayList<>(route.getPrependedASes());
    _nextHopInterfaces = new BDDDomain<>(pred, route._nextHopInterfaces);
    _peerAddress = new BDDDomain<>(pred, route._peerAddress);
    _sourceVrfs = new BDDDomain<>(pred, route._sourceVrfs);
    _tracks = route.getTracks();
    _tunnelEncapsulationAttribute = route._tunnelEncapsulationAttribute.and(pred);
    _allFields = route._allFields;
  }

  /**
   * Constructs a new BDDRoute by composing every attribute formula of the given route through the
   * given pairing (see {@link BDD#veccompose(BDDPairing)}). Used to functionally compose two
   * symbolic routes; see {@link #veccompose(BDDPairing)}. Concrete (non-BDD) state is carried over
   * unchanged.
   *
   * @param pairing a pairing from input variables to their replacement formulas
   * @param route the symbolic route to compose
   */
  public BDDRoute(BDDPairing pairing, BDDRoute route) {
    _factory = route._factory;

    BDD[] communityAtomicPredicates = new BDD[route._communityAtomicPredicates.length];
    for (int i = 0; i < communityAtomicPredicates.length; i++) {
      communityAtomicPredicates[i] = route._communityAtomicPredicates[i].veccompose(pairing);
    }
    _communityAtomicPredicates = communityAtomicPredicates;
    _asPathRegexAtomicPredicates = route._asPathRegexAtomicPredicates.veccompose(pairing);
    _clusterListLength = route._clusterListLength.veccompose(pairing);
    _prefixLength = route._prefixLength.veccompose(pairing);
    _prefix = route._prefix.veccompose(pairing);
    _nextHop = route._nextHop.veccompose(pairing);
    _adminDist = route._adminDist.veccompose(pairing);
    _med = route._med.veccompose(pairing);
    _tag = route._tag.veccompose(pairing);
    _localPref = route._localPref.veccompose(pairing);
    _weight = route._weight.veccompose(pairing);
    _protocolHistory = route._protocolHistory.veccompose(pairing);
    _originType = route._originType.veccompose(pairing);
    _ospfMetric = route._ospfMetric.veccompose(pairing);
    _nextHopInterfaces = route._nextHopInterfaces.veccompose(pairing);
    _peerAddress = route._peerAddress.veccompose(pairing);
    _sourceVrfs = route._sourceVrfs.veccompose(pairing);
    _tunnelEncapsulationAttribute = route._tunnelEncapsulationAttribute.veccompose(pairing);
    BDD[] tracks = new BDD[route._tracks.length];
    for (int i = 0; i < tracks.length; i++) {
      tracks[i] = route._tracks[i].veccompose(pairing);
    }
    _tracks = tracks;
    _bitNames = route._bitNames;
    _nextHopSet = route._nextHopSet;
    _nextHopType = route._nextHopType;
    _unsupported = route._unsupported;
    _prependedASes = new ArrayList<>(route._prependedASes);
    _allFields = route._allFields;
  }

  /*
   * Helper function that builds a map from BDD variable index
   * to some more meaningful name. Helpful for debugging.
   */
  private void addBitNames(String s, int length, int index, boolean reverse) {
    for (int i = index; i < index + length; i++) {
      if (reverse) {
        _bitNames.put(i, s + (length - 1 - (i - index)));
      } else {
        _bitNames.put(i, s + (i - index + 1));
      }
    }
  }

  /*
   * Convenience method for the copy constructor
   */
  @Override
  public BDDRoute deepCopy() {
    return new BDDRoute(this);
  }

  /**
   * Create a BDD representing the constraint that the value of a specific enum attribute in the
   * route announcement's protocol is a member of the given set.
   *
   * @param elements the set of elements that are allowed
   * @param bddDomain the attribute to be constrained
   * @return the BDD representing this constraint
   */
  public <T> BDD anyElementOf(Set<T> elements, BDDDomain<T> bddDomain) {
    return _factory.orAll(elements.stream().map(bddDomain::value).collect(Collectors.toList()));
  }

  /**
   * Not all assignments to the BDD variables that make up a BDDRoute represent valid routes. This
   * method produces constraints that well-formed routes must satisfy, represented as a BDD. It is
   * useful when the goal is to produce concrete example routes from a BDDRoute, for instance.
   *
   * <p>Note that it does not suffice to enforce these constraints as part of symbolic route
   * analysis (see {@link TransferBDD}). That analysis computes a BDD representing the input routes
   * that are accepted by a given route map. Conjoining the well-formedness constraints with that
   * BDD would ensure that all models are feasible routes. But if a client of the analysis is
   * instead interested in routes that are denied by the route map, then negating that BDD and
   * obtaining models would be incorrect, because it would not ensure that the well-formedness
   * constraints hold.
   *
   * @param onlyBGPRoutes whether to constrain the input routes to be BGP routes
   * @return the constraints
   */
  public BDD wellFormednessConstraints(boolean onlyBGPRoutes) {

    // the prefix length should be 32 or less
    BDD prefLenConstraint = _prefixLength.leq(32);
    // the next hop should be neither the min nor the max possible IP
    // this constraint is enforced by NextHopIp's constructor
    BDD nextHopConstraint = _nextHop.range(Ip.ZERO.asLong() + 1, Ip.MAX.asLong() - 1);
    // The various BDD Domains should all lie in the domain (needed if the domain is not power-of-2
    // in size).
    BDD asPathRegexValid = _asPathRegexAtomicPredicates.getIsValidConstraint();
    BDD nextHopInterfacesValid = _nextHopInterfaces.getIsValidConstraint();
    BDD originTypeValid = _originType.getIsValidConstraint();
    BDD ospfMetricValid = _ospfMetric.getIsValidConstraint();
    BDD protocolConstraint =
        onlyBGPRoutes ? anyElementOf(ALL_BGP_PROTOCOLS, this.getProtocolHistory()) : _factory.one();
    BDD peerAddressValid = _peerAddress.getIsValidConstraint();
    BDD sourceVrfValid = _sourceVrfs.getIsValidConstraint();
    BDD tunnelEncapValid = _tunnelEncapsulationAttribute.getIsValidConstraint();
    return _factory.andAllAndFree(
        prefLenConstraint,
        nextHopConstraint,
        asPathRegexValid,
        nextHopInterfacesValid,
        originTypeValid,
        ospfMetricValid,
        protocolConstraint,
        peerAddressValid,
        sourceVrfValid,
        tunnelEncapValid);
  }

  /**
   * Augments a given pairing to pair corresponding BDDs from the given BDDRoute with this one. The
   * BDDs in the given BDDRoute must all be variables -- i.e. {@code identity} must be a route of
   * pure identity-variable formulas (see {@link #touchedFields}), not an arbitrary route.
   *
   * @param identity the BDDRoute of variables
   * @param pairing the existing pairing
   */
  public void augmentPairing(BDDRoute identity, BDDPairing pairing) {
    _asPathRegexAtomicPredicates.augmentPairing(identity._asPathRegexAtomicPredicates, pairing);
    _clusterListLength.augmentPairing(identity._clusterListLength, pairing);
    pairing.set(identity._communityAtomicPredicates, _communityAtomicPredicates);
    _prefixLength.augmentPairing(identity._prefixLength, pairing);
    _prefix.augmentPairing(identity._prefix, pairing);
    _nextHop.augmentPairing(identity._nextHop, pairing);
    _adminDist.augmentPairing(identity._adminDist, pairing);
    _med.augmentPairing(identity._med, pairing);
    _tag.augmentPairing(identity._tag, pairing);
    _weight.augmentPairing(identity._weight, pairing);
    _localPref.augmentPairing(identity._localPref, pairing);
    _protocolHistory.augmentPairing(identity._protocolHistory, pairing);
    _originType.augmentPairing(identity._originType, pairing);
    _ospfMetric.augmentPairing(identity._ospfMetric, pairing);
    _nextHopInterfaces.augmentPairing(identity._nextHopInterfaces, pairing);
    _peerAddress.augmentPairing(identity._peerAddress, pairing);
    _sourceVrfs.augmentPairing(identity._sourceVrfs, pairing);
    pairing.set(identity._tracks, _tracks);
    _tunnelEncapsulationAttribute.augmentPairing(identity._tunnelEncapsulationAttribute, pairing);
  }

  /**
   * Produces a new {@link BDDRoute} by composing every attribute formula through the given pairing
   * (see {@link BDD#veccompose(BDDPairing)}). This route is viewed as a function of some input
   * variables; composing it through a pairing that maps those input variables to the formulas of
   * another symbolic route yields the functional composition of the two -- the route that results
   * from feeding the other route's output as this route's input.
   *
   * <p>The pairing is typically built by {@link TransferBDDUtils#makeRoutePairing} from the route
   * supplying the inputs. Concrete (non-BDD) state -- the prepended-AS list, next-hop type/set
   * flags, and the unsupported flag -- is carried over unchanged; callers that compose chains of
   * routes are responsible for combining those (e.g. concatenating prepended ASes).
   *
   * @param pairing a pairing from input variables to their replacement formulas
   * @return the composed route
   */
  public BDDRoute veccompose(BDDPairing pairing) {
    return new BDDRoute(pairing, this);
  }

  /** The set of BDD variables that appear in any attribute formula of this route. */
  public BDD support() {
    return _factory.andAllAndFree(
        ImmutableList.of(_prefixLength.support(), _prefix.support(), mutableSupport()));
  }

  /**
   * Like {@link #support}, but excludes the destination prefix/prefix-length variables. Since no
   * route policy can ever change a route's destination, every {@link BDDRoute} built alongside a
   * given one shares those variables rather than getting its own copy (see {@link BDDRouteFactory})
   * -- so a caller existentially quantifying a whole block away (e.g. to eliminate an original,
   * pre-image route once a relation over it is built) must use this, not {@link #support}:
   * quantifying out the full support would erase the prefix/prefix-length variables from every
   * other formula that mentions them too, not just this block's copy.
   */
  public BDD mutableSupport() {
    ImmutableList.Builder<BDD> supports = ImmutableList.builder();
    for (Attribute attribute : Attribute.values()) {
      supports.add(attribute.support(this));
    }
    // COMMUNITIES/TRACKS have no Attribute of their own -- each cell is independently addressed
    // via Field.Community/Field.Track instead (see Field's doc) -- so they're covered here
    // per-cell rather than by a single Attribute member.
    Arrays.stream(_communityAtomicPredicates).map(BDD::support).forEach(supports::add);
    Arrays.stream(_tracks).map(BDD::support).forEach(supports::add);
    return _factory.andAllAndFree(supports.build());
  }

  /**
   * Every scalar attribute of a {@link BDDRoute} -- every composable attribute other than
   * prefix/prefix-length (which every {@link BDDRoute} in the same variable domain shares as the
   * literal same BDD variables, see {@link #makeAt}, since no route policy can ever change a
   * route's destination) and other than {@code COMMUNITIES}/{@code TRACKS} (each many independent
   * BDD variables -- one per atomic predicate / tracked name -- addressed instead by {@link
   * Field.Community}/{@link Field.Track}, since e.g. a policy term that adds one community out of
   * many touches only that community's {@link Field}, not the whole array; see {@link Field}'s
   * doc). Used to name which fields a step actually WRITES (see {@link #touchedFields}), so a
   * caller can scope both the relation it builds (see {@link #equalsRelationOn}) and the push that
   * composes it (the exist-set and relabel pairing) to exactly those fields -- a field a step never
   * writes is never mentioned by the relation, never quantified, and never relabeled, so it passes
   * through the incoming route's correlation with the origin completely unconstrained by this step
   * (not equated to anything -- there is nothing to model).
   */
  public enum Attribute implements Field {
    AS_PATH_REGEX {
      @Override
      public BDD support(BDDRoute route) {
        return route._asPathRegexAtomicPredicates.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._asPathRegexAtomicPredicates.augmentPairing(
            identity._asPathRegexAtomicPredicates, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route
            ._asPathRegexAtomicPredicates
            .getInteger()
            .eq(other._asPathRegexAtomicPredicates.getInteger());
      }
    },
    CLUSTER_LIST_LENGTH {
      @Override
      public BDD support(BDDRoute route) {
        return route._clusterListLength.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._clusterListLength.augmentPairing(identity._clusterListLength, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._clusterListLength.eq(other._clusterListLength);
      }
    },
    NEXT_HOP {
      @Override
      public BDD support(BDDRoute route) {
        return route._nextHop.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._nextHop.augmentPairing(identity._nextHop, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._nextHop.eq(other._nextHop);
      }
    },
    ADMIN_DIST {
      @Override
      public BDD support(BDDRoute route) {
        return route._adminDist.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._adminDist.augmentPairing(identity._adminDist, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._adminDist.eq(other._adminDist);
      }
    },
    MED {
      @Override
      public BDD support(BDDRoute route) {
        return route._med.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._med.augmentPairing(identity._med, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._med.eq(other._med);
      }
    },
    TAG {
      @Override
      public BDD support(BDDRoute route) {
        return route._tag.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._tag.augmentPairing(identity._tag, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._tag.eq(other._tag);
      }
    },
    WEIGHT {
      @Override
      public BDD support(BDDRoute route) {
        return route._weight.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._weight.augmentPairing(identity._weight, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._weight.eq(other._weight);
      }
    },
    LOCAL_PREF {
      @Override
      public BDD support(BDDRoute route) {
        return route._localPref.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._localPref.augmentPairing(identity._localPref, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._localPref.eq(other._localPref);
      }
    },
    PROTOCOL_HISTORY {
      @Override
      public BDD support(BDDRoute route) {
        return route._protocolHistory.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._protocolHistory.augmentPairing(identity._protocolHistory, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._protocolHistory.eq(other._protocolHistory);
      }
    },
    ORIGIN_TYPE {
      @Override
      public BDD support(BDDRoute route) {
        return route._originType.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._originType.augmentPairing(identity._originType, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._originType.eq(other._originType);
      }
    },
    OSPF_METRIC {
      @Override
      public BDD support(BDDRoute route) {
        return route._ospfMetric.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._ospfMetric.augmentPairing(identity._ospfMetric, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._ospfMetric.eq(other._ospfMetric);
      }
    },
    NEXT_HOP_INTERFACES {
      @Override
      public BDD support(BDDRoute route) {
        return route._nextHopInterfaces.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._nextHopInterfaces.augmentPairing(identity._nextHopInterfaces, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._nextHopInterfaces.eq(other._nextHopInterfaces);
      }
    },
    PEER_ADDRESS {
      @Override
      public BDD support(BDDRoute route) {
        return route._peerAddress.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._peerAddress.augmentPairing(identity._peerAddress, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._peerAddress.eq(other._peerAddress);
      }
    },
    SOURCE_VRFS {
      @Override
      public BDD support(BDDRoute route) {
        return route._sourceVrfs.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._sourceVrfs.augmentPairing(identity._sourceVrfs, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._sourceVrfs.eq(other._sourceVrfs);
      }
    },
    TUNNEL_ENCAPSULATION_ATTRIBUTE {
      @Override
      public BDD support(BDDRoute route) {
        return route._tunnelEncapsulationAttribute.support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        route._tunnelEncapsulationAttribute.augmentPairing(
            identity._tunnelEncapsulationAttribute, pairing);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route
            ._tunnelEncapsulationAttribute
            .allDifferences(other._tunnelEncapsulationAttribute)
            .notEq();
      }
    };
  }

  /**
   * A single independently-touchable/scopable BDD-variable-bearing piece of a {@link BDDRoute}:
   * either a whole {@link Attribute} (every scalar attribute is indivisible -- e.g. {@code MED} is
   * always one 32-bit integer, so writing it always touches all 32 bits together), or one single
   * community atomic predicate / tracked name -- see {@link #community}/{@link #track}.
   *
   * <p>This finer granularity than a whole {@code COMMUNITIES}/{@code TRACKS} attribute matters for
   * a policy like a tag-dispatch export policy where every term does {@code community add SELF}
   * plus one term-specific community: at whole-attribute granularity, every term touches "some
   * community", so every term's relation (and the push it feeds) must constrain/quantify ALL ~30+
   * community atomic predicates, even the ~28 neither this term nor most others ever mention. At
   * per-community granularity, each term touches only the 2-3 {@link Field}s it actually writes, so
   * the relation, the OR across terms, and the push are all narrower by the same factor. {@link
   * Attribute} has no such array to index into, so it implements {@link Field} directly rather than
   * needing an indexed sibling of its own.
   */
  public sealed interface Field permits Attribute, Field.Community, Field.Track {
    /** The support (BDD variable set) of this field on {@code route}. */
    BDD support(BDDRoute route);

    /**
     * Augments {@code pairing} to map {@code identity}'s variable(s) for this field to {@code
     * route}'s corresponding variable(s). {@code identity} must be a route of pure
     * identity-variable formulas -- see {@link #touchedFields}.
     */
    void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing);

    /** The relation {@code route}'s formula for this field equals {@code other}'s. */
    BDD eq(BDDRoute route, BDDRoute other);

    /** The {@code index}-th community atomic predicate. */
    record Community(int index) implements Field {
      @Override
      public BDD support(BDDRoute route) {
        return route._communityAtomicPredicates[index].support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        pairing.set(
            identity._communityAtomicPredicates[index].var(),
            route._communityAtomicPredicates[index]);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._communityAtomicPredicates[index].biimp(
            other._communityAtomicPredicates[index]);
      }
    }

    /** The {@code index}-th tracked name. */
    record Track(int index) implements Field {
      @Override
      public BDD support(BDDRoute route) {
        return route._tracks[index].support();
      }

      @Override
      public void augmentPairing(BDDRoute route, BDDRoute identity, BDDPairing pairing) {
        pairing.set(identity._tracks[index].var(), route._tracks[index]);
      }

      @Override
      public BDD eq(BDDRoute route, BDDRoute other) {
        return route._tracks[index].biimp(other._tracks[index]);
      }
    }
  }

  /** {@link Field.Community} for {@code index}. Convenience constructor. */
  public static Field community(int index) {
    return new Field.Community(index);
  }

  /** {@link Field.Track} for {@code index}. Convenience constructor. */
  public static Field track(int index) {
    return new Field.Track(index);
  }

  /**
   * The support (BDD variable set) of a single {@link Field} of this route. Used to build a push's
   * exist-set/relabel-pairing scoped to only the fields a step relation actually writes -- see
   * {@link Field}'s javadoc.
   */
  public BDD fieldSupport(Field field) {
    return field.support(this);
  }

  /** The union of {@link #fieldSupport} over each of {@code fields}. */
  public BDD fieldSupportOn(Set<Field> fields) {
    return _factory.andAllAndFree(fields.stream().map(this::fieldSupport).toList());
  }

  /**
   * Augments {@code pairing} to map {@code identity}'s variable(s) for a single {@link Field} to
   * this route's corresponding variable(s). Same contract as {@link #augmentPairing}, scoped to one
   * field -- see {@link Field}'s javadoc.
   */
  public void augmentPairingOn(BDDRoute identity, Field field, BDDPairing pairing) {
    field.augmentPairing(this, identity, pairing);
  }

  /**
   * The fields on which this route's formulas differ from {@code identity}'s -- i.e. the fields a
   * step's output function actually WRITES, as opposed to leaving as the identity (which a route
   * policy achieves simply by never mentioning/setting that field). See {@link Field}'s javadoc for
   * why this matters: a caller building a step's relation should constrain and push only these
   * fields, leaving every other field of the incoming route to pass through unconstrained -- not
   * equated to itself, just never touched.
   *
   * @param identity a route of pure identity-variable formulas over the same domain as this route
   *     (e.g. a fresh block from {@link BDDRouteFactory}, itself)
   */
  public Set<Field> touchedFields(BDDRoute identity) {
    Set<Field> touched = new HashSet<>();
    if (!_asPathRegexAtomicPredicates.equals(identity._asPathRegexAtomicPredicates)) {
      touched.add(Attribute.AS_PATH_REGEX);
    }
    if (!_clusterListLength.equals(identity._clusterListLength)) {
      touched.add(Attribute.CLUSTER_LIST_LENGTH);
    }
    for (int i = 0; i < _communityAtomicPredicates.length; i++) {
      if (!_communityAtomicPredicates[i].equals(identity._communityAtomicPredicates[i])) {
        touched.add(community(i));
      }
    }
    if (!_nextHop.equals(identity._nextHop)) {
      touched.add(Attribute.NEXT_HOP);
    }
    if (!_adminDist.equals(identity._adminDist)) {
      touched.add(Attribute.ADMIN_DIST);
    }
    if (!_med.equals(identity._med)) {
      touched.add(Attribute.MED);
    }
    if (!_tag.equals(identity._tag)) {
      touched.add(Attribute.TAG);
    }
    if (!_weight.equals(identity._weight)) {
      touched.add(Attribute.WEIGHT);
    }
    if (!_localPref.equals(identity._localPref)) {
      touched.add(Attribute.LOCAL_PREF);
    }
    if (!_protocolHistory.equals(identity._protocolHistory)) {
      touched.add(Attribute.PROTOCOL_HISTORY);
    }
    if (!_originType.equals(identity._originType)) {
      touched.add(Attribute.ORIGIN_TYPE);
    }
    if (!_ospfMetric.equals(identity._ospfMetric)) {
      touched.add(Attribute.OSPF_METRIC);
    }
    if (!_nextHopInterfaces.equals(identity._nextHopInterfaces)) {
      touched.add(Attribute.NEXT_HOP_INTERFACES);
    }
    if (!_peerAddress.equals(identity._peerAddress)) {
      touched.add(Attribute.PEER_ADDRESS);
    }
    if (!_sourceVrfs.equals(identity._sourceVrfs)) {
      touched.add(Attribute.SOURCE_VRFS);
    }
    for (int i = 0; i < _tracks.length; i++) {
      if (!_tracks[i].equals(identity._tracks[i])) {
        touched.add(track(i));
      }
    }
    if (!_tunnelEncapsulationAttribute.equals(identity._tunnelEncapsulationAttribute)) {
      touched.add(Attribute.TUNNEL_ENCAPSULATION_ATTRIBUTE);
    }
    return touched;
  }

  /**
   * Frees every BDD backing this route's attribute formulas. This route must not be used after this
   * call.
   */
  public void free() {
    _asPathRegexAtomicPredicates.free();
    _clusterListLength.free();
    _prefixLength.free();
    _prefix.free();
    for (BDD communityAtomicPredicate : _communityAtomicPredicates) {
      communityAtomicPredicate.free();
    }
    _nextHop.free();
    _adminDist.free();
    _med.free();
    _tag.free();
    _weight.free();
    _localPref.free();
    _protocolHistory.free();
    _originType.free();
    _ospfMetric.free();
    _nextHopInterfaces.free();
    _peerAddress.free();
    _sourceVrfs.free();
    for (BDD track : _tracks) {
      track.free();
    }
    _tunnelEncapsulationAttribute.free();
  }

  /**
   * Produces the relation {@code this == other}: a BDD over the union of this route's and {@code
   * other}'s variables that holds exactly when every attribute formula of this route agrees with
   * the corresponding formula of {@code other}. Combined with a guard and existential
   * quantification, this turns a {@code (BDDRoute, BDD guard)} pair -- a function plus a path
   * condition -- into a flat BDD describing the set of possible outputs (a relational image
   * computation).
   *
   * @param other a route over the same or a disjoint set of variables
   */
  public BDD equalsRelation(BDDRoute other) {
    return equalsRelationOn(other, _allFields);
  }

  /**
   * Like {@link #equalsRelation}, but constrains only {@code fields}; every other field is left
   * completely unmentioned (not merely "unconstrained" via a trivial term -- genuinely absent from
   * the returned BDD's support). Pairing this with a push whose exist-set/relabel pairing are
   * scoped to the same {@code fields} means a field this step doesn't write is never quantified and
   * never relabeled, so it passes through the incoming relation's correlation with the origin
   * completely untouched -- no equality term is needed to model "preserved", because omission from
   * both the relation and the push's scope already achieves it. See {@link Field}'s javadoc.
   */
  public BDD equalsRelationOn(BDDRoute other, Set<Field> fields) {
    return _factory.andAllAndFree(fields.stream().map(f -> f.eq(this, other)).toList());
  }

  /*
   * Converts a BDD to the graphviz DOT format for debugging.
   */
  String dot(BDD bdd) {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph G {\n");
    sb.append("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];\n");
    sb.append("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];\n");
    dotRec(sb, bdd, new HashSet<>());
    sb.append("}");
    return sb.toString();
  }

  /*
   * Creates a unique id for a bdd node when generating
   * a DOT file for graphviz
   */
  private Integer dotId(BDD bdd) {
    if (bdd.isZero()) {
      return 0;
    }
    if (bdd.isOne()) {
      return 1;
    }
    return bdd.hashCode() + 2;
  }

  /*
   * Recursively builds each of the intermediate BDD nodes in the
   * graphviz DOT format.
   */
  private void dotRec(StringBuilder sb, BDD bdd, Set<BDD> visited) {
    if (bdd.isOne() || bdd.isZero() || visited.contains(bdd)) {
      return;
    }
    int val = dotId(bdd);
    int valLow = dotId(bdd.low());
    int valHigh = dotId(bdd.high());
    String name = _bitNames.get(bdd.var());
    sb.append(val).append(" [label=\"").append(name).append("\"]\n");
    sb.append(val).append(" -> ").append(valLow).append("[style=dotted]\n");
    sb.append(val).append(" -> ").append(valHigh).append("[style=filled]\n");
    visited.add(bdd);
    dotRec(sb, bdd.low(), visited);
    dotRec(sb, bdd.high(), visited);
  }

  public MutableBDDInteger getAdminDist() {
    return _adminDist;
  }

  public void setAdminDist(MutableBDDInteger adminDist) {
    _adminDist = adminDist;
  }

  public BDDDomain<Integer> getAsPathRegexAtomicPredicates() {
    return _asPathRegexAtomicPredicates;
  }

  public void setAsPathRegexAtomicPredicates(BDDDomain<Integer> asPathRegexAtomicPredicates) {
    _asPathRegexAtomicPredicates = asPathRegexAtomicPredicates;
  }

  public MutableBDDInteger getClusterListLength() {
    return _clusterListLength;
  }

  public void setClusterListLength(MutableBDDInteger clusterListLength) {
    _clusterListLength = clusterListLength;
  }

  public BDD[] getCommunityAtomicPredicates() {
    return _communityAtomicPredicates;
  }

  public void setCommunityAtomicPredicates(BDD[] communityAtomicPredicates) {
    _communityAtomicPredicates = communityAtomicPredicates;
  }

  public BDDFactory getFactory() {
    return _factory;
  }

  public MutableBDDInteger getLocalPref() {
    return _localPref;
  }

  public void setLocalPref(MutableBDDInteger localPref) {
    _localPref = localPref;
  }

  public MutableBDDInteger getMed() {
    return _med;
  }

  public void setMed(MutableBDDInteger med) {
    _med = med;
  }

  public MutableBDDInteger getNextHop() {
    return _nextHop;
  }

  public void setNextHop(MutableBDDInteger nextHop) {
    _nextHop = nextHop;
  }

  public boolean getNextHopSet() {
    return _nextHopSet;
  }

  public void setNextHopSet(boolean nextHopSet) {
    _nextHopSet = nextHopSet;
  }

  public NextHopType getNextHopType() {
    return _nextHopType;
  }

  public void setNextHopType(NextHopType nextHopType) {
    _nextHopType = nextHopType;
  }

  public BDDDomain<OriginType> getOriginType() {
    return _originType;
  }

  public void setOriginType(BDDDomain<OriginType> originType) {
    _originType = originType;
  }

  public BDDDomain<OspfType> getOspfMetric() {
    return _ospfMetric;
  }

  public void setOspfMetric(BDDDomain<OspfType> ospfMetric) {
    _ospfMetric = ospfMetric;
  }

  public MutableBDDInteger getPrefix() {
    return _prefix;
  }

  public MutableBDDInteger getPrefixLength() {
    return _prefixLength;
  }

  public List<Long> getPrependedASes() {
    return _prependedASes;
  }

  public void setPrependedASes(List<Long> prependedASes) {
    _prependedASes = prependedASes;
  }

  public BDDDomain<RoutingProtocol> getProtocolHistory() {
    return _protocolHistory;
  }

  public MutableBDDInteger getTag() {
    return _tag;
  }

  public void setTag(MutableBDDInteger tag) {
    _tag = tag;
  }

  public void setTunnelEncapsulationAttribute(@Nonnull BDDTunnelEncapsulationAttribute value) {
    _tunnelEncapsulationAttribute = value;
  }

  public BDDDomain<Integer> getNextHopInterfaces() {
    return _nextHopInterfaces;
  }

  public BDDDomain<Integer> getPeerAddress() {
    return _peerAddress;
  }

  public BDDDomain<Integer> getSourceVrfs() {
    return _sourceVrfs;
  }

  public BDD[] getTracks() {
    return _tracks;
  }

  public void setTracks(BDD[] tracks) {
    _tracks = tracks;
  }

  public @Nonnull BDDTunnelEncapsulationAttribute getTunnelEncapsulationAttribute() {
    return _tunnelEncapsulationAttribute;
  }

  public MutableBDDInteger getWeight() {
    return _weight;
  }

  public void setWeight(MutableBDDInteger weight) {
    _weight = weight;
  }

  public boolean getUnsupported() {
    return _unsupported;
  }

  public void setUnsupported(boolean unsupported) {
    _unsupported = unsupported;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof BDDRoute)) {
      return false;
    }
    BDDRoute other = (BDDRoute) o;
    return Objects.equals(_adminDist, other._adminDist)
        && Objects.equals(_ospfMetric, other._ospfMetric)
        && Objects.equals(_originType, other._originType)
        && Objects.equals(_protocolHistory, other._protocolHistory)
        && Objects.equals(_med, other._med)
        && Objects.equals(_localPref, other._localPref)
        && Objects.equals(_clusterListLength, other._clusterListLength)
        && Objects.equals(_tag, other._tag)
        && Objects.equals(_weight, other._weight)
        && Objects.equals(_nextHop, other._nextHop)
        && Objects.equals(_nextHopSet, other._nextHopSet)
        && Objects.equals(_nextHopType, other._nextHopType)
        && Objects.equals(_prefix, other._prefix)
        && Objects.equals(_prefixLength, other._prefixLength)
        && Arrays.equals(_communityAtomicPredicates, other._communityAtomicPredicates)
        && Objects.equals(_asPathRegexAtomicPredicates, other._asPathRegexAtomicPredicates)
        && Objects.equals(_prependedASes, other._prependedASes)
        && Objects.equals(_nextHopInterfaces, other._nextHopInterfaces)
        && Objects.equals(_peerAddress, other._peerAddress)
        && Objects.equals(_sourceVrfs, other._sourceVrfs)
        && Arrays.equals(_tracks, other._tracks)
        && _tunnelEncapsulationAttribute.equals(other._tunnelEncapsulationAttribute)
        && Objects.equals(_unsupported, other._unsupported);
  }

  @Override
  public int hashCode() {
    // Does not include every field, but must include the fields that distinguish routes in
    // practice. The community atomic predicates in particular are included: when a BDDRoute is
    // used as a symbolic route function, the scalar attribute integers (prefix, local-pref, MED,
    // ...) are often left as their free identity functions (the constraint lives in an external
    // guard), so communities are the axis that distinguishes most distinct route functions.
    // Omitting them collapsed distinct functions into a handful of hash buckets, treeifying
    // HashMaps keyed on BDDRoute and turning every insert into a chain of full structural
    // equals() calls.
    return Objects.hash(
        _adminDist,
        _originType,
        _med,
        _localPref,
        _clusterListLength,
        _tag,
        _weight,
        _nextHop,
        _prefix,
        _prefixLength,
        _asPathRegexAtomicPredicates,
        _prependedASes,
        Arrays.hashCode(_communityAtomicPredicates),
        Arrays.hashCode(_tracks));
  }
}
