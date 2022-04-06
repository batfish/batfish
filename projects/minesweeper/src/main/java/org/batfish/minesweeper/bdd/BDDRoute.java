package org.batfish.minesweeper.bdd;

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
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.datamodel.Ip;
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

  private int _hcode = 0;

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

  private MutableBDDInteger _localPref;

  private MutableBDDInteger _med;

  private MutableBDDInteger _nextHop;

  // to properly determine the next-hop IP that results from each path through a given route-map we
  // need to track a few more pieces of information:  whether the next-hop is explicitly discarded
  // by the route-map and whether the next-hop is explicitly set by the route-map
  private BDD _nextHopDiscarded;
  private BDD _nextHopSet;

  private BDDDomain<OspfType> _ospfMetric;

  private final MutableBDDInteger _prefix;

  private final MutableBDDInteger _prefixLength;

  private final BDDDomain<RoutingProtocol> _protocolHistory;

  private MutableBDDInteger _tag;

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
        aps.getCommunityAtomicPredicates().getNumAtomicPredicates(),
        aps.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());
  }

  /**
   * Creates a collection of BDD variables representing the various attributes of a control plane
   * advertisement. Each atomic predicate created to represent community regexes will be allocated a
   * BDD variable and a BDD, and similarly for the atomic predicates for AS-path regexes, so the
   * number of such atomic predicates is provided.
   */
  public BDDRoute(
      BDDFactory factory, int numCommAtomicPredicates, int numAsPathRegexAtomicPredicates) {
    _factory = factory;

    int numVars = factory.varNum();
    int numNeeded =
        32 * 6
            + 6
            + numCommAtomicPredicates
            + numAsPathRegexAtomicPredicates
            + IntMath.log2(RoutingProtocol.values().length, RoundingMode.CEILING)
            + 2;
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
    // Initialize integer values
    _med = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("med", 32, idx, false);
    idx += 32;
    _nextHop = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("nextHop", 32, idx, false);
    idx += 32;
    _nextHopSet = factory.zero();
    _nextHopDiscarded = factory.zero();
    _tag = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("tag", 32, idx, false);
    idx += 32;
    _adminDist = MutableBDDInteger.makeFromIndex(factory, 8, idx, false);
    addBitNames("ad", 8, idx, false);
    idx += 8;
    _localPref = MutableBDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("lp", 32, idx, false);
    idx += 32;
    // need 6 bits for prefix length because there are 33 possible values, 0 - 32
    _prefixLength = MutableBDDInteger.makeFromIndex(factory, 6, idx, true);
    addBitNames("pfxLen", 5, idx, true);
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
    // Initialize OSPF type
    _ospfMetric = new BDDDomain<>(factory, allMetricTypes, idx);
    len = _ospfMetric.getInteger().size();
    addBitNames("ospfMetric", len, idx, false);
  }

  /*
   * Create a BDDRecord from another. Because BDDs are immutable,
   * there is no need for a deep copy.
   */
  public BDDRoute(BDDRoute other) {
    _factory = other._factory;

    _asPathRegexAtomicPredicates = other._asPathRegexAtomicPredicates.clone();
    _communityAtomicPredicates = other._communityAtomicPredicates.clone();
    _prefixLength = new MutableBDDInteger(other._prefixLength);
    _prefix = new MutableBDDInteger(other._prefix);
    _nextHop = new MutableBDDInteger(other._nextHop);
    _nextHopSet = other._nextHopSet.id();
    _nextHopDiscarded = other._nextHopDiscarded.id();
    _adminDist = new MutableBDDInteger(other._adminDist);
    _med = new MutableBDDInteger(other._med);
    _tag = new MutableBDDInteger(other._tag);
    _localPref = new MutableBDDInteger(other._localPref);
    _protocolHistory = new BDDDomain<>(other._protocolHistory);
    _ospfMetric = new BDDDomain<>(other._ospfMetric);
    _bitNames = other._bitNames;
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
   * Create a BDD representing the constraint that the route announcement's protocol is in the given
   * set.
   *
   * @param protocols the set of protocols that are allowed
   * @return the BDD representing this constraint
   */
  public BDD anyProtocolIn(Set<RoutingProtocol> protocols) {
    return _factory.orAll(
        protocols.stream().map(_protocolHistory::value).collect(Collectors.toList()));
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
    BDD protocolConstraint = anyProtocolIn(ALL_BGP_PROTOCOLS);
    // the prefix length should be 32 or less
    BDD prefLenConstraint = _prefixLength.leq(32);
    // at most one AS-path regex atomic predicate should be true, since by construction their
    // regexes are all pairwise disjoint
    // Note: the same constraint does not apply to community regexes because a route has a set
    // of communities, so more than one regex can be simultaneously true
    BDD asPathConstraint = _factory.one();
    for (int i = 0; i < _asPathRegexAtomicPredicates.length; i++) {
      for (int j = i + 1; j < _asPathRegexAtomicPredicates.length; j++) {
        asPathConstraint.andWith(
            _asPathRegexAtomicPredicates[i].nand(_asPathRegexAtomicPredicates[j]));
      }
    }
    // the next hop should be neither the min nor the max possible IP
    // this constraint is enforced by NextHopIp's constructor
    BDD nextHopConstraint = _nextHop.range(Ip.ZERO.asLong() + 1, Ip.MAX.asLong() - 1);

    return protocolConstraint
        .andWith(prefLenConstraint)
        .andWith(asPathConstraint)
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

  public BDD getNextHopDiscarded() {
    return _nextHopDiscarded;
  }

  public void setNextHopDiscarded(BDD nextHopDiscarded) {
    _nextHopDiscarded = nextHopDiscarded;
  }

  public BDD getNextHopSet() {
    return _nextHopSet;
  }

  public void setNextHopSet(BDD nextHopSet) {
    _nextHopSet = nextHopSet;
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

  public BDDDomain<RoutingProtocol> getProtocolHistory() {
    return _protocolHistory;
  }

  public MutableBDDInteger getTag() {
    return _tag;
  }

  public void setTag(MutableBDDInteger tag) {
    _tag = tag;
  }

  @Override
  public int hashCode() {
    if (_hcode == 0) {
      int result = _adminDist != null ? _adminDist.hashCode() : 0;
      result = 31 * result + (_ospfMetric != null ? _ospfMetric.hashCode() : 0);
      result = 31 * result + (_med != null ? _med.hashCode() : 0);
      result = 31 * result + (_localPref != null ? _localPref.hashCode() : 0);
      result = 31 * result + (_tag != null ? _tag.hashCode() : 0);
      result = 31 * result + (_nextHop != null ? _nextHop.hashCode() : 0);
      result = 31 * result + (_nextHopDiscarded != null ? _nextHopDiscarded.hashCode() : 0);
      result = 31 * result + (_nextHopSet != null ? _nextHopSet.hashCode() : 0);
      result =
          31 * result
              + (_communityAtomicPredicates != null
                  ? Arrays.hashCode(_communityAtomicPredicates)
                  : 0);
      result =
          31 * result
              + (_asPathRegexAtomicPredicates != null
                  ? Arrays.hashCode(_asPathRegexAtomicPredicates)
                  : 0);
      _hcode = result;
    }
    return _hcode;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDRoute)) {
      return false;
    }
    BDDRoute other = (BDDRoute) o;

    return Objects.equals(_ospfMetric, other._ospfMetric)
        && Objects.equals(_localPref, other._localPref)
        && Arrays.equals(_communityAtomicPredicates, other._communityAtomicPredicates)
        && Arrays.equals(_asPathRegexAtomicPredicates, other._asPathRegexAtomicPredicates)
        && Objects.equals(_med, other._med)
        && Objects.equals(_nextHop, other._nextHop)
        && Objects.equals(_nextHopDiscarded, other._nextHopDiscarded)
        && Objects.equals(_nextHopSet, other._nextHopSet)
        && Objects.equals(_tag, other._tag)
        && Objects.equals(_adminDist, other._adminDist);
  }
}
