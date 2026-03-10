package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.swapPairing;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
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
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExprs;

/** A collection of attributes describing an packet, represented using BDDs */
public class BDDPacket implements Serializable {

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
  private int _nextFreeBDDVarIdxBeforePacketVars = 0;
  private int _nextFreeBDDVarIdx = FIRST_PACKET_VAR;

  // Packet bits
  private final @Nonnull ImmutableBDDInteger _dscp;
  private final @Nonnull PrimedBDDInteger _dstIp;
  private final @Nonnull PrimedBDDInteger _dstPort;
  private final @Nonnull ImmutableBDDInteger _ecn;
  private final @Nonnull ImmutableBDDInteger _fragmentOffset;
  private final @Nonnull BDDIcmpCode _icmpCode;
  private final @Nonnull BDDIcmpType _icmpType;
  private final @Nonnull BDDIpProtocol _ipProtocol;
  private final @Nonnull BDDPacketLength _packetLength;
  private final @Nonnull PrimedBDDInteger _srcIp;
  private final @Nonnull PrimedBDDInteger _srcPort;
  private final @Nonnull BDD _tcpAck;
  private final @Nonnull BDD _tcpCwr;
  private final @Nonnull BDD _tcpEce;
  private final @Nonnull BDD _tcpFin;
  private final @Nonnull BDD _tcpPsh;
  private final @Nonnull BDD _tcpRst;
  private final @Nonnull BDD _tcpSyn;
  private final @Nonnull BDD _tcpUrg;
  private final @Nonnull BDD _tcpFlags;

  private final BDDPairing _swapSourceAndDestinationPairing;
  @LazyInit private @Nullable BDD _saneFlow;

  // Generating flow preference for representative flow picking
  private transient Supplier<BDDFlowConstraintGenerator> _flowConstraintGeneratorSupplier;
  private transient IpSpaceToBDD _dstIpSpaceToBDD;
  private transient IpSpaceToBDD _srcIpSpaceToBDD;

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
        FIRST_PACKET_VAR // reserved for auxiliary variables before packet vars
            + IP_LENGTH * 4 // primed/unprimed src/dst
            + PORT_LENGTH * 4 // primed/unprimed src/dst
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

    _dstIp = allocatePrimedBDDInteger("dstIp", IP_LENGTH);
    _srcIp = allocatePrimedBDDInteger("srcIp", IP_LENGTH);
    _dstPort = allocatePrimedBDDInteger("dstPort", PORT_LENGTH);
    _srcPort = allocatePrimedBDDInteger("srcPort", PORT_LENGTH);
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
    _tcpFlags =
        factory.andLiterals(_tcpAck, _tcpCwr, _tcpEce, _tcpFin, _tcpPsh, _tcpRst, _tcpSyn, _tcpUrg);
    _dscp = allocateBDDInteger("dscp", DSCP_LENGTH);
    _ecn = allocateBDDInteger("ecn", ECN_LENGTH);
    _fragmentOffset = allocateBDDInteger("fragmentOffset", FRAGMENT_OFFSET_LENGTH);
    _packetLength = new BDDPacketLength(allocateBDDInteger("packetLength", PACKET_LENGTH_LENGTH));

    _swapSourceAndDestinationPairing =
        swapPairing(
            BDDUtils.concatBitvectors(_dstIp.getVar()._bitvec, _dstPort.getVar()._bitvec),
            BDDUtils.concatBitvectors(_srcIp.getVar()._bitvec, _srcPort.getVar()._bitvec));

