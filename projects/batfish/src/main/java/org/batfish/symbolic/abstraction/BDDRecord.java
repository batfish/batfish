package org.batfish.symbolic.abstraction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.Protocol;

/**
 * A collection of attributes describing an advertisement, represented using BDDs
 *
 * @author Ryan Beckett
 */
public class BDDRecord {

  private static final int prefixIndex = 135;
  static BDDFactory factory;

  static {
    CallbackHandler handler = new CallbackHandler();
    try {
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      factory = JFactory.init(1000, 5000);
      // factory.setCacheRatio(4);
      factory.disableReorder();
      // factory.set
      // Disables printing
      factory.registerGCCallback(handler, m);
      factory.registerResizeCallback(handler, m);
      factory.registerReorderCallback(handler, m);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  private BDDInteger _adminDist;

  private Map<Integer, String> _bitNames;

  private SortedMap<CommunityVar, BDD> _communities;

  private BDDInteger _localPref;

  private BDDInteger _med;

  private BDDInteger _metric;

  private BDDDomain<OspfType> _ospfMetric;

  private BDDInteger _prefix;

  private BDDInteger _prefixLength;

  private BDDDomain<Protocol> _protocolHistory;

  /*
   * Creates a collection of BDD variables representing the
   * various attributes of a control plane advertisement.
   */
  public BDDRecord(Set<CommunityVar> comms) {
    int numVars = factory.varNum();
    int numNeeded = 32 * 5 + 5 + comms.size() + 4;
    if (numVars < numNeeded) {
      factory.setVarNum(numNeeded);
    }
    _bitNames = new HashMap<>();

    int idx = 0;
    // Initialize the choice of protocol
    List<Protocol> allProtos = new ArrayList<>();
    allProtos.add(Protocol.CONNECTED);
    allProtos.add(Protocol.STATIC);
    allProtos.add(Protocol.OSPF);
    allProtos.add(Protocol.BGP);
    _protocolHistory = new BDDDomain<>(factory, allProtos, idx);
    int len = _protocolHistory.getInteger().getBitvec().length;
    addBitNames("proto", len, idx);
    idx += len;
    // Initialize integer values
    _metric = BDDInteger.makeFromIndex(factory, 32, idx);
    addBitNames("metric", 32, idx);
    idx += 32;
    _med = BDDInteger.makeFromIndex(factory, 32, idx);
    addBitNames("med", 32, idx);
    idx += 32;
    _adminDist = BDDInteger.makeFromIndex(factory, 32, idx);
    addBitNames("ad", 32, idx);
    idx += 32;
    _localPref = BDDInteger.makeFromIndex(factory, 32, idx);
    addBitNames("lp", 32, idx);
    idx += 32;
    _prefixLength = BDDInteger.makeFromIndex(factory, 5, idx);
    addBitNames("pfxLen", 32, idx);
    idx += 5;
    _prefix = BDDInteger.makeFromIndex(factory, 32, idx);
    addBitNames("pfx", 32, idx);
    idx += 32;
    // Initialize communities
    _communities = new TreeMap<>();
    for (CommunityVar comm : comms) {
      if (comm.getType() != Type.REGEX) {
        _communities.put(comm, factory.ithVar(idx));
        _bitNames.put(idx, comm.getValue());
        idx++;
      }
    }
    // Initialize OSPF type
    // Realistically, the AST will only set E1 or E1. Others are just for completeness
    List<OspfType> allMetricTypes = new ArrayList<>();
    allMetricTypes.add(OspfType.O);
    allMetricTypes.add(OspfType.OIA);
    allMetricTypes.add(OspfType.E1);
    allMetricTypes.add(OspfType.E2);
    _ospfMetric = new BDDDomain<>(factory, allMetricTypes, idx);
    len = _ospfMetric.getInteger().getBitvec().length;
    addBitNames("ospfMetric", len, idx);
  }

  /*
   * Create a BDDRecord from another. Because BDDs are immutable,
   * there is no need for a deep copy.
   */
  public BDDRecord(BDDRecord other) {
    _communities = new TreeMap<>(other._communities);
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
  private void addBitNames(String s, int length, int index) {
    for (int i = index; i < index + length; i++) {
      _bitNames.put(i, s + (i - index));
    }
  }

  /*
   * Convenience method for the copy constructor
   */
  public BDDRecord copy() {
    return new BDDRecord(this);
  }

  /*
   * Converts a BDD to the graphviz DOT format for debugging.
   */
  public String dot(BDD bdd) {
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
    this._adminDist = adminDist;
  }

  public Map<CommunityVar, BDD> getCommunities() {
    return _communities;
  }

  public void setCommunities(SortedMap<CommunityVar, BDD> communities) {
    this._communities = communities;
  }

  public BDDInteger getLocalPref() {
    return _localPref;
  }

  public void setLocalPref(BDDInteger localPref) {
    this._localPref = localPref;
  }

  public BDDInteger getMed() {
    return _med;
  }

  public void setMed(BDDInteger med) {
    this._med = med;
  }

  public BDDInteger getMetric() {
    return _metric;
  }

  public void setMetric(BDDInteger metric) {
    this._metric = metric;
  }

  public BDDDomain<OspfType> getOspfMetric() {
    return _ospfMetric;
  }

  public void setOspfMetric(BDDDomain<OspfType> ospfMetric) {
    this._ospfMetric = ospfMetric;
  }

  public BDDInteger getPrefix() {
    return _prefix;
  }

  public void setPrefix(BDDInteger prefix) {
    this._prefix = prefix;
  }

  public BDDInteger getPrefixLength() {
    return _prefixLength;
  }

  public void setPrefixLength(BDDInteger prefixLength) {
    this._prefixLength = prefixLength;
  }

  public BDDDomain<Protocol> getProtocolHistory() {
    return _protocolHistory;
  }

  public void setProtocolHistory(BDDDomain<Protocol> protocolHistory) {
    this._protocolHistory = protocolHistory;
  }

  @Override
  public int hashCode() {
    int result = _prefix != null ? _prefix.hashCode() : 0;
    result = 31 * result + (_prefixLength != null ? _prefixLength.hashCode() : 0);
    result = 31 * result + (_adminDist != null ? _adminDist.hashCode() : 0);
    result = 31 * result + (_metric != null ? _metric.hashCode() : 0);
    result = 31 * result + (_ospfMetric != null ? _ospfMetric.hashCode() : 0);
    result = 31 * result + (_med != null ? _med.hashCode() : 0);
    result = 31 * result + (_localPref != null ? _localPref.hashCode() : 0);
    result = 31 * result + (_protocolHistory != null ? _protocolHistory.hashCode() : 0);
    result = 31 * result + (_communities != null ? _communities.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDRecord)) {
      return false;
    }
    BDDRecord other = (BDDRecord) o;

    return Objects.equals(_metric, other._metric)
        && Objects.equals(_ospfMetric, other._ospfMetric)
        && Objects.equals(_localPref, other._localPref)
        && Objects.equals(_communities, other._communities)
        && Objects.equals(_med, other._med)
        && Objects.equals(_protocolHistory, other._protocolHistory)
        && Objects.equals(_adminDist, other._adminDist)
        && Objects.equals(_prefix, other._prefix)
        && Objects.equals(_prefixLength, other._prefixLength);
  }

  /*
   * Take the point-wise disjunction of two BDDRecords
   */
  public BDDRecord or(BDDRecord other) {
    BDDRecord rec = new BDDRecord(this);

    BDD[] prefix = rec.getPrefix().getBitvec();
    BDD[] prefixLen = rec.getPrefixLength().getBitvec();
    BDD[] metric = rec.getMetric().getBitvec();
    BDD[] adminDist = rec.getAdminDist().getBitvec();
    BDD[] med = rec.getMed().getBitvec();
    BDD[] localPref = rec.getLocalPref().getBitvec();
    BDD[] ospfMet = rec.getOspfMetric().getInteger().getBitvec();
    BDD[] proto = rec.getProtocolHistory().getInteger().getBitvec();

    BDD[] prefix1 = getPrefix().getBitvec();
    BDD[] prefixLen1 = getPrefixLength().getBitvec();
    BDD[] metric1 = getMetric().getBitvec();
    BDD[] adminDist1 = getAdminDist().getBitvec();
    BDD[] med1 = getMed().getBitvec();
    BDD[] localPref1 = getLocalPref().getBitvec();
    BDD[] ospfMet1 = getOspfMetric().getInteger().getBitvec();
    BDD[] proto1 = getProtocolHistory().getInteger().getBitvec();

    BDD[] prefix2 = other.getPrefix().getBitvec();
    BDD[] prefixLen2 = other.getPrefixLength().getBitvec();
    BDD[] metric2 = other.getMetric().getBitvec();
    BDD[] adminDist2 = other.getAdminDist().getBitvec();
    BDD[] med2 = other.getMed().getBitvec();
    BDD[] localPref2 = other.getLocalPref().getBitvec();
    BDD[] ospfMet2 = other.getOspfMetric().getInteger().getBitvec();
    BDD[] proto2 = other.getProtocolHistory().getInteger().getBitvec();

    for (int i = 0; i < 32; i++) {
      metric[i] = metric1[i].or(metric2[i]);
      adminDist[i] = adminDist1[i].or(adminDist2[i]);
      med[i] = med1[i].or(med2[i]);
      localPref[i] = localPref1[i].or(localPref2[i]);
      prefix[i] = prefix1[i].or(prefix2[i]);
    }
    for (int i = 0; i < 5; i++) {
      prefixLen[i] = prefixLen1[i].or(prefixLen2[i]);
    }
    for (int i = 0; i < ospfMet.length; i++) {
      ospfMet[i] = ospfMet1[i].or(ospfMet2[i]);
    }
    for (int i = 0; i < proto.length; i++) {
      proto[i] = proto1[i].or(proto2[i]);
    }
    getCommunities().forEach((cvar, bdd1) -> {
      BDD bdd2 = other.getCommunities().get(cvar);
      rec.getCommunities().put(cvar, bdd1.or(bdd2));
    });

    return rec;
  }

  public BDDRecord restrict(Prefix pfx) {
    int len = pfx.getPrefixLength();
    BitSet bits = pfx.getAddress().getAddressBits();

    BDDPairing p = factory.makePair();
    for (int i = 0; i < len; i++) {
      int var = prefixIndex + i;
      BDD subst = bits.get(i) ? factory.one() : factory.zero();
      p.set(var, subst);
    }

    BDDRecord rec = new BDDRecord(this);
    BDD[] prefix = rec.getPrefix().getBitvec();
    BDD[] prefixLen = rec.getPrefixLength().getBitvec();
    BDD[] metric = rec.getMetric().getBitvec();
    BDD[] adminDist = rec.getAdminDist().getBitvec();
    BDD[] med = rec.getMed().getBitvec();
    BDD[] localPref = rec.getLocalPref().getBitvec();
    BDD[] ospfMet = rec.getOspfMetric().getInteger().getBitvec();
    BDD[] proto = rec.getProtocolHistory().getInteger().getBitvec();
    for (int i = 0; i < 32; i++) {
      metric[i] = metric[i].veccompose(p);
      adminDist[i] = adminDist[i].veccompose(p);
      med[i] = med[i].veccompose(p);
      localPref[i] = localPref[i].veccompose(p);
      prefix[i] = prefix[i].veccompose(p);
    }
    for (int i = 0; i < 5; i++) {
      prefixLen[i] = prefixLen[i].veccompose(p);
    }
    for (int i = 0; i < ospfMet.length; i++) {
      ospfMet[i] = ospfMet[i].veccompose(p);
    }
    for (int i = 0; i < proto.length; i++) {
      proto[i] = proto[i].veccompose(p);
    }
    SortedMap<CommunityVar, BDD> comms = new TreeMap<>();
    rec.getCommunities().forEach((cvar, bdd) -> comms.put(cvar, bdd.veccompose(p)));
    rec.setCommunities(comms);

    return rec;
  }

  public BDDRecord restrict(List<Prefix> prefixes) {
    if (prefixes.isEmpty()) {
      throw new BatfishException("Empty prefix list in BDDRecord restrict");
    }
    BDDRecord r = restrict(prefixes.get(0));
    for (int i = 1; i < prefixes.size(); i++) {
      Prefix p = prefixes.get(i);
      BDDRecord x = restrict(p);
      r = r.or(x);
    }
    return r;
  }
}
