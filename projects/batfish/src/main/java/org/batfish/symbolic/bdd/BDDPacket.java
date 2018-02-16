package org.batfish.symbolic.bdd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;

/**
 * A collection of attributes describing an packet, represented using BDDs
 *
 * @author Ryan Beckett
 */
public class BDDPacket {

  public static BDDFactory factory;

  private static BDDPairing pairing;

  static {
    factory = JFactory.init(10000, 1000);
    // factory.enableReorder();
    // factory.setCacheRatio(64);
    // Disables printing
    try {
      CallbackHandler handler = new CallbackHandler();
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      factory.registerGCCallback(handler, m);
      factory.registerResizeCallback(handler, m);
      factory.registerReorderCallback(handler, m);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    pairing = factory.makePair();
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
  public BDDPacket() {

    // Make sure we have the right number of variables
    int numVars = factory.varNum();
    int numNeeded = 32 * 2 + 16 * 2 + 8 * 3 + 8;
    if (numVars < numNeeded) {
      factory.setVarNum(numNeeded);
    }

    _bitNames = new HashMap<>();

    // Initialize integer values
    int idx = 0;
    _ipProtocol = BDDInteger.makeFromIndex(factory, 8, idx, false);
    addBitNames("ipProtocol", 8, idx, false);
    idx += 8;
    _dstIp = BDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("dstIp", 32, idx, false);
    idx += 32;
    _srcIp = BDDInteger.makeFromIndex(factory, 32, idx, false);
    addBitNames("srcIp", 32, idx, false);
    idx += 32;
    _dstPort = BDDInteger.makeFromIndex(factory, 16, idx, false);
    addBitNames("dstPort", 16, idx, false);
    idx += 16;
    _srcPort = BDDInteger.makeFromIndex(factory, 16, idx, false);
    addBitNames("srcPort", 16, idx, false);
    idx += 16;
    _icmpCode = BDDInteger.makeFromIndex(factory, 8, idx, false);
    addBitNames("icmpCode", 8, idx, false);
    idx += 8;
    _icmpType = BDDInteger.makeFromIndex(factory, 8, idx, false);
    addBitNames("icmpType", 8, idx, false);
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
      int var = _dstIp.getBitvec()[i].var(); // dstIpIndex + i;
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

  /*
   * Create a headerspace object from a BDD
   */
  public HeaderSpace toHeaderSpace(BDD bdd) {
    Set<Integer> vars = new HashSet<>();
    example(vars, bdd.satOne());

    long dstIp = 0;
    long srcIp = 0;
    int dstPort = 0;
    int srcPort = 0;
    int ipProto = 0;
    int icmpType = 0;
    int icmpCode = 0;

    TcpFlags flags = new TcpFlags();

    for (Integer var : vars) {
      String name = _bitNames.get(var);

      if (name.startsWith("dstIp")) {
        int bit = Integer.parseInt(name.substring(5));
        dstIp = dstIp + (int) Math.pow(2, 32 - bit);
      }
      if (name.startsWith("srcIp")) {
        int bit = Integer.parseInt(name.substring(5));
        srcIp = srcIp + (int) Math.pow(2, 32 - bit);
      }
      if (name.startsWith("srcPort")) {
        int bit = Integer.parseInt(name.substring(7));
        srcPort = srcPort + (int) Math.pow(2, 16 - bit);
      }
      if (name.startsWith("dstPort")) {
        int bit = Integer.parseInt(name.substring(7));
        dstPort = dstPort + (int) Math.pow(2, 16 - bit);
      }
      if (name.startsWith("icmpType")) {
        int bit = Integer.parseInt(name.substring(8));
        icmpType = icmpType + (int) Math.pow(2, 8 - bit);
      }
      if (name.startsWith("icmpCode")) {
        int bit = Integer.parseInt(name.substring(8));
        icmpCode = icmpCode + (int) Math.pow(2, 8 - bit);
      }
      if (name.startsWith("ipProtocol")) {
        int bit = Integer.parseInt(name.substring(10));
        ipProto = ipProto + (int) Math.pow(2, 8 - bit);
      }
      if (name.startsWith("tcpAck")) {
        flags.setAck(true);
      }
      if (name.startsWith("tcpFin")) {
        flags.setFin(true);
      }
      if (name.startsWith("tcpPsh")) {
        flags.setPsh(true);
      }
      if (name.startsWith("tcpRst")) {
        flags.setRst(true);
      }
      if (name.startsWith("tcpSyn")) {
        flags.setSyn(true);
      }
      if (name.startsWith("tcpEce")) {
        flags.setEce(true);
      }
      if (name.startsWith("tcpCwr")) {
        flags.setCwr(true);
      }
      if (name.startsWith("tcpUrg")) {
        flags.setUrg(true);
      }
    }

    HeaderSpace space = new HeaderSpace();

    List<IpWildcard> wcsDst = new ArrayList<>();
    wcsDst.add(new IpWildcard(new Ip(dstIp)));
    space.setDstIps(wcsDst);

    List<IpWildcard> wcsSrc = new ArrayList<>();
    wcsSrc.add(new IpWildcard(new Ip(srcIp)));
    space.setSrcIps(wcsSrc);

    List<SubRange> dstPorts = new ArrayList<>();
    dstPorts.add(new SubRange(dstPort, dstPort));
    space.setDstPorts(dstPorts);

    List<SubRange> srcPorts = new ArrayList<>();
    srcPorts.add(new SubRange(srcPort, srcPort));
    space.setSrcPorts(srcPorts);

    List<IpProtocol> ipProtos = new ArrayList<>();
    ipProtos.add(IpProtocol.fromNumber(ipProto));
    space.setIpProtocols(ipProtos);

    List<SubRange> icmpTypes = new ArrayList<>();
    icmpTypes.add(new SubRange(icmpType, icmpType));
    space.setIcmpTypes(icmpTypes);

    List<SubRange> icmpCodes = new ArrayList<>();
    icmpCodes.add(new SubRange(icmpCode, icmpCode));
    space.setIcmpCodes(icmpCodes);

    List<TcpFlags> tcpFlags = new ArrayList<>();
    tcpFlags.add(flags);
    space.setTcpFlags(tcpFlags);

    return space;
  }

  /*
   * Recursively builds each of the intermediate BDD nodes in the
   * graphviz DOT format.
   */
  private void example(Set<Integer> vars, BDD bdd) {
    if (bdd.isOne() || bdd.isZero()) {
      return;
    }
    BDD low = bdd.low();
    BDD high = bdd.high();
    if (low.isZero()) {
      vars.add(bdd.var());
    }
    example(vars, low);
    example(vars, high);
  }

  /*
   * Create a BDD from a headerspace object
   */
  public BDD fromHeaderSpace(HeaderSpace l) {

    BDD local = null;

    if (l.getDstIps() != null) {
      BDD val = computeWildcardMatch(l.getDstIps(), getDstIp(), new HashSet<>());
      val = l.getDstIps().isEmpty() ? factory.one() : val;
      local = val;
    }

    if (l.getSrcIps() != null) {
      BDD val = computeWildcardMatch(l.getSrcIps(), getSrcIp(), null);
      val = l.getDstIps().isEmpty() ? factory.one() : val;
      local = (local == null ? val : local.and(val));
    }

    if (l.getDscps() != null && !l.getDscps().isEmpty()) {
      throw new BatfishException("detected dscps");
    }

    if (l.getDstPorts() != null) {
      BDD val = computeValidRange(l.getDstPorts(), getDstPort());
      val = l.getDstPorts().isEmpty() ? factory.one() : val;
      local = (local == null ? val : local.and(val));
    }

    if (l.getSrcPorts() != null) {
      BDD val = computeValidRange(l.getSrcPorts(), getSrcPort());
      val = l.getSrcPorts().isEmpty() ? factory.one() : val;
      local = (local == null ? val : local.and(val));
    }

    if (l.getEcns() != null && !l.getEcns().isEmpty()) {
      throw new BatfishException("detected ecns");
    }

    if (l.getTcpFlags() != null) {
      BDD val = computeTcpFlags(l.getTcpFlags());
      val = l.getTcpFlags().isEmpty() ? factory.one() : val;
      local = (local == null ? val : local.and(val));
    }

    if (l.getFragmentOffsets() != null && !l.getFragmentOffsets().isEmpty()) {
      throw new BatfishException("detected fragment offsets");
    }

    if (l.getIcmpCodes() != null) {
      BDD val = computeValidRange(l.getIcmpCodes(), getIcmpCode());
      val = l.getIcmpCodes().isEmpty() ? factory.one() : val;
      local = (local == null ? val : local.and(val));
    }

    if (l.getIcmpTypes() != null) {
      BDD val = computeValidRange(l.getIcmpTypes(), getIcmpType());
      val = l.getIcmpTypes().isEmpty() ? factory.one() : val;
      local = (local == null ? val : local.and(val));
    }

    if (l.getStates() != null && !l.getStates().isEmpty()) {
      throw new BatfishException("detected states");
    }

    if (l.getIpProtocols() != null) {
      BDD val = computeIpProtocols(l.getIpProtocols());
      val = l.getIpProtocols().isEmpty() ? factory.one() : val;
      local = (local == null ? val : local.and(val));
    }

    if (l.getNotDscps() != null && !l.getNotDscps().isEmpty()) {
      throw new BatfishException("detected NOT dscps");
    }

    if (l.getNotDstIps() != null && !l.getNotDstIps().isEmpty()) {
      throw new BatfishException("detected NOT dst ip");
    }

    if (l.getNotSrcIps() != null && !l.getNotSrcIps().isEmpty()) {
      throw new BatfishException("detected NOT src ip");
    }

    if (l.getNotDstPorts() != null && !l.getNotDstPorts().isEmpty()) {
      throw new BatfishException("detected NOT dst port");
    }

    if (l.getNotSrcPorts() != null && !l.getNotSrcPorts().isEmpty()) {
      throw new BatfishException("detected NOT src port");
    }

    if (l.getNotEcns() != null && !l.getNotEcns().isEmpty()) {
      throw new BatfishException("detected NOT ecns");
    }

    if (l.getNotIcmpCodes() != null && !l.getNotIcmpCodes().isEmpty()) {
      throw new BatfishException("detected NOT icmp codes");
    }

    if (l.getNotIcmpTypes() != null && !l.getNotIcmpTypes().isEmpty()) {
      throw new BatfishException("detected NOT icmp types");
    }

    if (l.getNotFragmentOffsets() != null && !l.getNotFragmentOffsets().isEmpty()) {
      throw new BatfishException("detected NOT fragment offset");
    }

    if (l.getNotIpProtocols() != null && !l.getNotIpProtocols().isEmpty()) {
      throw new BatfishException("detected NOT ip protocols");
    }

    if (local != null) {
      if (l.getNegate()) {
        local = local.not();
      }
      return local;
    }
    return factory.zero();
  }

  /*
   * Convert a set of ip protocols to a boolean expression on the symbolic packet
   */
  private BDD computeIpProtocols(Set<IpProtocol> ipProtos) {
    BDD acc = factory.zero();
    for (IpProtocol proto : ipProtos) {
      BDD isValue = getIpProtocol().value(proto.number());
      acc = acc.or(isValue);
    }
    return acc;
  }

  /*
   * Convert Tcp flags to a boolean expression on the symbolic packet
   */
  private BDD computeTcpFlags(List<TcpFlags> flags) {
    BDD acc = factory.zero();
    for (TcpFlags fs : flags) {
      acc = acc.or(computeTcpFlags(fs));
    }
    return acc;
  }

  /*
   * Convert a Tcp flag to a boolean expression on the symbolic packet
   */
  private BDD computeTcpFlags(TcpFlags flags) {
    BDD acc = factory.one();
    if (flags.getUseAck()) {
      BDD value = flags.getAck() ? getTcpAck() : getTcpAck().not();
      acc = acc.and(value);
    }
    if (flags.getUseCwr()) {
      BDD value = flags.getCwr() ? getTcpCwr() : getTcpCwr().not();
      acc = acc.and(value);
    }
    if (flags.getUseEce()) {
      BDD value = flags.getEce() ? getTcpEce() : getTcpEce().not();
      acc = acc.and(value);
    }
    if (flags.getUseFin()) {
      BDD value = flags.getFin() ? getTcpFin() : getTcpFin().not();
      acc = acc.and(value);
    }
    if (flags.getUsePsh()) {
      BDD value = flags.getPsh() ? getTcpPsh() : getTcpPsh().not();
      acc = acc.and(value);
    }
    if (flags.getUseRst()) {
      BDD value = flags.getRst() ? getTcpRst() : getTcpRst().not();
      acc = acc.and(value);
    }
    if (flags.getUseSyn()) {
      BDD value = flags.getSyn() ? getTcpSyn() : getTcpSyn().not();
      acc = acc.and(value);
    }
    if (flags.getUseUrg()) {
      BDD value = flags.getUrg() ? getTcpUrg() : getTcpUrg().not();
      acc = acc.and(value);
    }
    return acc;
  }

  /*
   * Convert a set of ranges and a packet field to a symbolic boolean expression
   */
  private BDD computeValidRange(Set<SubRange> ranges, BDDInteger field) {
    BDD acc = factory.zero();
    for (SubRange range : ranges) {
      int start = range.getStart();
      int end = range.getEnd();
      // System.out.println("Range: " + start + "--" + end);
      if (start == end) {
        BDD isValue = field.value(start);
        acc = acc.or(isValue);
      } else {
        BDD r = field.geq(start).and(field.leq(end));
        acc = acc.or(r);
      }
    }
    return acc;
  }

  /*
   * Convert a set of wildcards and a packet field to a symbolic boolean expression
   */
  private BDD computeWildcardMatch(
      Set<IpWildcard> wcs, BDDInteger field, @Nullable Set<Prefix> ignored) {
    BDD acc = factory.zero();
    for (IpWildcard wc : wcs) {
      if (!wc.isPrefix()) {
        throw new BatfishException("ERROR: computeDstWildcards, non sequential mask detected");
      }
      Prefix p = wc.toPrefix();
      // if (!PrefixUtils.isContainedBy(p, ignored)) {
      acc = acc.or(isRelevantFor(p, field));
      // }
    }
    return acc;
  }

  /*
   * Does the 32 bit integer match the prefix using lpm?
   */
  private BDD isRelevantFor(Prefix p, BDDInteger i) {
    return firstBitsEqual(i.getBitvec(), p, p.getPrefixLength());
  }

  /*
   * Check if the first length bits match the BDDInteger
   * representing the advertisement prefix.
   *
   * Note: We assume the prefix is never modified, so it will
   * be a bitvector containing only the underlying variables:
   * [var(0), ..., var(n)]
   */
  private BDD firstBitsEqual(BDD[] bits, Prefix p, int length) {
    BitSet b = p.getStartIp().getAddressBits();
    BDD acc = factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = b.get(i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.and(bits[i].not());
      }
    }
    return acc;
  }
}
