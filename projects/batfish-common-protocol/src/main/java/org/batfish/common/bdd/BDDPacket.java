package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.swapPairing;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.acl.AclLineMatchExprs;

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
  private static final int JFACTORY_CACHE_RATIO = 8;

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
  private final @Nonnull ImmutableBDDInteger _dscp;
  private final @Nonnull ImmutableBDDInteger _dstIp;
  private final @Nonnull ImmutableBDDInteger _dstPort;
  private final @Nonnull ImmutableBDDInteger _ecn;
  private final @Nonnull ImmutableBDDInteger _fragmentOffset;
  private final @Nonnull BDDIcmpCode _icmpCode;
  private final @Nonnull BDDIcmpType _icmpType;
  private final @Nonnull BDDIpProtocol _ipProtocol;
  private final @Nonnull BDDPacketLength _packetLength;
  private final @Nonnull ImmutableBDDInteger _srcIp;
  private final @Nonnull ImmutableBDDInteger _srcPort;
  private final @Nonnull BDD _tcpAck;
  private final @Nonnull BDD _tcpCwr;
  private final @Nonnull BDD _tcpEce;
  private final @Nonnull BDD _tcpFin;
  private final @Nonnull BDD _tcpPsh;
  private final @Nonnull BDD _tcpRst;
  private final @Nonnull BDD _tcpSyn;
  private final @Nonnull BDD _tcpUrg;

  private final BDDPairing _swapSourceAndDestinationPairing;
  private final IpSpaceToBDD _dstIpSpaceToBDD;
  private final IpSpaceToBDD _srcIpSpaceToBDD;
  @LazyInit private @Nullable BDD _saneFlow;

  // Generating flow preference for representative flow picking
  private final Supplier<BDDFlowConstraintGenerator> _flowConstraintGeneratorSupplier =
      Suppliers.memoize(() -> new BDDFlowConstraintGenerator(this));

  public static BDDFactory defaultFactory(BiFunction<Integer, Integer, BDDFactory> init) {
    BDDFactory factory =
        init.apply(JFACTORY_INITIAL_NODE_TABLE_SIZE, JFACTORY_INITIAL_NODE_CACHE_SIZE);
    factory.setCacheRatio(JFACTORY_CACHE_RATIO);
    return factory;
  }

  /**
   * Creates a collection of BDD variables representing the various attributes of a control plane
   * advertisement.
   */
  public BDDPacket() {
    this(defaultFactory(JFactory::init));
  }

  /**
   * Creates a collection of BDD variables representing the various attributes of a control plane
   * advertisement using the given existing {@link BDDFactory}.
   */
  public BDDPacket(BDDFactory factory) {
    _factory = factory;
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

    BDD[] dstIpBitvec = allocateBDDBits("dstIp", IP_LENGTH);
    BDD[] srcIpBitvec = allocateBDDBits("srcIp", IP_LENGTH);
    BDD[] dstPortBitvec = allocateBDDBits("dstPort", PORT_LENGTH);
    BDD[] srcPortBitvec = allocateBDDBits("srcPort", PORT_LENGTH);
    _dstIp = new ImmutableBDDInteger(_factory, dstIpBitvec);
    _srcIp = new ImmutableBDDInteger(_factory, srcIpBitvec);
    _dstPort = new ImmutableBDDInteger(_factory, dstPortBitvec);
    _srcPort = new ImmutableBDDInteger(_factory, srcPortBitvec);
    _ipProtocol = new BDDIpProtocol(allocateBDDInteger("ipProtocol", IP_PROTOCOL_LENGTH));
    _icmpCode = new BDDIcmpCode(allocateBDDInteger("icmpCode", ICMP_CODE_LENGTH));
    _icmpType = new BDDIcmpType(allocateBDDInteger("icmpType", ICMP_TYPE_LENGTH));
    _tcpAck = allocateBDDBit("tcpAck");
    _tcpCwr = allocateBDDBit("tcpCwr");
    _tcpEce = allocateBDDBit("tcpEce");
    _tcpFin = allocateBDDBit("tcpFin");
    _tcpPsh = allocateBDDBit("tcpPsh");
    _tcpRst = allocateBDDBit("tcpRst");
    _tcpSyn = allocateBDDBit("tcpSyn");
    _tcpUrg = allocateBDDBit("tcpUrg");
    _dscp = allocateBDDInteger("dscp", DSCP_LENGTH);
    _ecn = allocateBDDInteger("ecn", ECN_LENGTH);
    _fragmentOffset = allocateBDDInteger("fragmentOffset", FRAGMENT_OFFSET_LENGTH);
    _packetLength = new BDDPacketLength(allocateBDDInteger("packetLength", PACKET_LENGTH_LENGTH));

    _swapSourceAndDestinationPairing =
        swapPairing(
            BDDUtils.concatBitvectors(dstIpBitvec, dstPortBitvec),
            BDDUtils.concatBitvectors(srcIpBitvec, srcPortBitvec));

    _dstIpSpaceToBDD = new MemoizedIpSpaceToBDD(_dstIp, ImmutableMap.of());
    _srcIpSpaceToBDD = new MemoizedIpSpaceToBDD(_srcIp, ImmutableMap.of());
  }

  public @Nonnull BDD getSaneFlowConstraint() {
    BDD ret = _saneFlow;
    if (ret == null) {
      IpAccessListToBdd toBdd =
          new IpAccessListToBddImpl(
              this, BDDSourceManager.empty(this), ImmutableMap.of(), ImmutableMap.of());
      ret = toBdd.convert(AclLineMatchExprs.VALID_FLOWS);
      _saneFlow = ret;
    }
    // Make a copy, just in case the caller does something silly like try to free it.
    return ret.id();
  }

  /*
   * Helper function that builds a map from BDD variable index
   * to some more meaningful name. Helpful for debugging.
   */
  private void addBitNames(String s, int length, int index) {
    for (int i = index; i < index + length; i++) {
      _bitNames.put(i, s + (i - index + 1));
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
   * Allocate a new {@link ImmutableBDDInteger} variable.
   *
   * @param name Used for debugging.
   * @param bits The number of bits to allocate.
   * @return The new variable.
   */
  public ImmutableBDDInteger allocateBDDInteger(String name, int bits) {
    return new ImmutableBDDInteger(_factory, allocateBDDBits(name, bits));
  }

  /**
   * Allocate {@link BDD} variables.
   *
   * @param name Used for debugging.
   * @param bits The number of bits to allocate.
   * @return An array of the new {@link BDD} variables.
   */
  private BDD[] allocateBDDBits(String name, int bits) {
    if (_factory.varNum() < _nextFreeBDDVarIdx + bits) {
      _factory.setVarNum(_nextFreeBDDVarIdx + bits);
    }
    BDD[] bdds = new BDD[bits];
    for (int i = 0; i < bits; i++) {
      bdds[i] = _factory.ithVar(_nextFreeBDDVarIdx + i);
    }
    addBitNames(name, bits, _nextFreeBDDVarIdx);
    _nextFreeBDDVarIdx += bits;
    return bdds;
  }

  public IpSpaceToBDD getDstIpSpaceToBDD() {
    return _dstIpSpaceToBDD;
  }

  public IpSpaceToBDD getSrcIpSpaceToBDD() {
    return _srcIpSpaceToBDD;
  }

  /**
   * @return The {@link BDDFactory} used by this packet.
   */
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
    BDD saneBDD = bdd.and(getSaneFlowConstraint());
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
    BDD saneBDD = bdd.and(getSaneFlowConstraint());
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
    BDD saneBDD = bdd.and(getSaneFlowConstraint());
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
    fb.setTcpFlagsAck(bits.get(_tcpAck.level()));
    fb.setTcpFlagsCwr(bits.get(_tcpCwr.level()));
    fb.setTcpFlagsEce(bits.get(_tcpEce.level()));
    fb.setTcpFlagsFin(bits.get(_tcpFin.level()));
    fb.setTcpFlagsPsh(bits.get(_tcpPsh.level()));
    fb.setTcpFlagsRst(bits.get(_tcpRst.level()));
    fb.setTcpFlagsSyn(bits.get(_tcpSyn.level()));
    fb.setTcpFlagsUrg(bits.get(_tcpUrg.level()));
    fb.setDscp(_dscp.satAssignmentToLong(bits).intValue());
    fb.setEcn(_ecn.satAssignmentToLong(bits).intValue());
    fb.setFragmentOffset(_fragmentOffset.satAssignmentToLong(bits).intValue());
    fb.setPacketLength(_packetLength.satAssignmentToValue(bits));
    return fb;
  }

  @Nonnull
  public ImmutableBDDInteger getDscp() {
    return _dscp;
  }

  @Nonnull
  public ImmutableBDDInteger getDstIp() {
    return _dstIp;
  }

  @Nonnull
  public ImmutableBDDInteger getDstPort() {
    return _dstPort;
  }

  @Nonnull
  public ImmutableBDDInteger getEcn() {
    return _ecn;
  }

  @Nonnull
  public ImmutableBDDInteger getFragmentOffset() {
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
  public ImmutableBDDInteger getSrcIp() {
    return _srcIp;
  }

  @Nonnull
  public ImmutableBDDInteger getSrcPort() {
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

  public BDD swapSourceAndDestinationFields(BDD bdd) {
    return bdd.replace(_swapSourceAndDestinationPairing);
  }
}
