package org.batfish.minesweeper.bdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.IDeepCopy;
import org.batfish.minesweeper.OspfType;
import org.batfish.minesweeper.Protocol;

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

  static BDDFactory factory;

  private static List<Protocol> allProtos;

  private static List<OspfType> allMetricTypes;

  private static BDDPairing pairing;

  private int _hcode = 0;

  static {
    allMetricTypes = new ArrayList<>();
    allMetricTypes.add(OspfType.O);
    allMetricTypes.add(OspfType.OIA);
    allMetricTypes.add(OspfType.E1);
    allMetricTypes.add(OspfType.E2);

    allProtos = new ArrayList<>();
    allProtos.add(Protocol.CONNECTED);
    allProtos.add(Protocol.STATIC);
    allProtos.add(Protocol.OSPF);
    allProtos.add(Protocol.BGP);

    factory = JFactory.init(100000, 10000);
    // factory.disableReorder();
    factory.setCacheRatio(64);
    /*
    try {
      // Disables printing
      CallbackHandler handler = new CallbackHandler();
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      factory.registerGCCallback(handler, m);
      factory.registerResizeCallback(handler, m);
      factory.registerReorderCallback(handler, m);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    */
    pairing = factory.makePair();
  }

  private BDDInteger _adminDist;

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

  private BDDInteger _localPref;

  private BDDInteger _med;

  private BDDInteger _metric;

  private BDDDomain<OspfType> _ospfMetric;

  private final BDDInteger _prefix;

  private final BDDInteger _prefixLength;

  private final BDDDomain<Protocol> _protocolHistory;

  /**
   * A constructor that obtains the number of atomic predicates for community and AS-path regexes
   * from a given {@link org.batfish.minesweeper.Graph} object.
   */
  public BDDRoute(Graph g) {
    this(
        g.getCommunityAtomicPredicates().getNumAtomicPredicates(),
        g.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());
  }

  /**
   * Creates a collection of BDD variables representing the various attributes of a control plane
   * advertisement. Each atomic predicate created to represent community regexes will be allocated a
   * BDD variable and a BDD, and similarly for the atomic predicates for AS-path regexes, so the
   * number of such atomic predicates is provided.
   */
  public BDDRoute(int numCommAtomicPredicates, int numAsPathRegexAtomicPredicates) {
    int numVars = factory.varNum();
    int numNeeded = 32 * 5 + 6 + numCommAtomicPredicates + numAsPathRegexAtomicPredicates + 4;
    if (numVars < numNeeded) {
      factory.setVarNum(numNeeded);
    }
    _bitNames = new HashMap<>();

    int idx = 0;
    _protocolHistory = new BDDDomain<>(factory, allProtos, idx);
    int len = _protocolHistory.getInteger().size();
    addBitNames("proto", len, idx, false);
    idx += len;
    // Initialize integer values
    _metric = BDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("metric", 32, idx, false);
    idx += 32;
    _med = BDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("med", 32, idx, false);
    idx += 32;
    _adminDist = BDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("ad", 32, idx, false);
    idx += 32;
    _localPref = BDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("lp", 32, idx, false);
    idx += 32;
    // need 6 bits for prefix length because there are 33 possible values, 0 - 32
    _prefixLength = BDDInteger.makeFromIndex(factory, 6, idx, true);
    addBitNames("pfxLen", 5, idx, true);
    idx += 6;
    _prefix = BDDInteger.makeFromIndex(factory, 32, idx, true);
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
    _asPathRegexAtomicPredicates = other._asPathRegexAtomicPredicates.clone();
    _communityAtomicPredicates = other._communityAtomicPredicates.clone();
    _prefixLength = new BDDInteger(other._prefixLength);
    _prefix = new BDDInteger(other._prefix);
    _metric = new BDDInteger(other._metric);
    _adminDist = new BDDInteger(other._adminDist);
    _med = new BDDInteger(other._med);
    _localPref = new BDDInteger(other._localPref);
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
   * @return the constraints
   */
  public BDD wellFormednessConstraints() {

    // the prefix length should be 32 or less
    BDD prefLenConstraint = _prefixLength.leq(32);
    // at most one AS-path regex atomic predicate should be true, since by construction their
    // regexes are all pairwise disjoint
    // Note: the same constraint does not apply to community regexes because a route has a set
    // of communities, so more than one regex can be simultaneously true
    BDD asPathConstraint = factory.one();
    for (int i = 0; i < _asPathRegexAtomicPredicates.length; i++) {
      for (int j = i + 1; j < _asPathRegexAtomicPredicates.length; j++) {
        asPathConstraint.andWith(
            _asPathRegexAtomicPredicates[i].nand(_asPathRegexAtomicPredicates[j]));
      }
    }
    return prefLenConstraint.andWith(asPathConstraint);
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

  public BDDInteger getAdminDist() {
    return _adminDist;
  }

  public void setAdminDist(BDDInteger adminDist) {
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
    return factory;
  }

  public BDDInteger getLocalPref() {
    return _localPref;
  }

  public void setLocalPref(BDDInteger localPref) {
    _localPref = localPref;
  }

  public BDDInteger getMed() {
    return _med;
  }

  public void setMed(BDDInteger med) {
    _med = med;
  }

  public BDDInteger getMetric() {
    return _metric;
  }

  public void setMetric(BDDInteger metric) {
    _metric = metric;
  }

  public BDDDomain<OspfType> getOspfMetric() {
    return _ospfMetric;
  }

  public void setOspfMetric(BDDDomain<OspfType> ospfMetric) {
    _ospfMetric = ospfMetric;
  }

  public BDDInteger getPrefix() {
    return _prefix;
  }

  public BDDInteger getPrefixLength() {
    return _prefixLength;
  }

  public BDDDomain<Protocol> getProtocolHistory() {
    return _protocolHistory;
  }

  @Override
  public int hashCode() {
    if (_hcode == 0) {
      int result = _adminDist != null ? _adminDist.hashCode() : 0;
      result = 31 * result + (_metric != null ? _metric.hashCode() : 0);
      result = 31 * result + (_ospfMetric != null ? _ospfMetric.hashCode() : 0);
      result = 31 * result + (_med != null ? _med.hashCode() : 0);
      result = 31 * result + (_localPref != null ? _localPref.hashCode() : 0);
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

    return Objects.equals(_metric, other._metric)
        && Objects.equals(_ospfMetric, other._ospfMetric)
        && Objects.equals(_localPref, other._localPref)
        && Arrays.equals(_communityAtomicPredicates, other._communityAtomicPredicates)
        && Arrays.equals(_asPathRegexAtomicPredicates, other._asPathRegexAtomicPredicates)
        && Objects.equals(_med, other._med)
        && Objects.equals(_adminDist, other._adminDist);
  }

  /*
   * Take the point-wise disjunction of two BDDRecords
   */
  public void orWith(BDDRoute other) {
    BDD[] metric = getMetric().getBitvec();
    BDD[] adminDist = getAdminDist().getBitvec();
    BDD[] med = getMed().getBitvec();
    BDD[] localPref = getLocalPref().getBitvec();
    BDD[] ospfMet = getOspfMetric().getInteger().getBitvec();

    BDD[] metric2 = other.getMetric().getBitvec();
    BDD[] adminDist2 = other.getAdminDist().getBitvec();
    BDD[] med2 = other.getMed().getBitvec();
    BDD[] localPref2 = other.getLocalPref().getBitvec();
    BDD[] ospfMet2 = other.getOspfMetric().getInteger().getBitvec();

    for (int i = 0; i < 32; i++) {
      metric[i].orWith(metric2[i]);
      adminDist[i].orWith(adminDist2[i]);
      med[i].orWith(med2[i]);
      localPref[i].orWith(localPref2[i]);
    }
    for (int i = 0; i < ospfMet.length; i++) {
      ospfMet[i].orWith(ospfMet2[i]);
    }
    for (int i = 0; i < _communityAtomicPredicates.length; i++) {
      _communityAtomicPredicates[i].orWith(other.getCommunityAtomicPredicates()[i]);
    }
    for (int i = 0; i < _asPathRegexAtomicPredicates.length; i++) {
      _asPathRegexAtomicPredicates[i].orWith(other.getAsPathRegexAtomicPredicates()[i]);
    }
  }

  public BDDRoute restrict(Prefix pfx) {
    int len = pfx.getPrefixLength();
    long bits = pfx.getStartIp().asLong();
    int[] vars = new int[len];
    BDD[] vals = new BDD[len];
    // NOTE: do not create a new pairing each time
    // JavaBDD will start to memory leak
    pairing.reset();
    for (int i = 0; i < len; i++) {
      int var = _prefix.getBitvec()[i].var(); // prefixIndex + i;
      BDD subst = Ip.getBitAtPosition(bits, i) ? factory.one() : factory.zero();
      vars[i] = var;
      vals[i] = subst;
    }
    pairing.set(vars, vals);

    BDDRoute rec = new BDDRoute(this);
    BDD[] metric = rec.getMetric().getBitvec();
    BDD[] adminDist = rec.getAdminDist().getBitvec();
    BDD[] med = rec.getMed().getBitvec();
    BDD[] localPref = rec.getLocalPref().getBitvec();
    BDD[] ospfMet = rec.getOspfMetric().getInteger().getBitvec();
    for (int i = 0; i < 32; i++) {
      metric[i] = metric[i].veccompose(pairing);
      adminDist[i] = adminDist[i].veccompose(pairing);
      med[i] = med[i].veccompose(pairing);
      localPref[i] = localPref[i].veccompose(pairing);
    }
    for (int i = 0; i < ospfMet.length; i++) {
      ospfMet[i] = ospfMet[i].veccompose(pairing);
    }
    BDD[] commAPs = rec.getCommunityAtomicPredicates();
    for (int i = 0; i < commAPs.length; i++) {
      commAPs[i] = commAPs[i].veccompose(pairing);
    }
    BDD[] asPathAPs = rec.getAsPathRegexAtomicPredicates();
    for (int i = 0; i < asPathAPs.length; i++) {
      asPathAPs[i] = asPathAPs[i].veccompose(pairing);
    }
    return rec;
  }

  public BDDRoute restrict(List<Prefix> prefixes) {
    if (prefixes.isEmpty()) {
      throw new BatfishException("Empty prefix list in BDDRecord restrict");
    }
    BDDRoute r = restrict(prefixes.get(0));
    for (int i = 1; i < prefixes.size(); i++) {
      Prefix p = prefixes.get(i);
      BDDRoute x = restrict(p);
      r.orWith(x);
    }
    return r;
  }
}
