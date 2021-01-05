package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDInteger.makeFromIndex;
import static org.batfish.common.bdd.BDDUtils.swapPairing;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
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
  private static final int JFACTORY_INITIAL_NODE_TABLE_SIZE = 1_000_000;

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

  /*
   * The first BDD variable used to encode packets. Clients can use these bits anyway they want to.
   */
  public static final int FIRST_PACKET_VAR = 100;

  private static final int DSCP_LENGTH = 6;
  private static final int ECN_LENGTH = 2;
  private static final int FRAGMENT_OFFSET_LENGTH = 13;
  private static final int ICMP_CODE_LENGTH = 8;
  private static final int ICMP_TYPE_LENGTH = 8;
  private static final int IP_LENGTH = 32;
  private static final int IP_PROTOCOL_LENGTH = 8;
  private static final int PORT_LENGTH = 16;
  private static final int TCP_FLAG_LENGTH = 1;
  private static final int PACKET_LENGTH_LENGTH = 16;

  private final Map<Integer, String> _bitNames;
  private final BDDFactory _factory;
  private int _nextFreeBDDVarIdx = FIRST_PACKET_VAR;

  // Packet bits
  private final @Nonnull BDDInteger _dscp;
  private final @Nonnull BDDInteger _dstIp;
  private final @Nonnull BDDInteger _dstPort;
  private final @Nonnull BDDInteger _ecn;
  private final @Nonnull BDDInteger _fragmentOffset;
  private final @Nonnull BDDIcmpCode _icmpCode;
  private final @Nonnull BDDIcmpType _icmpType;
  private final @Nonnull BDDIpProtocol _ipProtocol;
  private final @Nonnull BDDPacketLength _packetLength;
  private final @Nonnull BDDInteger _srcIp;
  private final @Nonnull BDDInteger _srcPort;
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
  private final @Nonnull BDD _saneFlow;

  // Generating flow preference for representative flow picking
  private final Supplier<BDDFlowConstraintGenerator> _flowConstraintGeneratorSupplier =
      Suppliers.memoize(() -> new BDDFlowConstraintGenerator(this));

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
            + PACKET_LENGTH_LENGTH;
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
    _packetLength =
        new BDDPacketLength(allocateBDDInteger("packetLength", PACKET_LENGTH_LENGTH, false));

    _pairing = _factory.makePair();
    _swapSourceAndDestinationPairing =
        swapPairing(
            getDstIp(), getSrcIp(), //
            getDstPort(), getSrcPort());

    _dstIpSpaceToBDD = new MemoizedIpSpaceToBDD(_dstIp, ImmutableMap.of());
    _srcIpSpaceToBDD = new MemoizedIpSpaceToBDD(_srcIp, ImmutableMap.of());

    _saneFlow = saneIpFlow();
  }

  public @Nonnull BDD getSaneFlowConstraint() {
    // Make a copy, just in case the caller does something silly like try to free it.
    return _saneFlow.id();
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
    addBitNames(name, bits, _nextFreeBDDVarIdx, false);
    _nextFreeBDDVarIdx += bits;
    return var;
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
   * Get a representative flow in a BDD according to a given preference.
   *
   * @param bdd a BDD representing a set of packet headers
   * @param preference a FlowPreference representing flow preference
   * @return A Flow.Builder for a representative of the set, if it's non-empty
   */
  public Optional<Flow.Builder> getFlow(BDD bdd, FlowPreference preference) {
    BDD saneBDD = bdd.and(_saneFlow);
    if (saneBDD.isZero()) {
      return Optional.empty();
    }

    BDD representativeBDD = getFlowBDD(saneBDD, preference);

    if (representativeBDD.isZero()) {
      // Should not be possible if the preference is well-formed.
      return Optional.of(getRepresentativeFlow(saneBDD));
    }

    return Optional.of(getRepresentativeFlow(representativeBDD));
  }

  /**
   * Restrict a BDD according to a given flow preference.
   *
   * @param bdd a BDD representing a set of packet headers
   * @param preference a FlowPreference representing flow preference
   * @return A BDD restricted to more preferred flows. Note that the return value is NOT a full
   *     assignment.
   */
  public @Nonnull BDD getFlowBDD(BDD bdd, FlowPreference preference) {
    BDD saneBDD = bdd.and(_saneFlow);
    if (saneBDD.isZero()) {
      return saneBDD;
    }
    return BDDRepresentativePicker.pickRepresentative(
        saneBDD, _flowConstraintGeneratorSupplier.get().generateFlowPreference(preference));
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
    return getFlow(bdd, FlowPreference.DEBUGGING);
  }

  /**
   * Returns a {@link Flow.Builder} corresponding to one assignment of the given {@link BDD}.
   *
   * @throws IllegalArgumentException if there are no assignments of the given {@link BDD} that
   *     correspond to valid L3 flows.
   */
  public @Nonnull Flow.Builder getRepresentativeFlow(BDD bdd) {
    BDD saneBDD = bdd.and(_saneFlow);
    checkArgument(!saneBDD.isZero(), "The input set of flows does not contain any valid flows");
    return getFromFromAssignment(saneBDD.minAssignmentBits());
  }

  public Flow.Builder getFromFromAssignment(BitSet bits) {
    Flow.Builder fb = Flow.builder();
    fb.setDstIp(Ip.create(_dstIp.satAssignmentToLong(bits)));
    fb.setSrcIp(Ip.create(_srcIp.satAssignmentToLong(bits)));
    fb.setDstPort(_dstPort.satAssignmentToLong(bits).intValue());
    fb.setSrcPort(_srcPort.satAssignmentToLong(bits).intValue());
    fb.setIpProtocol(_ipProtocol.satAssignmentToValue(bits));
    fb.setIcmpCode(_icmpCode.satAssignmentToValue(bits));
    fb.setIcmpType(_icmpType.satAssignmentToValue(bits));
    fb.setTcpFlagsAck(bits.get(_tcpAck.level()) ? 1 : 0);
    fb.setTcpFlagsCwr(bits.get(_tcpCwr.level()) ? 1 : 0);
    fb.setTcpFlagsEce(bits.get(_tcpEce.level()) ? 1 : 0);
    fb.setTcpFlagsFin(bits.get(_tcpFin.level()) ? 1 : 0);
    fb.setTcpFlagsPsh(bits.get(_tcpPsh.level()) ? 1 : 0);
    fb.setTcpFlagsRst(bits.get(_tcpRst.level()) ? 1 : 0);
    fb.setTcpFlagsSyn(bits.get(_tcpSyn.level()) ? 1 : 0);
    fb.setTcpFlagsUrg(bits.get(_tcpUrg.level()) ? 1 : 0);
    fb.setDscp(_dscp.satAssignmentToLong(bits).intValue());
    fb.setEcn(_ecn.satAssignmentToLong(bits).intValue());
    fb.setFragmentOffset(_fragmentOffset.satAssignmentToLong(bits).intValue());
    fb.setPacketLength(_packetLength.satAssignmentToValue(bits));
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
  public BDDPacketLength getPacketLength() {
    return _packetLength;
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
        && _fragmentOffset.equals(other._fragmentOffset);
  }

  /**
   * Returns a BDD representing known constraints on all sane IP flows. For example, all IP Packets
   * are at least 20 bytes long, and all TCP packets are at least 40 bytes.
   */
  private BDD saneIpFlow() {
    BDD ipPacketsAreAtLeast20Long = _packetLength.geq(20);
    BDD validIcmp = _ipProtocol.value(IpProtocol.ICMP).impWith(_packetLength.geq(64));
    BDD validUdp =
        _ipProtocol
            .value(IpProtocol.UDP)
            .impWith(_packetLength.geq(28).and(_srcPort.geq(1)).and(_dstPort.geq(1)));
    BDD validTcp =
        _ipProtocol
            .value(IpProtocol.TCP)
            .impWith(_packetLength.geq(40).and(_srcPort.geq(1)).and(_dstPort.geq(1)));
    return BDDOps.andNull(ipPacketsAreAtLeast20Long, validIcmp, validTcp, validUdp);
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