    initTransientFields();
  }

  private void initTransientFields() {
    _flowConstraintGeneratorSupplier =
        Suppliers.memoize(() -> new BDDFlowConstraintGenerator(this));
    _dstIpSpaceToBDD = new IpSpaceToBDD(_dstIp.getVar());
    _srcIpSpaceToBDD = new IpSpaceToBDD(_srcIp.getVar());
  }

  @Serial
  private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    initTransientFields();
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
    return allocateBDDBitAfterPacketVars(name);
  }

  /**
   * Allocate a new single-bit {@link BDD} variable.
   *
   * @param name Used for debugging.
   * @param preferBeforePacketVars Whether the variable should be allocated before the variables
   *     used to encode packet headers. If true, and there are no remaining variables before the
   *     packet headers, will try to allocate after.
   * @return A {@link BDD} representing the sentence "this variable is true" for the new variable.
   */
  public BDD allocateBDDBit(String name, boolean preferBeforePacketVars) {
    return preferBeforePacketVars && _nextFreeBDDVarIdxBeforePacketVars < FIRST_PACKET_VAR
        ? allocateBDDBitBeforePacketVars(name)
        : allocateBDDBitAfterPacketVars(name);
  }

  private BDD allocateBDDBitAfterPacketVars(String name) {
    if (_factory.varNum() < _nextFreeBDDVarIdx + 1) {
      _factory.setVarNum(_nextFreeBDDVarIdx + 1);
    }
    _bitNames.put(_nextFreeBDDVarIdx, name);
    BDD bdd = _factory.ithVar(_nextFreeBDDVarIdx);
    _nextFreeBDDVarIdx++;
    return bdd;
  }

  /**
   * Allocate a new single-bit {@link BDD} variable before the variables used to encode packet
   * headers. Requires such a variable is available.
   *
   * @param name Used for debugging.
   * @return A {@link BDD} representing the sentence "this variable is true" for the new variable.
   */
  private BDD allocateBDDBitBeforePacketVars(String name) {
    checkArgument(
        _nextFreeBDDVarIdxBeforePacketVars < FIRST_PACKET_VAR,
        "No unassigned variable before packet vars");
    _bitNames.put(_nextFreeBDDVarIdxBeforePacketVars, name);
    BDD bdd = _factory.ithVar(_nextFreeBDDVarIdxBeforePacketVars);
    _nextFreeBDDVarIdxBeforePacketVars++;
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
    return allocateBDDInteger(name, bits, false);
  }

  /**
   * Allocate a new {@link ImmutableBDDInteger} variable.
   *
   * @param name Used for debugging.
   * @param bits The number of bits to allocate.
   * @param preferBeforePacketVars Whether the variable should be allocated before the variables
   *     used to encode packet headers. If true, and there are no remaining variables before the
   *     packet headers, will try to allocate after.
   * @return The new variable.
   */
  public ImmutableBDDInteger allocateBDDInteger(
      String name, int bits, boolean preferBeforePacketVars) {
    BDD[] vars =
        preferBeforePacketVars && _nextFreeBDDVarIdxBeforePacketVars + bits < FIRST_PACKET_VAR
            ? allocateBDDBitsBeforePacketVars(name, bits)
            : allocateBDDBitsAfterPacketVars(name, bits);
    return new ImmutableBDDInteger(_factory, vars);
  }

  /**
   * Allocate {@link BDD} variables before the variables used to encode packet headers. Requires
   * there are enough such variables available.
   *
   * @param name Used for debugging.
   * @param bits The number of bits to allocate.
   * @return An array of the new {@link BDD} variables.
   */
  private BDD[] allocateBDDBitsBeforePacketVars(String name, int bits) {
    checkArgument(
        _nextFreeBDDVarIdxBeforePacketVars + bits < FIRST_PACKET_VAR,
        "not enough unassigned variables before packet vars");
    BDD[] bdds = new BDD[bits];
    for (int i = 0; i < bits; i++) {
      bdds[i] = _factory.ithVar(_nextFreeBDDVarIdxBeforePacketVars + i);
    }
    addBitNames(name, bits, _nextFreeBDDVarIdxBeforePacketVars);
    _nextFreeBDDVarIdxBeforePacketVars += bits;
    return bdds;
  }

  /**
   * Allocate {@link BDD} variables.
   *
   * @param name Used for debugging.
   * @param bits The number of bits to allocate.
   * @return An array of the new {@link BDD} variables.
   */
  private BDD[] allocateBDDBitsAfterPacketVars(String name, int bits) {
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

  /** Create a new {@link PrimedBDDInteger} with interleaved unprimed and primed variables. */
  private PrimedBDDInteger allocatePrimedBDDInteger(String name, int length) {
    checkArgument(
        _factory.varNum() >= _nextFreeBDDVarIdx + length * 2,
        "Not enough variables to create PrimedBDDInteger");
    BDD[] vars = new BDD[length];
    BDD[] primedVars = new BDD[length];
    for (int i = 0; i < length; i++) {
      _bitNames.put(_nextFreeBDDVarIdx, name + i);
      vars[i] = _factory.ithVar(_nextFreeBDDVarIdx++);
      _bitNames.put(_nextFreeBDDVarIdx, name + "'" + i);
      primedVars[i] = _factory.ithVar(_nextFreeBDDVarIdx++);
    }
    return new PrimedBDDInteger(_factory, vars, primedVars);
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
    BDD representativeBDD = getFlowBDD(bdd, preference);
    if (representativeBDD.isZero()) {
      return Optional.empty();
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
        saneBDD, _flowConstraintGeneratorSupplier.get().getFlowPreference(preference));
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
    IpProtocol protocol = _ipProtocol.satAssignmentToValue(bits);
    Flow.Builder fb = Flow.builder();
    fb.setDstIp(Ip.create(_dstIp.getVar().satAssignmentToLong(bits)));
    fb.setSrcIp(Ip.create(_srcIp.getVar().satAssignmentToLong(bits)));
    fb.setIpProtocol(protocol);
    fb.setDscp(_dscp.satAssignmentToInt(bits));
    fb.setEcn(_ecn.satAssignmentToInt(bits));
    fb.setFragmentOffset(_fragmentOffset.satAssignmentToInt(bits));
    fb.setPacketLength(_packetLength.satAssignmentToValue(bits));
    if (IpProtocol.IP_PROTOCOLS_WITH_PORTS.contains(protocol)) {
      fb.setDstPort(_dstPort.getVar().satAssignmentToInt(bits));
      fb.setSrcPort(_srcPort.getVar().satAssignmentToInt(bits));
    }
    if (protocol == IpProtocol.ICMP) {
      fb.setIcmpCode(_icmpCode.satAssignmentToValue(bits));
      fb.setIcmpType(_icmpType.satAssignmentToValue(bits));
    }
    if (protocol == IpProtocol.TCP) {
      fb.setTcpFlagsAck(bits.get(_tcpAck.level()));
      fb.setTcpFlagsCwr(bits.get(_tcpCwr.level()));
      fb.setTcpFlagsEce(bits.get(_tcpEce.level()));
      fb.setTcpFlagsFin(bits.get(_tcpFin.level()));
      fb.setTcpFlagsPsh(bits.get(_tcpPsh.level()));
      fb.setTcpFlagsRst(bits.get(_tcpRst.level()));
      fb.setTcpFlagsSyn(bits.get(_tcpSyn.level()));
      fb.setTcpFlagsUrg(bits.get(_tcpUrg.level()));
    }
    return fb;
  }

  public @Nonnull ImmutableBDDInteger getDscp() {
    return _dscp;
  }

  public @Nonnull ImmutableBDDInteger getDstIp() {
    return _dstIp.getVar();
  }

  public @Nonnull PrimedBDDInteger getDstIpPrimedBDDInteger() {
    return _dstIp;
  }

  public @Nonnull ImmutableBDDInteger getDstPort() {
    return _dstPort.getVar();
  }

  public @Nonnull PrimedBDDInteger getDstPortPrimedBDDInteger() {
    return _dstPort;
  }

  public @Nonnull ImmutableBDDInteger getEcn() {
    return _ecn;
  }

  public @Nonnull ImmutableBDDInteger getFragmentOffset() {
    return _fragmentOffset;
  }

  public @Nonnull BDDIcmpCode getIcmpCode() {
    return _icmpCode;
  }

  public @Nonnull BDDIcmpType getIcmpType() {
    return _icmpType;
  }

  public @Nonnull BDDIpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  public @Nonnull BDDPacketLength getPacketLength() {
    return _packetLength;
  }

  public @Nonnull ImmutableBDDInteger getSrcIp() {
    return _srcIp.getVar();
  }

  public @Nonnull PrimedBDDInteger getSrcIpPrimedBDDInteger() {
    return _srcIp;
  }

  public @Nonnull ImmutableBDDInteger getSrcPort() {
    return _srcPort.getVar();
  }

  public @Nonnull PrimedBDDInteger getSrcPortPrimedBDDInteger() {
    return _srcPort;
  }

  /**
   * Returns an assignment BDD with all TCP flags true.
   *
   * <p>May be useful for {@link BDD#project(BDD)}, {@link BDD#testsVars(BDD)}, or similar
   * operations.
   */
  public @Nonnull BDD getTcpFlagsVars() {
    return _tcpFlags;
  }

  public @Nonnull BDD getTcpAck() {
    return _tcpAck;
  }

  public @Nonnull BDD getTcpCwr() {
    return _tcpCwr;
  }

  public @Nonnull BDD getTcpEce() {
    return _tcpEce;
  }

  public @Nonnull BDD getTcpFin() {
    return _tcpFin;
  }

  public @Nonnull BDD getTcpPsh() {
    return _tcpPsh;
  }

  public @Nonnull BDD getTcpRst() {
    return _tcpRst;
  }

  public @Nonnull BDD getTcpSyn() {
    return _tcpSyn;
  }

  public @Nonnull BDD getTcpUrg() {
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
