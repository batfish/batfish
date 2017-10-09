package org.batfish.symbolic.abstraction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.symbolic.CommunityVar;
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
  private Map<CommunityVar, BDD> _communities;
  private Map<Integer, String> _bitNames;

  // TODO: Same parent route reflectors

  // TODO: BGP internal on interface

  // TODO: OSPF area on interface

  // TODO: MED


  private void addBitNames(String s, int length, int index) {
    for (int i = index; i < index + length; i++) {
      _bitNames.put(i, s + (i - index));
    }
  }

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
    _communities = new HashMap<>();
    for (CommunityVar comm : comms) {
      _communities.put(comm, factory.ithVar(idx));
      _bitNames.put(idx, comm.getValue());
      idx++;
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

  public BDDRecord(BDDRecord other) {
    _communities = new HashMap<>(other._communities);
    _prefixLength = new BDDInteger(other._prefixLength);
    _prefix = new BDDInteger(other._prefix);
    _metric = new BDDInteger(other._metric);
    _adminDist = new BDDInteger(other._adminDist);
    _med = new BDDInteger(other._med);
    _localPref = new BDDInteger(other._localPref);
    _protocolHistory = new BDDDomain<>(other._protocolHistory);
  }

  private Integer dotId(BDD bdd) {
    if (bdd.isZero()) {
      return 0;
    }
    if (bdd.isOne()) {
      return 1;
    }
    return bdd.hashCode() + 2;
  }

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

  public void setPrefixLength(BDDInteger prefixLength) {
    this._prefixLength = prefixLength;
  }

  public BDDInteger getPrefix() {
    return _prefix;
  }

  public void setPrefix(BDDInteger prefix) {
    this._prefix = prefix;
  }

  public BDDInteger getAdminDist() {
    return _adminDist;
  }

  public void setAdminDist(BDDInteger adminDist) {
    this._adminDist = adminDist;
  }

  public BDDInteger getMetric() {
    return _metric;
  }

  public void setMetric(BDDInteger metric) {
    this._metric = metric;
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

  public void setLocalPref(BDDInteger localPref) {
    this._localPref = localPref;
  }

  public BDDDomain<Protocol> getProtocolHistory() {
    return _protocolHistory;
  }

  public void setProtocolHistory(BDDDomain<Protocol> protocolHistory) {
    this._protocolHistory = protocolHistory;
  }

  public Map<CommunityVar, BDD> getCommunities() {
    return _communities;
  }

  public void setCommunities(Map<CommunityVar, BDD> communities) {
    this._communities = communities;
  }

  public BDDRecord copy() {
    return new BDDRecord(this);
  }
}
