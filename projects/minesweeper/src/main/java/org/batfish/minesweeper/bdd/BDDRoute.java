package org.batfish.minesweeper.bdd;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.math.IntMath;
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
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.IDeepCopy;
import org.batfish.minesweeper.OspfType;

/**
 * A collection of attributes describing a route advertisement, used for symbolic route analysis.
 *
 * @author Ryan Beckett
 */
public class BDDRoute implements IDeepCopy<BDDRoute> {

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
   * Each AS-path regex atomic predicate (see class Graph) is allocated both a BDD variable and a
   * BDD, as in the general encoding described above. This array maintains the BDDs. The nth BDD
   * indicates the conditions under which the route's AS path satisfies the nth atomic predicate.
   *
   * <p>Since the AS-path atomic predicates are disjoint, at most one of them can be true in any
   * concrete route. So we could use a binary encoding instead, where we use log(N) BDDs to
   * represent N atomic predicates. See {@link org.batfish.common.bdd.BDDFiniteDomain} for an
   * example of that encoding. If N is large, that could improve efficiency of symbolic route
   * analysis (see {@link TransferBDD}) and of the route well-formedness constraints (see
   * wellFormednessConstraints() below).
   */
  private BDD[] _asPathRegexAtomicPredicates;

  // for now we only track the cluster list's length, not its contents;
  // that is all that is needed to support matching on the length
  private MutableBDDInteger _clusterListLength;

  private MutableBDDInteger _localPref;

  private MutableBDDInteger _med;

  private MutableBDDInteger _nextHop;

  // to properly determine the next-hop IP that results from each path through a given route-map we
  // need to track a few more pieces of information:  whether the next-hop is explicitly discarded
  // by the route-map and whether the next-hop is explicitly set by the route-map
  private boolean _nextHopDiscarded;
  private boolean _nextHopSet;

  private BDDDomain<OriginType> _originType;

  private BDDDomain<OspfType> _ospfMetric;

  private final MutableBDDInteger _prefix;

  private final MutableBDDInteger _prefixLength;

  /**
   * A sequence of AS numbers that is prepended to the original AS-path. The use of a fully concrete
   * value here is sufficient to accurately represent the effects of a single execution path through
   * a route map, since any single path encounters a fixed set of AS-path prepend statements. Hence
   * this representation is sufficient to support {@link TransferBDD#computePaths(Set)}, which
   * produces on BDDRoute per execution path. However, this representation precludes the use of a
   * BDDRoute to accurately represent the effects of multiple execution paths, unless those paths
   * prepend the same exact sequence of ASes to the AS-path.
   */
  @Nonnull private List<Long> _prependedASes;

  private final BDDDomain<RoutingProtocol> _protocolHistory;

  /**
   * Contains a BDD variable for each source VRF that is encountered along the path. See {@link
   * org.batfish.datamodel.routing_policy.expr.MatchSourceVrf}.
   */
  private BDD[] _sourceVrfs;

  private MutableBDDInteger _tag;

  /**
   * Contains a BDD variable for each "track" that is encountered along the path. See {@link
   * org.batfish.datamodel.routing_policy.expr.TrackSucceeded}.
   */
  private BDD[] _tracks;

  private MutableBDDInteger _weight;

  // whether the analysis encountered an unsupported route-policy
  // statement/expression
  private boolean _unsupported;

