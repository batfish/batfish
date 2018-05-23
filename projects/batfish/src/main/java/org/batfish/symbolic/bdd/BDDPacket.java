package org.batfish.symbolic.bdd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/*
 * Symbolic encoding of a packet using BDDs. Includes packet header
 * fields such as dstIp, srcIp, dstPort, etc.
 *
 * The dstIp is shared with the 'prefix' bits from the BDDRoute class
 * so that we can apply ACLs freely to headers generated from a prefix.
 *
 */
public class BDDPacket {

  private BDDNetFactory _netFactory;

  private BDDPairing _pairing;

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
   * Creates a collection of BDD routeVariables representing the
   * various attributes of a control plane advertisement.
   */
  public BDDPacket(BDDNetFactory netFactory) {
    _netFactory = netFactory;
    _pairing = _netFactory.makePair();
    _bitNames = new HashMap<>();
    BDDFactory factory = _netFactory.getFactory();

    // Initialize integer values
    _ipProtocol =
        BDDInteger.makeFromIndex(
            factory, _netFactory.getNumBitsIpProto(), _netFactory.getIndexIpProto(), false);
    _dstIp =
        BDDInteger.makeFromIndex(
            factory, _netFactory.getNumBitsDstIp(), _netFactory.getIndexDstIp(), false);
    _srcIp =
        BDDInteger.makeFromIndex(
            factory, _netFactory.getNumBitsSrcIp(), _netFactory.getIndexSrcIp(), false);
    _dstPort =
        BDDInteger.makeFromIndex(
            factory, _netFactory.getNumBitsDstPort(), _netFactory.getIndexDstPort(), false);
    _srcPort =
        BDDInteger.makeFromIndex(
            factory, _netFactory.getNumBitsSrcPort(), _netFactory.getIndexSrcPort(), false);
    _icmpCode =
        BDDInteger.makeFromIndex(
            factory, _netFactory.getNumBitsIcmpCode(), _netFactory.getIndexIcmpCode(), false);
    _icmpType =
        BDDInteger.makeFromIndex(
            factory, _netFactory.getNumBitsIcmpType(), _netFactory.getIndexIcmpType(), false);
    _tcpAck = factory.ithVar(_netFactory.getIndexTcpFlags());
    _tcpCwr = factory.ithVar(_netFactory.getIndexTcpFlags() + 1);
    _tcpEce = factory.ithVar(_netFactory.getIndexTcpFlags() + 2);
    _tcpFin = factory.ithVar(_netFactory.getIndexTcpFlags() + 3);
    _tcpPsh = factory.ithVar(_netFactory.getIndexTcpFlags() + 4);
    _tcpRst = factory.ithVar(_netFactory.getIndexTcpFlags() + 5);
    _tcpSyn = factory.ithVar(_netFactory.getIndexTcpFlags() + 6);
    _tcpUrg = factory.ithVar(_netFactory.getIndexTcpFlags() + 7);
    addBitNames(
        "ipProtocol", _netFactory.getNumBitsIpProto(), _netFactory.getIndexIpProto(), false);
    addBitNames("dstIp", _netFactory.getNumBitsDstIp(), _netFactory.getIndexDstIp(), false);
    addBitNames("srcIp", _netFactory.getNumBitsSrcIp(), _netFactory.getIndexSrcIp(), false);
    addBitNames("dstPort", _netFactory.getNumBitsDstPort(), _netFactory.getIndexDstPort(), false);
    addBitNames("srcPort", _netFactory.getNumBitsSrcPort(), _netFactory.getIndexSrcPort(), false);
    addBitNames(
        "icmpCode", _netFactory.getNumBitsIcmpCode(), _netFactory.getIndexIcmpCode(), false);
    addBitNames(
        "icmpType", _netFactory.getNumBitsIcmpType(), _netFactory.getIndexIcmpType(), false);
    _bitNames.put(_netFactory.getIndexTcpFlags(), "tcpAck");
    _bitNames.put(_netFactory.getIndexTcpFlags() + 1, "tcpCwr");
    _bitNames.put(_netFactory.getIndexTcpFlags() + 2, "tcpEce");
    _bitNames.put(_netFactory.getIndexTcpFlags() + 3, "tcpFin");
    _bitNames.put(_netFactory.getIndexTcpFlags() + 4, "tcpPsh");
    _bitNames.put(_netFactory.getIndexTcpFlags() + 5, "tcpRst");
    _bitNames.put(_netFactory.getIndexTcpFlags() + 6, "tcpSyn");
    _bitNames.put(_netFactory.getIndexTcpFlags() + 7, "tcpUrg");
  }

  /*
   * Create a BDDRecord from another. Because BDDs are immutable,
   * there is no need for a deep copy.
   */
  public BDDPacket(BDDPacket other) {
    _netFactory = other._netFactory;
    _bitNames = other._bitNames;
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
  public BDDPacket copy() {
    return new BDDPacket(this);
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

  public Map<Integer, String> getBitNames() {
    return _bitNames;
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
    long bits = pfx.getStartIp().asLong();
    int[] vars = new int[len];
    BDD[] vals = new BDD[len];
    _pairing.reset();
    for (int i = 0; i < len; i++) {
      int var = _dstIp.getBitvec()[i].var();
      BDD subst = Ip.getBitAtPosition(bits, i) ? _netFactory.one() : _netFactory.zero();
      vars[i] = var;
      vals[i] = subst;
    }
    _pairing.set(vars, vals);
    return bdd.veccompose(_pairing);
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