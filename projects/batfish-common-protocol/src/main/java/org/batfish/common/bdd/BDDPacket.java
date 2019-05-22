package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDInteger.makeFromIndex;
import static org.batfish.common.bdd.BDDUtils.isAssignment;
import static org.batfish.common.bdd.BDDUtils.swapPairing;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowState;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * A collection of attributes describing an packet, represented using BDDs
 *
 * @author Ryan Beckett
 */
public class BDDPacket {

  /*
   * Initial size of the BDD factory node table. Automatically resized as needed. Increasing this
   * will reduce time spent garbage collecting for large computations, but will waste memory for
   * smaller ones.
   */
  private static final int JFACTORY_INITIAL_NODE_TABLE_SIZE = 10000;

  /*
   * The ratio of node table size to node cache size to preserve when resizing. The default
   * value is 0, which means never resize the cache.
   */
  private static final int JFACTORY_CACHE_RATIO = 32;

  /*
   * Initial size of the BDD factory node cache. Automatically resized when the node table is,
   * to preserve the cache ratio.
   */
  private static final int JFACTORY_INITIAL_NODE_CACHE_SIZE =
      (JFACTORY_INITIAL_NODE_TABLE_SIZE + JFACTORY_CACHE_RATIO - 1) / JFACTORY_CACHE_RATIO;

  private static final int DSCP_LENGTH = 6;
  private static final int ECN_LENGTH = 2;
  private static final int FRAGMENT_OFFSET_LENGTH = 13;
  private static final int ICMP_CODE_LENGTH = 8;
  private static final int ICMP_TYPE_LENGTH = 8;
  private static final int IP_LENGTH = 32;
  private static final int IP_PROTOCOL_LENGTH = 8;
  private static final int PORT_LENGTH = 16;
  private static final int STATE_LENGTH = 2;
  private static final int TCP_FLAG_LENGTH = 1;

  private final Map<Integer, String> _bitNames;
  private final BDDFactory _factory;
  private int _nextFreeBDDVarIdx = 0;

  // Packet bits
  private final @Nonnull BDDInteger _dscp;
  private final @Nonnull BDDInteger _dstIp;
  private final @Nonnull BDDInteger _dstPort;
  private final @Nonnull BDDInteger _ecn;
  private final @Nonnull BDDInteger _fragmentOffset;
  private final @Nonnull BDDIcmpCode _icmpCode;
  private final @Nonnull BDDIcmpType _icmpType;
  private final @Nonnull BDDIpProtocol _ipProtocol;
  private final @Nonnull BDDInteger _srcIp;
  private final @Nonnull BDDInteger _srcPort;
  private final @Nonnull BDDInteger _state;
  private final @Nonnull BDD _tcpAck;
  private final @Nonnull BDD _tcpCwr;
  private final @Nonnull BDD _tcpEce;
  private final @Nonnull BDD _tcpFin;
  private final @Nonnull BDD _tcpPsh;
  private final @Nonnull BDD _tcpRst;
  private final @Nonnull BDD _tcpSyn;
  private final @Nonnull BDD _tcpUrg;

  private final BDDPairing _pairing;
  private final BDDPairing _swapSourceAndDestinationPairing;
  private final IpSpaceToBDD _dstIpSpaceToBDD;
  private final IpSpaceToBDD _srcIpSpaceToBDD;

  // Picking representative flows
  private final Supplier<BDDRepresentativePicker> _picker =
      Suppliers.memoize(
          () ->
              new BDDRepresentativePicker(
                  new BDDFlowConstraintGenerator(this)
                      .generateFlowPreference(FlowPreference.DEBUGGING)));

