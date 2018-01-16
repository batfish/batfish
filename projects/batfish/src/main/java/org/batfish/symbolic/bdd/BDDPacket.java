package org.batfish.symbolic.bdd;

import java.lang.reflect.Method;
import java.util.BitSet;
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
import org.batfish.datamodel.Prefix;

/**
 * A collection of attributes describing an packet, represented using BDDs
 *
 * @author Ryan Beckett
 */
public class BDDPacket {

  private static final int dstIpIndex = 8;

  static BDDFactory factory;

  private static BDDPairing pairing;

  static {
    CallbackHandler handler = new CallbackHandler();
    try {
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      factory = JFactory.init(10000, 1000);
      factory.disableReorder();
      // Disables printing
      factory.registerGCCallback(handler, m);
      factory.registerResizeCallback(handler, m);
      factory.registerReorderCallback(handler, m);
      pairing = factory.makePair();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  private Map<Integer, String> _bitNames;

  private BDDInteger _dstIp;

  private BDDInteger _dstPort;

  private BDDInteger _icmpCode;

  private BDDInteger _icmpType;

  private BDDInteger _ipProtocol;

  private BDDInteger _srcIp;

  private BDDInteger _srcPort;

  private BDD _tcpAck;

  private BDD _tcpCwr;

  private BDD _tcpEce;

  private BDD _tcpFin;

  private BDD _tcpPsh;

  private BDD _tcpRst;

  private BDD _tcpSyn;

  private BDD _tcpUrg;

  /*
   * Creates a collection of BDD variables representing the
   * various attributes of a control plane advertisement.
   */
  BDDPacket() {

    // Make sure we have the right number of variables
    int numVars = factory.varNum();
    int numNeeded = 32 * 2 + 16 * 2 + 8 * 3 + 8;
    if (numVars < numNeeded) {
      factory.setVarNum(numNeeded);
    }

    _bitNames = new HashMap<>();

    // Initialize integer values
    int idx = 0;
    _ipProtocol = BDDInteger.makeFromIndex(factory, 8, idx);
    addBitNames("ipProtocol", 8, idx);
    idx += 8;
    _dstIp = BDDInteger.makeFromIndex(factory, 32, idx);
    addBitNames("dstIp", 32, idx);
    idx += 32;
    _srcIp = BDDInteger.makeFromIndex(factory, 32, idx);
    addBitNames("srcIp", 32, idx);
    idx += 32;
    _dstPort = BDDInteger.makeFromIndex(factory, 16, idx);
    addBitNames("dstPort", 16, idx);
    idx += 16;
    _srcPort = BDDInteger.makeFromIndex(factory, 16, idx);
    addBitNames("srcPort", 16, idx);
    idx += 16;
    _icmpCode = BDDInteger.makeFromIndex(factory, 8, idx);
    addBitNames("icmpCode", 8, idx);
    idx += 8;
    _icmpType = BDDInteger.makeFromIndex(factory, 8, idx);
    addBitNames("icmpType", 8, idx);
    idx += 8;
    _tcpAck = factory.ithVar(idx);
    _bitNames.put(idx, "tcpAck");
    idx += 1;
    _tcpCwr = factory.ithVar(idx);
    _bitNames.put(idx, "tcpCwr");
    idx += 1;
    _tcpEce = factory.ithVar(idx);
    _bitNames.put(idx, "tcpEce");
    idx += 1;
    _tcpFin = factory.ithVar(idx);
    _bitNames.put(idx, "tcpFin");
    idx += 1;
    _tcpPsh = factory.ithVar(idx);
    _bitNames.put(idx, "tcpPsh");
    idx += 1;
    _tcpRst = factory.ithVar(idx);
    _bitNames.put(idx, "tcpRst");
    idx += 1;
    _tcpSyn = factory.ithVar(idx);
    _bitNames.put(idx, "tcpSyn");
    idx += 1;
    _tcpUrg = factory.ithVar(idx);
    _bitNames.put(idx, "tcpUrg");
  }

  /*
   * Create a BDDRecord from another. Because BDDs are immutable,
   * there is no need for a deep copy.
   */
  private BDDPacket(BDDPacket other) {
    _srcIp = new BDDInteger(other._srcIp);
    _dstIp = new BDDInteger(other._dstIp);
    _srcPort = new BDDInteger(other._srcPort);
    _dstPort = new BDDInteger(other._dstPort);
    _icmpCode = new BDDInteger(other._icmpCode);
    _icmpType = new BDDInteger(other._icmpType);
    _ipProtocol = new BDDInteger(other._ipProtocol);
    _tcpAck = other._tcpAck;
    _tcpCwr = other._tcpCwr;
    _tcpEce = other._tcpEce;
    _tcpFin = other._tcpFin;
    _tcpPsh = other._tcpPsh;
    _tcpRst = other._tcpRst;
    _tcpSyn = other._tcpSyn;
    _tcpUrg = other._tcpUrg;
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
  public BDDPacket copy() {
    return new BDDPacket(this);
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

  public BDDInteger getDstIp() {
    return _dstIp;
  }

  public void setDstIp(BDDInteger x) {
    this._dstIp = x;
  }

  public BDDInteger getDstPort() {
    return _dstPort;
  }

  public void setDstPort(BDDInteger x) {
    this._dstPort = x;
  }

  public BDDInteger getIcmpCode() {
    return _icmpCode;
  }

  public void setIcmpCode(BDDInteger x) {
    this._icmpCode = x;
  }

  public BDDInteger getIcmpType() {
    return _icmpType;
  }

  public void setIcmpType(BDDInteger x) {
    this._icmpType = x;
  }

  public BDDInteger getIpProtocol() {
    return _ipProtocol;
  }

  public void setIpProtocol(BDDInteger x) {
    this._ipProtocol = x;
  }

  public BDDInteger getSrcIp() {
    return _srcIp;
  }

  public void setSrcIp(BDDInteger x) {
    this._srcIp = x;
  }

  public BDDInteger getSrcPort() {
    return _srcPort;
  }

  public void setSrcPort(BDDInteger x) {
    this._srcPort = x;
  }

  public BDD getTcpAck() {
    return _tcpAck;
  }

  public void setTcpAck(BDD tcpAck) {
    this._tcpAck = tcpAck;
  }

  public BDD getTcpCwr() {
    return _tcpCwr;
  }

  public void setTcpCwr(BDD tcpCwr) {
    this._tcpCwr = tcpCwr;
  }

  public BDD getTcpEce() {
    return _tcpEce;
  }

  public void setTcpEce(BDD tcpEce) {
    this._tcpEce = tcpEce;
  }

  public BDD getTcpFin() {
    return _tcpFin;
  }

  public void setTcpFin(BDD tcpFin) {
    this._tcpFin = tcpFin;
  }

  public BDD getTcpPsh() {
    return _tcpPsh;
  }

  public void setTcpPsh(BDD tcpPsh) {
    this._tcpPsh = tcpPsh;
  }

  public BDD getTcpRst() {
    return _tcpRst;
  }

  public void setTcpRst(BDD tcpRst) {
    this._tcpRst = tcpRst;
  }

  public BDD getTcpSyn() {
    return _tcpSyn;
  }

  public void setTcpSyn(BDD tcpSyn) {
    this._tcpSyn = tcpSyn;
  }

  public BDD getTcpUrg() {
    return _tcpUrg;
  }

  public void setTcpUrg(BDD tcpUrg) {
    this._tcpUrg = tcpUrg;
  }

  @Override
  public int hashCode() {
    int result = _dstIp != null ? _dstIp.hashCode() : 0;
    result = 31 * result + (_srcIp != null ? _srcIp.hashCode() : 0);
    result = 31 * result + (_dstPort != null ? _dstPort.hashCode() : 0);
    result = 31 * result + (_srcPort != null ? _srcPort.hashCode() : 0);
    result = 31 * result + (_icmpCode != null ? _icmpCode.hashCode() : 0);
    result = 31 * result + (_icmpType != null ? _icmpType.hashCode() : 0);
    result = 31 * result + (_ipProtocol != null ? _ipProtocol.hashCode() : 0);
    result = 31 * result + (_tcpAck != null ? _tcpAck.hashCode() : 0);
    result = 31 * result + (_tcpCwr != null ? _tcpCwr.hashCode() : 0);
    result = 31 * result + (_tcpEce != null ? _tcpEce.hashCode() : 0);
    result = 31 * result + (_tcpFin != null ? _tcpFin.hashCode() : 0);
    result = 31 * result + (_tcpPsh != null ? _tcpPsh.hashCode() : 0);
    result = 31 * result + (_tcpRst != null ? _tcpRst.hashCode() : 0);
    result = 31 * result + (_tcpSyn != null ? _tcpSyn.hashCode() : 0);
    result = 31 * result + (_tcpUrg != null ? _tcpUrg.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDPacket)) {
      return false;
    }
    BDDPacket other = (BDDPacket) o;

    return Objects.equals(_srcPort, other._srcPort)
        && Objects.equals(_icmpType, other._icmpType)
        && Objects.equals(_icmpCode, other._icmpCode)
        && Objects.equals(_ipProtocol, other._ipProtocol)
        && Objects.equals(_dstPort, other._dstPort)
        && Objects.equals(_dstIp, other._dstIp)
        && Objects.equals(_srcIp, other._srcIp)
        && Objects.equals(_tcpAck, other._tcpAck)
        && Objects.equals(_tcpCwr, other._tcpCwr)
        && Objects.equals(_tcpEce, other._tcpEce)
        && Objects.equals(_tcpFin, other._tcpFin)
        && Objects.equals(_tcpPsh, other._tcpPsh)
        && Objects.equals(_tcpRst, other._tcpRst)
        && Objects.equals(_tcpSyn, other._tcpSyn)
        && Objects.equals(_tcpUrg, other._tcpUrg);
  }

  public BDD restrict(BDD bdd, Prefix pfx) {
    int len = pfx.getPrefixLength();
    BitSet bits = pfx.getStartIp().getAddressBits();
    int[] vars = new int[len];
    BDD[] vals = new BDD[len];
    pairing.reset();
    for (int i = 0; i < len; i++) {
      int var = dstIpIndex + i;
      BDD subst = bits.get(i) ? factory.one() : factory.zero();
      vars[i] = var;
      vals[i] = subst;
    }
    pairing.set(vars, vals);
    return bdd.veccompose(pairing);
  }

  public BDD restrict(BDD bdd, List<Prefix> prefixes) {
    if (prefixes.isEmpty()) {
      throw new BatfishException("Empty prefix list in BDDRecord restrict");
    }
    BDD r = restrict(bdd, prefixes.get(0));
    for (int i = 1; i < prefixes.size(); i++) {
      Prefix p = prefixes.get(i);
      BDD x = restrict(bdd, p);
      r = r.or(x);
    }
    return r;
  }
}
