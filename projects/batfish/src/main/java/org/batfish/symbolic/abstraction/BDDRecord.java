package org.batfish.symbolic.abstraction;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
import net.sf.javabdd.JFactory;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.Protocol;

/**
 * A collection of attributes describing an advertisement, represented using BDDs
 *
 * @author Ryan Beckett
 */
public class BDDRecord {

  static BDDFactory factory;

  static {
    CallbackHandler handler = new CallbackHandler();
    try {
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      factory = JFactory.init(100, 10000);
      factory.disableReorder();
      // Disables printing
      factory.registerGCCallback(handler, m);
      factory.registerResizeCallback(handler, m);
      factory.registerReorderCallback(handler, m);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  private BDDInteger _prefix;

  private BDDInteger _prefixLength;

  private BDDInteger _adminDist;

  private BDDInteger _metric;

  private BDDInteger _med;

  private BDDInteger _localPref;

  private BDDDomain<Protocol> _protocolHistory;

  private SortedMap<CommunityVar, BDD> _communities;

  private Map<Integer, String> _bitNames;

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
   * Creates a collection of BDD variables representing the
   * various attributes of a control plane advertisement.
   */
  public BDDRecord(Set<CommunityVar> comms) {

    // Make sure we have the right number of variables
    int numVars = factory.varNum();
    int numNeeded = 32 * 5 + 5 + comms.size() + 2;
    if (numVars < numNeeded) {
      factory.setVarNum(numNeeded);
    }

    _bitNames = new HashMap<>();

    // Initialize integer values
    int idx = 0;
    _metric = BDDInteger.makeFromIndex(32, idx);
    addBitNames("metric", 32, idx);
    idx += 32;
    _adminDist = BDDInteger.makeFromIndex(32, idx);
    addBitNames("ad", 32, idx);
    idx += 32;
    _med = BDDInteger.makeFromIndex(32, idx);
    addBitNames("med", 32, idx);
    idx += 32;
    _localPref = BDDInteger.makeFromIndex(32, idx);
    addBitNames("lp", 32, idx);
    idx += 32;
    _prefixLength = BDDInteger.makeFromIndex(5, idx);
    addBitNames("pfxLen", 32, idx);
    idx += 5;
    _prefix = BDDInteger.makeFromIndex(32, idx);
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

    // Initialize the choice of protocol
    List<Protocol> allProtos = new ArrayList<>();
    allProtos.add(Protocol.CONNECTED);
    allProtos.add(Protocol.STATIC);
    allProtos.add(Protocol.OSPF);
    allProtos.add(Protocol.BGP);
    _protocolHistory = new BDDDomain<>(allProtos, idx);

    int len = _protocolHistory.getInteger().getBitvec().length;
    addBitNames("proto", len, idx);
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
  }

  /*
   * Convenience method for the copy constructor
   */
  public BDDRecord copy() {
    return new BDDRecord(this);
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
  private void getDotRec(StringBuilder sb, BDD bdd, Set<BDD> visited) {
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
    getDotRec(sb, bdd.low(), visited);
    getDotRec(sb, bdd.high(), visited);
  }

  /*
   * Converts a BDD to the graphviz DOT format for debugging.
   */
  public String getDot(BDD bdd) {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph G {\n");
    sb.append("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];\n");
    sb.append("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];\n");
    getDotRec(sb, bdd, new HashSet<>());
    sb.append("}");
    return sb.toString();
  }

  public BDDInteger getPrefixLength() {
    return _prefixLength;
  }

  public BDDInteger getPrefix() {
    return _prefix;
  }

  public BDDInteger getAdminDist() {
    return _adminDist;
  }

  public BDDInteger getMetric() {
    return _metric;
  }

  public BDDInteger getMed() {
    return _med;
  }

  public void setMed(BDDInteger med) {
    this._med = med;
  }

  public BDDInteger getLocalPref() {
    return _localPref;
  }

  public BDDDomain<Protocol> getProtocolHistory() {
    return _protocolHistory;
  }

  public void setLocalPref(BDDInteger localPref) {
    this._localPref = localPref;
  }

  public void setMetric(BDDInteger metric) {
    this._metric = metric;
  }

  public void setPrefix(BDDInteger prefix) {
    this._prefix = prefix;
  }

  public void setProtocolHistory(BDDDomain<Protocol> protocolHistory) {
    this._protocolHistory = protocolHistory;
  }

  public Map<CommunityVar, BDD> getCommunities() {
    return _communities;
  }

  public void setAdminDist(BDDInteger adminDist) {
    this._adminDist = adminDist;
  }

  public void setPrefixLength(BDDInteger prefixLength) {
    this._prefixLength = prefixLength;
  }

  public void setCommunities(SortedMap<CommunityVar, BDD> communities) {
    this._communities = communities;
  }


  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDRecord)) {
      return false;
    }
    BDDRecord other = (BDDRecord) o;

    return Objects.equals(_metric, other._metric)
        && Objects.equals(_localPref, other._localPref)
        && Objects.equals(_communities, other._communities)
        && Objects.equals(_med, other._med)
        && Objects.equals(_protocolHistory, other._protocolHistory)
        && Objects.equals(_adminDist, other._adminDist)
        && Objects.equals(_prefix, other._prefix)
        && Objects.equals(_prefixLength, other._prefixLength);
  }

  @Override
  public int hashCode() {
    int result = _prefix != null ? _prefix.hashCode() : 0;
    result = 31 * result + (_prefixLength != null ? _prefixLength.hashCode() : 0);
    result = 31 * result + (_adminDist != null ? _adminDist.hashCode() : 0);
    result = 31 * result + (_metric != null ? _metric.hashCode() : 0);
    result = 31 * result + (_med != null ? _med.hashCode() : 0);
    result = 31 * result + (_localPref != null ? _localPref.hashCode() : 0);
    result = 31 * result + (_protocolHistory != null ? _protocolHistory.hashCode() : 0);
    result = 31 * result + (_communities != null ? _communities.hashCode() : 0);
    return result;
  }
}