  /**
   * The routing protocols allowed in a BGP route announcement (see {@link
   * org.batfish.datamodel.BgpRoute}).
   */
  public static final Set<RoutingProtocol> ALL_BGP_PROTOCOLS =
      ImmutableSet.of(RoutingProtocol.AGGREGATE, RoutingProtocol.BGP, RoutingProtocol.IBGP);

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
        aps.getSourceVrfs().size(),
        aps.getTracks().size());
  }

  /**
   * Creates a collection of BDD variables representing the various attributes of a control plane
   * advertisement. Each atomic predicate created to represent community regexes will be allocated a
   * BDD variable and a BDD, and similarly for the atomic predicates for AS-path regexes, so the
   * number of such atomic predicates is provided.
   */
  public BDDRoute(
      BDDFactory factory,
      int numCommAtomicPredicates,
      int numAsPathRegexAtomicPredicates,
      int numSourceVrfs,
      int numTracks) {
    _factory = factory;

    int numVars = factory.varNum();
    int numNeeded =
        32 * 6
            + 16
            + 8
            + 6
            + numCommAtomicPredicates
            + numAsPathRegexAtomicPredicates
            + numSourceVrfs
            + numTracks
            + IntMath.log2(OriginType.values().length, RoundingMode.CEILING)
            + IntMath.log2(RoutingProtocol.values().length, RoundingMode.CEILING)
            + IntMath.log2(allMetricTypes.size(), RoundingMode.CEILING);
    if (numVars < numNeeded) {
      factory.setVarNum(numNeeded);
    }
    _bitNames = new HashMap<>();

    int idx = 0;
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
    _nextHopDiscarded = false;
    _tag = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("tag", 32, idx, false);
    idx += 32;
    _weight = MutableBDDInteger.makeFromIndex(factory, 16, idx, false);
    addBitNames("weight", 16, idx, false);
    idx += 16;
    _adminDist = MutableBDDInteger.makeFromIndex(factory, 8, idx, false);
    addBitNames("ad", 8, idx, false);
    idx += 8;
    _localPref = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("lp", 32, idx, false);
    idx += 32;
    _clusterListLength = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("clusterListLength", 32, idx, false);
    idx += 32;
    // need 6 bits for prefix length because there are 33 possible values, 0 - 32
    _prefixLength = MutableBDDInteger.makeFromIndex(factory, 6, idx, true);
    addBitNames("pfxLen", 6, idx, true);
    idx += 6;
    _prefix = MutableBDDInteger.makeFromIndex(factory, 32, idx, true);
    addBitNames("pfx", 32, idx, true);
    idx += 32;
    // Initialize one BDD per community atomic predicate, each of which has a corresponding
    // BDD variable
    _communityAtomicPredicates = new BDD[numCommAtomicPredicates];
    for (int i = 0; i < numCommAtomicPredicates; i++) {
      _communityAtomicPredicates[i] = factory.ithVar(idx);
      _bitNames.put(idx, "community atomic predicate " + i);
      idx++;
    }
    // Initialize one BDD per AS-path regex atomic predicate, each of which has a corresponding
    // BDD variable
    _asPathRegexAtomicPredicates = new BDD[numAsPathRegexAtomicPredicates];
    for (int i = 0; i < numAsPathRegexAtomicPredicates; i++) {
      _asPathRegexAtomicPredicates[i] = factory.ithVar(idx);
      _bitNames.put(idx, "AS-path regex atomic predicate " + i);
      idx++;
    }
    // Initialize one BDD per source VRF, each of which has a corresponding BDD variable
    _sourceVrfs = new BDD[numSourceVrfs];
    for (int i = 0; i < numSourceVrfs; i++) {
      _sourceVrfs[i] = factory.ithVar(idx);
      _bitNames.put(idx, "source VRF " + i);
      idx++;
    }
    // Initialize one BDD per tracked name, each of which has a corresponding BDD variable
    _tracks = new BDD[numTracks];
    for (int i = 0; i < numTracks; i++) {
      _tracks[i] = factory.ithVar(idx);
      _bitNames.put(idx, "track " + i);
      idx++;
    }
    // Initialize OSPF type
    _ospfMetric = new BDDDomain<>(factory, allMetricTypes, idx);
    len = _ospfMetric.getInteger().size();
    addBitNames("ospfMetric", len, idx, false);
    _prependedASes = new ArrayList<>();
    // Initially there are no unsupported statements encountered
    _unsupported = false;
  }

  /*
   * Create a BDDRecord from another. Because BDDs are immutable,
   * there is no need for a deep copy.
   */
  public BDDRoute(BDDRoute other) {
    _factory = other._factory;

    _asPathRegexAtomicPredicates = other._asPathRegexAtomicPredicates.clone();
    _clusterListLength = new MutableBDDInteger(other._clusterListLength);
    _communityAtomicPredicates = other._communityAtomicPredicates.clone();
    _prefixLength = new MutableBDDInteger(other._prefixLength);
    _prefix = new MutableBDDInteger(other._prefix);
    _nextHop = new MutableBDDInteger(other._nextHop);
    _nextHopSet = other._nextHopSet;
    _nextHopDiscarded = other._nextHopDiscarded;
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
    _sourceVrfs = other._sourceVrfs.clone();
    _tracks = other._tracks.clone();
    _unsupported = other._unsupported;
  }

  /*
   * Constructs a new BDDRoute by restricting the given one to conform to the predicate pred.
   *
   * @param pred the predicate used to restrict the output routes
   * @param route a symbolic route represented as a transformation on a set of routes (input).
   */
  public BDDRoute(BDD pred, BDDRoute route) {
    _factory = route._factory;

    // Create fresh arrays for atomic predicates
    BDD[] asPathAtomicPredicates = new BDD[route._asPathRegexAtomicPredicates.length];
    BDD[] communityAtomicPredicates = new BDD[route._communityAtomicPredicates.length];

    // Intersect each atomic predicate with pred.
    for (int i = 0; i < asPathAtomicPredicates.length; i++) {
      asPathAtomicPredicates[i] = route._asPathRegexAtomicPredicates[i].and(pred);
    }

    for (int i = 0; i < communityAtomicPredicates.length; i++) {
      communityAtomicPredicates[i] = route._communityAtomicPredicates[i].and(pred);
    }

    _asPathRegexAtomicPredicates = asPathAtomicPredicates;
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
    _nextHopDiscarded = route.getNextHopDiscarded();
    _unsupported = route.getUnsupported();
    _prependedASes = new ArrayList<>(route.getPrependedASes());
    _sourceVrfs = route.getSourceVrfs();
    _tracks = route.getTracks();
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
   * Create a BDD representing the constraint that the route announcement has at least one
   * community.
   *
   * @return the bdd
   */
  public BDD anyCommunity() {
    return _factory.orAll(_communityAtomicPredicates);
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

  /** Produce a constraint that at most one of the given array of BDDs is true. */
  private BDD atMostOneOf(BDD[] bdds) {
    BDD atMostOne = _factory.one();
    for (int i = 0; i < bdds.length; i++) {
      for (int j = i + 1; j < bdds.length; j++) {
        atMostOne.andWith(bdds[i].nand(bdds[j]));
      }
    }
    return atMostOne;
  }

  /**
   * Not all assignments to the BDD variables that make up a BDDRoute represent valid BGP routes.
   * This method produces constraints that well-formed BGP routes must satisfy, represented as a
   * BDD. It is useful when the goal is to produce concrete example BGP routes from a BDDRoute, for
   * instance.
   *
   * <p>Note that it does not suffice to enforce these constraints as part of symbolic route
   * analysis (see {@link TransferBDD}). That analysis computes a BDD representing the input routes
   * that are accepted by a given route map. Conjoining the well-formedness constraints with that
   * BDD would ensure that all models are feasible routes. But if a client of the analysis is
   * instead interested in routes that are denied by the route map, then negating that BDD and
   * obtaining models would be incorrect, because it would not ensure that the well-formedness
   * constraints hold.
   *
   * @return the constraints
   */
  public BDD bgpWellFormednessConstraints() {

    // the protocol should be one of the ones allowed in a BgpRoute
    BDD protocolConstraint = anyElementOf(ALL_BGP_PROTOCOLS, this.getProtocolHistory());
    // the prefix length should be 32 or less
    BDD prefLenConstraint = _prefixLength.leq(32);
    // at most one AS-path regex atomic predicate should be true, since by construction their
    // regexes are all pairwise disjoint
    // Note: the same constraint does not apply to community regexes because a route has a set
    // of communities, so more than one regex can be simultaneously true
    BDD asPathConstraint = atMostOneOf(_asPathRegexAtomicPredicates);
    // at most one source VRF should be in the environment
    BDD sourceVrfConstraint = atMostOneOf(_sourceVrfs);
    // the next hop should be neither the min nor the max possible IP
    // this constraint is enforced by NextHopIp's constructor
    BDD nextHopConstraint = _nextHop.range(Ip.ZERO.asLong() + 1, Ip.MAX.asLong() - 1);

    return protocolConstraint
        .andWith(prefLenConstraint)
        .andWith(asPathConstraint)
        .andWith(sourceVrfConstraint)
        .andWith(nextHopConstraint);
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

  public BDD[] getAsPathRegexAtomicPredicates() {
    return _asPathRegexAtomicPredicates;
  }

  public void setAsPathRegexAtomicPredicates(BDD[] asPathRegexAtomicPredicates) {
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

  public boolean getNextHopDiscarded() {
    return _nextHopDiscarded;
  }

  public void setNextHopDiscarded(boolean nextHopDiscarded) {
    _nextHopDiscarded = nextHopDiscarded;
  }

  public boolean getNextHopSet() {
    return _nextHopSet;
  }

  public void setNextHopSet(boolean nextHopSet) {
    _nextHopSet = nextHopSet;
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

  public BDD[] getSourceVrfs() {
    return _sourceVrfs;
  }

  public BDD[] getTracks() {
    return _tracks;
  }

  public void setTracks(BDD[] tracks) {
    _tracks = tracks;
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

  // BDDRoutes are mutable so in general the default pointer equality is the right thing to use;
  // This method is used only to test the results of our symbolic route analysis.
  @VisibleForTesting
  boolean equalsForTesting(BDDRoute other) {
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
        && Objects.equals(_nextHopDiscarded, other._nextHopDiscarded)
        && Objects.equals(_nextHopSet, other._nextHopSet)
        && Objects.equals(_prefix, other._prefix)
        && Objects.equals(_prefixLength, other._prefixLength)
        && Arrays.equals(_communityAtomicPredicates, other._communityAtomicPredicates)
        && Arrays.equals(_asPathRegexAtomicPredicates, other._asPathRegexAtomicPredicates)
        && Objects.equals(_prependedASes, other._prependedASes)
        && Arrays.equals(_sourceVrfs, other._sourceVrfs)
        && Arrays.equals(_tracks, other._tracks)
        && Objects.equals(_unsupported, other._unsupported);
  }
}