  /*
   * Creates a collection of BDD variables representing the
   * various attributes of a control plane advertisement.
   */
  public BDDPacket() {
    _factory = JFactory.init(JFACTORY_INITIAL_NODE_TABLE_SIZE, JFACTORY_INITIAL_NODE_CACHE_SIZE);
    _factory.setCacheRatio(JFACTORY_CACHE_RATIO);
    // Do not impose a maximum node table increase
    _factory.setMaxIncrease(0);
    // Disables printing
    /*
    try {
      CallbackHandler handler = new CallbackHandler();
      Method m = handler.getClass().getDeclaredMethod("handle", (Class<?>[]) null);
      factory.registerGCCallback(handler, m);
      factory.registerResizeCallback(handler, m);
      factory.registerReorderCallback(handler, m);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    */
    // Make sure we have the right number of variables
    int numNeeded =
        IP_LENGTH * 2
            + PORT_LENGTH * 2
            + IP_PROTOCOL_LENGTH
            + ICMP_CODE_LENGTH
            + ICMP_TYPE_LENGTH
            + TCP_FLAG_LENGTH * 8
            + DSCP_LENGTH
            + ECN_LENGTH
            + FRAGMENT_OFFSET_LENGTH
            + STATE_LENGTH;
    if (_factory.varNum() < numNeeded) {
      _factory.setVarNum(numNeeded);
    }

    _bitNames = new HashMap<>();

    _dstIp = allocateBDDInteger("dstIp", IP_LENGTH, false);
    _srcIp = allocateBDDInteger("srcIp", IP_LENGTH, false);
    _dstPort = allocateBDDInteger("dstPort", PORT_LENGTH, false);
    _srcPort = allocateBDDInteger("srcPort", PORT_LENGTH, false);
    _ipProtocol = new BDDIpProtocol(allocateBDDInteger("ipProtocol", IP_PROTOCOL_LENGTH, false));
    _icmpCode = new BDDIcmpCode(allocateBDDInteger("icmpCode", ICMP_CODE_LENGTH, false));
    _icmpType = new BDDIcmpType(allocateBDDInteger("icmpType", ICMP_TYPE_LENGTH, false));
    _tcpAck = allocateBDDBit("tcpAck");
    _tcpCwr = allocateBDDBit("tcpCwr");
    _tcpEce = allocateBDDBit("tcpEce");
    _tcpFin = allocateBDDBit("tcpFin");
    _tcpPsh = allocateBDDBit("tcpPsh");
    _tcpRst = allocateBDDBit("tcpRst");
    _tcpSyn = allocateBDDBit("tcpSyn");
    _tcpUrg = allocateBDDBit("tcpUrg");
    _dscp = allocateBDDInteger("dscp", DSCP_LENGTH, false);
    _ecn = allocateBDDInteger("ecn", ECN_LENGTH, false);
    _fragmentOffset = allocateBDDInteger("fragmentOffset", FRAGMENT_OFFSET_LENGTH, false);
    _state = allocateBDDInteger("state", STATE_LENGTH, false);

    _pairing = _factory.makePair();
    _swapSourceAndDestinationPairing =
        swapPairing(
            getDstIp(), getSrcIp(), //
            getDstPort(), getSrcPort());

    _dstIpSpaceToBDD = new MemoizedIpSpaceToBDD(_dstIp, ImmutableMap.of());
    _srcIpSpaceToBDD = new MemoizedIpSpaceToBDD(_srcIp, ImmutableMap.of());
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

  /**
   * Allocate a new single-bit {@link BDD} variable.
   *
   * @param name Used for debugging.
   * @return A {@link BDD} representing the sentence "this variable is true" for the new variable.
   */
  public BDD allocateBDDBit(String name) {
    if (_factory.varNum() < _nextFreeBDDVarIdx + 1) {
      _factory.setVarNum(_nextFreeBDDVarIdx + 1);
    }
    _bitNames.put(_nextFreeBDDVarIdx, name);
    BDD bdd = _factory.ithVar(_nextFreeBDDVarIdx);
    _nextFreeBDDVarIdx++;
    return bdd;
  }

  /**
   * Allocate a new {@link BDDInteger} variable.
   *
   * @param name Used for debugging.
   * @param bits The number of bits to allocate.
   * @param reverse If true, reverse the BDD order of the bits.
   * @return The new variable.
   */
  public BDDInteger allocateBDDInteger(String name, int bits, boolean reverse) {
    if (_factory.varNum() < _nextFreeBDDVarIdx + bits) {
      _factory.setVarNum(_nextFreeBDDVarIdx + bits);
    }
    BDDInteger var = makeFromIndex(_factory, bits, _nextFreeBDDVarIdx, reverse);
    addBitNames(name, STATE_LENGTH, _nextFreeBDDVarIdx, false);
    _nextFreeBDDVarIdx += bits;
    return var;
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

  public IpSpaceToBDD getDstIpSpaceToBDD() {
    return _dstIpSpaceToBDD;
  }

  public IpSpaceToBDD getSrcIpSpaceToBDD() {
    return _srcIpSpaceToBDD;
  }

  /** @return The {@link BDDFactory} used by this packet. */
  public BDDFactory getFactory() {
    return _factory;
  }

  /**
   * Get a representative flow in a BDD. First, try to get an ICMP echo request flow; second, try to
   * get a UDP flow used for traceroute; third, try to get a TCP flow with a named port; finally try
   * to get an arbitrary one.
   *
   * @param bdd a BDD representing a set of packet headers
   * @return A Flow.Builder for a representative of the set, if it's non-empty
   */
  public Optional<Flow.Builder> getFlow(BDD bdd) {
    BDD representativeBDD = _picker.get().pickRepresentative(bdd);
    if (representativeBDD.isZero()) {
      return Optional.empty();
    }
    return Optional.of(getFlowFromAssignment(representativeBDD));
  }

  public Flow.Builder getFlowFromAssignment(BDD satAssignment) {
    checkArgument(isAssignment(satAssignment));

    Flow.Builder fb = Flow.builder();
    fb.setDstIp(Ip.create(_dstIp.satAssignmentToLong(satAssignment)));
    fb.setSrcIp(Ip.create(_srcIp.satAssignmentToLong(satAssignment)));
    fb.setDstPort(_dstPort.satAssignmentToLong(satAssignment).intValue());
    fb.setSrcPort(_srcPort.satAssignmentToLong(satAssignment).intValue());
    fb.setIpProtocol(_ipProtocol.satAssignmentToValue(satAssignment));
    fb.setIcmpCode(_icmpCode.satAssignmentToValue(satAssignment));
    fb.setIcmpType(_icmpType.satAssignmentToValue(satAssignment));
    fb.setTcpFlagsAck(_tcpAck.and(satAssignment).isZero() ? 0 : 1);
    fb.setTcpFlagsCwr(_tcpCwr.and(satAssignment).isZero() ? 0 : 1);
    fb.setTcpFlagsEce(_tcpEce.and(satAssignment).isZero() ? 0 : 1);
    fb.setTcpFlagsFin(_tcpFin.and(satAssignment).isZero() ? 0 : 1);
    fb.setTcpFlagsPsh(_tcpPsh.and(satAssignment).isZero() ? 0 : 1);
    fb.setTcpFlagsRst(_tcpRst.and(satAssignment).isZero() ? 0 : 1);
    fb.setTcpFlagsSyn(_tcpSyn.and(satAssignment).isZero() ? 0 : 1);
    fb.setTcpFlagsUrg(_tcpUrg.and(satAssignment).isZero() ? 0 : 1);
    fb.setDscp(_dscp.satAssignmentToLong(satAssignment).intValue());
    fb.setEcn(_ecn.satAssignmentToLong(satAssignment).intValue());
    fb.setFragmentOffset(_fragmentOffset.satAssignmentToLong(satAssignment).intValue());
    fb.setState(FlowState.fromNum(_state.satAssignmentToLong(satAssignment).intValue()));
    return fb;
  }

  @Nonnull
  public BDDInteger getDscp() {
    return _dscp;
  }

  @Nonnull
  public BDDInteger getDstIp() {
    return _dstIp;
  }

  @Nonnull
  public BDDInteger getDstPort() {
    return _dstPort;
  }

  @Nonnull
  public BDDInteger getEcn() {
    return _ecn;
  }

  @Nonnull
  public BDDInteger getFragmentOffset() {
    return _fragmentOffset;
  }

  @Nonnull
  public BDDIcmpCode getIcmpCode() {
    return _icmpCode;
  }

  @Nonnull
  public BDDIcmpType getIcmpType() {
    return _icmpType;
  }

  @Nonnull
  public BDDIpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @Nonnull
  public BDDInteger getSrcIp() {
    return _srcIp;
  }

  @Nonnull
  public BDDInteger getSrcPort() {
    return _srcPort;
  }

  @Nonnull
  public BDDInteger getState() {
    return _state;
  }

  @Nonnull
  public BDD getTcpAck() {
    return _tcpAck;
  }

  @Nonnull
  public BDD getTcpCwr() {
    return _tcpCwr;
  }

  @Nonnull
  public BDD getTcpEce() {
    return _tcpEce;
  }

  @Nonnull
  public BDD getTcpFin() {
    return _tcpFin;
  }

  @Nonnull
  public BDD getTcpPsh() {
    return _tcpPsh;
  }

  @Nonnull
  public BDD getTcpRst() {
    return _tcpRst;
  }

  @Nonnull
  public BDD getTcpSyn() {
    return _tcpSyn;
  }

  @Nonnull
  public BDD getTcpUrg() {
    return _tcpUrg;
  }

  @Override
  public int hashCode() {
    int result = _dstIp.hashCode();
    result = 31 * result + _srcIp.hashCode();
    result = 31 * result + _dstPort.hashCode();
    result = 31 * result + _srcPort.hashCode();
    result = 31 * result + _icmpCode.hashCode();
    result = 31 * result + _icmpType.hashCode();
    result = 31 * result + _ipProtocol.hashCode();
    result = 31 * result + _tcpAck.hashCode();
    result = 31 * result + _tcpCwr.hashCode();
    result = 31 * result + _tcpEce.hashCode();
    result = 31 * result + _tcpFin.hashCode();
    result = 31 * result + _tcpPsh.hashCode();
    result = 31 * result + _tcpRst.hashCode();
    result = 31 * result + _tcpSyn.hashCode();
    result = 31 * result + _tcpUrg.hashCode();
    result = 31 * result + _dscp.hashCode();
    result = 31 * result + _ecn.hashCode();
    result = 31 * result + _fragmentOffset.hashCode();
    result = 31 * result + _state.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDPacket)) {
      return false;
    }
    BDDPacket other = (BDDPacket) o;

    return _dstIp.equals(other._dstIp)
        && _srcIp.equals(other._srcIp)
        && _srcPort.equals(other._srcPort)
        && _dstPort.equals(other._dstPort)
        && _icmpCode.equals(other._icmpCode)
        && _icmpType.equals(other._icmpType)
        && _ipProtocol.equals(other._ipProtocol)
        && _tcpAck.equals(other._tcpAck)
        && _tcpCwr.equals(other._tcpCwr)
        && _tcpEce.equals(other._tcpEce)
        && _tcpFin.equals(other._tcpFin)
        && _tcpPsh.equals(other._tcpPsh)
        && _tcpRst.equals(other._tcpRst)
        && _tcpSyn.equals(other._tcpSyn)
        && _tcpUrg.equals(other._tcpUrg)
        && _dscp.equals(other._dscp)
        && _ecn.equals(other._ecn)
        && _fragmentOffset.equals(other._fragmentOffset)
        && _state.equals(other._state);
  }

  public BDD restrict(BDD bdd, Prefix pfx) {
    int len = pfx.getPrefixLength();
    long bits = pfx.getStartIp().asLong();
    int[] vars = new int[len];
    BDD[] vals = new BDD[len];
    _pairing.reset();
    for (int i = 0; i < len; i++) {
      int var = _dstIp.getBitvec()[i].var(); // dstIpIndex + i;
      BDD subst = Ip.getBitAtPosition(bits, i) ? _factory.one() : _factory.zero();
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

  public BDD swapSourceAndDestinationFields(BDD bdd) {
    return bdd.replace(_swapSourceAndDestinationPairing);
  }
}
