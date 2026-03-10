package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDFlowConstraintGenerator.RefineAll.refineAll;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.RefineFirst.refineFirst;
import static org.batfish.datamodel.HeaderSpace.DEFAULT_PACKET_LENGTH;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;

/** This class generates common useful flow constraints as BDDs. */
public final class BDDFlowConstraintGenerator {
  /** Allows a caller to express preferences on how packets should be retrieved. */
  public enum FlowPreference {
    /** Prefers ICMP over UDP over TCP. */
    DEBUGGING,
    /** Prefers TCP over UDP over ICMP. */
    APPLICATION,
    /**
     * Prefers TCP over UDP over ICMP. Not currently different from {@link #APPLICATION}, but may
     * change.
     */
    TESTFILTER,
    /** Prefers UDP traceroute. */
    TRACEROUTE
  }

  public interface BddRefiner {
    /**
     * @param bdd such that bdd.isZero() returns false.
     * @return a BDD not == to the input. if isZero(), then the input bdd does not satisfy the
     *     preference.
     */
    BDD refine(BDD bdd);
  }

  /**
   * A {@link BddRefiner} that returns after the first sub-refiner successfully refines the input.
   * This is useful for an ordered list of mutually-exclusive preferences: once the first preference
   * consistent with the input is found, we can skip the rest.
   */
  static final class RefineFirst implements BddRefiner {
    private final @Nullable BDD _guard;
    private final List<BddRefiner> _children;

    private RefineFirst(BDD guard, List<BddRefiner> children) {
      _guard = guard;
      _children = ImmutableList.copyOf(children);
    }

    static RefineFirst refineFirst(List<BddRefiner> children) {
      return new RefineFirst(null, children);
    }

    static RefineFirst refineFirst(BDD guard, List<BddRefiner> children) {
      return new RefineFirst(guard, children);
    }

    static RefineFirst refineFirst(BDD guard, BddRefiner... children) {
      return new RefineFirst(guard, ImmutableList.copyOf(children));
    }

    static RefineFirst refineFirst(BddRefiner... children) {
      return new RefineFirst(null, ImmutableList.copyOf(children));
    }

    @Override
    public BDD refine(BDD bdd) {
      BDD matchGuard = _guard == null ? bdd.id() : _guard.and(bdd);
      if (matchGuard.isZero()) {
        return matchGuard;
      }
      for (BddRefiner child : _children) {
        BDD matchChild = child.refine(matchGuard);
        if (matchChild.isZero()) {
          matchChild.free();
        } else {
          matchGuard.free();
          return matchChild;
        }
      }
      // guard matched, but no child did.
      return matchGuard;
    }
  }

  /**
   * A {@link BddRefiner} that tries to refine the input using an ordered list of sub-refiners. Each
   * refiner is considered in order. At each step, if that sub-refiner is consistent with the
   * current BDD, its refinement is adopted and we continue to the next sub-refiner. If it is
   * inconsistent, ignore it and continue.
   *
   * <p>This is useful for a collection of preferences that are not mutually-exclusive. Note that
   * order is still important, because the input BDD may be consistent with two preferences
   * separately but not together.
   */
  static final class RefineAll implements BddRefiner {
    private final @Nullable BDD _guard;
    private final List<BddRefiner> _children;

    private RefineAll(@Nullable BDD guard, List<BddRefiner> children) {
      _guard = guard;
      _children = ImmutableList.copyOf(children);
    }

    static RefineAll refineAll(BDD guard, BddRefiner... children) {
      return new RefineAll(guard, ImmutableList.copyOf(children));
    }

    static RefineAll refineAll(BddRefiner... children) {
      return new RefineAll(null, ImmutableList.copyOf(children));
    }

    @Override
    public BDD refine(BDD bdd) {
      BDD res = _guard == null ? bdd.id() : _guard.and(bdd);
      if (res.isZero()) {
        return res;
      }

      for (BddRefiner child : _children) {
        BDD tmp = child.refine(res);
        if (tmp.isZero()) {
          tmp.free();
        } else {
          res.free();
          res = tmp;
        }
      }
      return res;
    }
  }

  static RefineFirst refine(BDD guard) {
    return new RefineFirst(guard, ImmutableList.of());
  }

  @VisibleForTesting static final Prefix PRIVATE_SUBNET_10 = Prefix.parse("10.0.0.0/8");
  @VisibleForTesting static final Prefix PRIVATE_SUBNET_172 = Prefix.parse("172.16.0.0/12");
  @VisibleForTesting static final Prefix PRIVATE_SUBNET_192 = Prefix.parse("192.168.0.0/16");

  @VisibleForTesting static final Prefix RESERVED_DOCUMENTATION_192 = Prefix.parse("192.0.2.0/24");

  @VisibleForTesting
  static final Prefix RESERVED_DOCUMENTATION_198 = Prefix.parse("198.51.100.0/24");

  @VisibleForTesting
  static final Prefix RESERVED_DOCUMENTATION_203 = Prefix.parse("203.0.113.0/24");

  static final int UDP_TRACEROUTE_FIRST_PORT = 33434;
  static final int UDP_TRACEROUTE_LAST_PORT = 33534;

  private final BDDPacket _bddPacket;
  private final BDDOps _bddOps;
  private final BddRefiner _icmpConstraints;
  private final BddRefiner _udpConstraints;
  private final BddRefiner _tcpConstraints;
  private final BddRefiner _defaultPacketLength;
  private final BddRefiner _ipConstraints;

  private final EnumMap<FlowPreference, BddRefiner> _refinerCache =
      new EnumMap<>(FlowPreference.class);

  BDDFlowConstraintGenerator(BDDPacket pkt) {
    _bddPacket = pkt;
    _bddOps = new BDDOps(pkt.getFactory());
    _defaultPacketLength = refine(_bddPacket.getPacketLength().value(DEFAULT_PACKET_LENGTH));
    _icmpConstraints = computeICMPConstraint();
    _udpConstraints = computeUDPConstraints();
    _tcpConstraints = computeTCPConstraints();
    _ipConstraints = computeIpConstraints();
  }

  private BddRefiner computeICMPConstraint() {
    BDD icmp = _bddPacket.getIpProtocol().value(IpProtocol.ICMP);
    BDDIcmpType type = _bddPacket.getIcmpType();
    BDD codeZero = _bddPacket.getIcmpCode().value(0);
    // Prefer ICMP Echo_Request, then anything with code 0, then anything ICMP/
    return refineFirst(
        icmp, refine(_bddOps.and(type.value(IcmpType.ECHO_REQUEST), codeZero)), refine(codeZero));
  }

  private BDD emphemeralPort(BDDInteger portInteger) {
    return portInteger.geq(NamedPort.EPHEMERAL_LOWEST.number());
  }

  private BddRefiner tcpNonEphemeralPortPreferences(BDDInteger tcpPort) {
    return refineFirst(
        refine(tcpPort.value(NamedPort.HTTP.number())),
        refine(tcpPort.value(NamedPort.HTTPS.number())),
        refine(tcpPort.value(NamedPort.SSH.number())),
        // at least not zero if possible
        refine(tcpPort.value(0).notEq()));
  }

  private BddRefiner tcpFlagPreferences() {
    BDD syn = _bddPacket.getTcpSyn();
    BDD ack = _bddPacket.getTcpAck();
    BDD notAck = ack.not();
    BDD notCwr = _bddPacket.getTcpCwr().not();
    BDD notEce = _bddPacket.getTcpEce().not();
    BDD notPsh = _bddPacket.getTcpPsh().not();
    BDD notUrg = _bddPacket.getTcpUrg().not();
    BDD notFin = _bddPacket.getTcpFin().not();
    BDD notRst = _bddPacket.getTcpRst().not();
    return refineFirst(
        // syn only
        refine(_bddOps.and(syn, notAck, notCwr, notEce, notPsh, notUrg, notFin, notRst)),
        // syn+ack only
        refine(_bddOps.and(syn, ack, notCwr, notEce, notPsh, notUrg, notFin, notRst)),
        // fall back to slow search
        refineAll(
            // Force all the rarely used flags off
            refine(notCwr),
            refine(notEce),
            refine(notPsh),
            refine(notUrg),
            // Less rarely used flags
            refine(notFin),
            // Sometimes used flags
            refine(notRst),
            // Prefer SYN, SYN_ACK, ACK
            refine(_bddOps.and(syn, notAck)),
            refine(_bddOps.and(ack, syn)),
            refine(_bddOps.and(ack))));
  }

  // Get TCP packets with special named ports, trying to find cases where only one side is
  // ephemeral.
  private BddRefiner computeTCPConstraints() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD tcp = _bddPacket.getIpProtocol().value(IpProtocol.TCP);

    BDD srcPortEphemeral = emphemeralPort(srcPort);
    BDD dstPortEphemeral = emphemeralPort(dstPort);

    BddRefiner nonEphemeralDstPortPreferences = tcpNonEphemeralPortPreferences(dstPort);
    BddRefiner nonEphemeralSrcPortPreferences = tcpNonEphemeralPortPreferences(srcPort);

    return refineAll(
        tcp,
        refineFirst(
            // First, try to nudge src and dst port apart. E.g., if one is ephemeral the other is
            // not.
            refineFirst(srcPortEphemeral.diff(dstPortEphemeral), nonEphemeralDstPortPreferences),
            refineFirst(dstPortEphemeral.diff(srcPortEphemeral), nonEphemeralSrcPortPreferences),
            // If both are non-ephemeral, apply port preferences
            refineAll(
                dstPortEphemeral.nor(srcPortEphemeral),
                refineFirst(nonEphemeralDstPortPreferences),
                refineFirst(nonEphemeralSrcPortPreferences))),
        tcpFlagPreferences());
  }

  private BddRefiner udpNonEphemeralPortPreferences(BDDInteger tcpPort) {
    return refineFirst(
        refine(tcpPort.value(NamedPort.DOMAIN.number())),
        refine(tcpPort.value(NamedPort.SNMP.number())),
        refine(tcpPort.value(NamedPort.SNMPTRAP.number())),
        // at least not zero if possible
        refine(tcpPort.value(0).notEq()));
  }

  // Get UDP packets with special named ports, trying to find cases where only one side is
  // ephemeral.
  private BddRefiner computeUDPConstraints() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD udp = _bddPacket.getIpProtocol().value(IpProtocol.UDP);

    BDD udpTraceroute =
        _bddOps.and(
            udp,
            dstPort.range(UDP_TRACEROUTE_FIRST_PORT, UDP_TRACEROUTE_LAST_PORT),
            srcPort.geq(NamedPort.EPHEMERAL_LOWEST.number()));

    BDD srcPortEphemeral = emphemeralPort(srcPort);
    BDD dstPortEphemeral = emphemeralPort(dstPort);

    BddRefiner nonEphemeralDstPortPreferences = udpNonEphemeralPortPreferences(dstPort);
    BddRefiner nonEphemeralSrcPortPreferences = udpNonEphemeralPortPreferences(srcPort);
    return refineFirst(
        udp,
        // Try for UDP traceroute.
        refine(udpTraceroute),
        // Next, try to nudge src and dst port apart. E.g., if one is ephemeral the other is not.
        refineFirst(srcPortEphemeral.diff(dstPortEphemeral), nonEphemeralDstPortPreferences),
        refineFirst(dstPortEphemeral.diff(srcPortEphemeral), nonEphemeralSrcPortPreferences),
        // Next, execute port preferences
        refineAll(
            refineFirst(nonEphemeralDstPortPreferences),
            refineFirst(nonEphemeralSrcPortPreferences)));
  }

  @VisibleForTesting
  static BDD isPrivateIp(BDDOps ops, IpSpaceToBDD ip) {
    return ops.or(
        ip.toBDD(PRIVATE_SUBNET_10), ip.toBDD(PRIVATE_SUBNET_172), ip.toBDD(PRIVATE_SUBNET_192));
  }

  @VisibleForTesting
  static BDD isDocumentationIp(BDDOps ops, IpSpaceToBDD ip) {
    return ops.or(
        ip.toBDD(RESERVED_DOCUMENTATION_192),
        ip.toBDD(RESERVED_DOCUMENTATION_198),
        ip.toBDD(RESERVED_DOCUMENTATION_203));
  }

  private static BddRefiner publicIpPreferences(BDDInteger ipInteger) {
    return refineFirst(
        // First, one of the special IPs.
        refine(ipInteger.value(Ip.parse("8.8.8.8").asLong())),
        refine(ipInteger.value(Ip.parse("1.1.1.1").asLong())),
        // Next, at least don't start with 0.
        refine(ipInteger.geq(Ip.parse("1.0.0.0").asLong())),
        // Next, try to be in class A.
        refine(ipInteger.leq(Ip.parse("126.255.255.254").asLong())));
  }

  private BddRefiner computeIpConstraints() {
    BDD srcIpPrivate = isPrivateIp(_bddOps, _bddPacket.getSrcIpSpaceToBDD());
    BDD dstIpPrivate = isPrivateIp(_bddOps, _bddPacket.getDstIpSpaceToBDD());

    BddRefiner publicDstIpPrefs = publicIpPreferences(_bddPacket.getDstIp());
    BddRefiner publicSrcIpPrefs = publicIpPreferences(_bddPacket.getSrcIp());
    return refineAll(
        // 0. Try to not use documentation IPs if that is possible.
        refine(isDocumentationIp(_bddOps, _bddPacket.getSrcIpSpaceToBDD()).not()),
        refine(isDocumentationIp(_bddOps, _bddPacket.getDstIpSpaceToBDD()).not()),

        // Try to nudge src and dst IP apart. E.g., if one is private the other should be
        // public.
        refineFirst(
            // private --> public
            refineFirst(srcIpPrivate.diff(dstIpPrivate), publicDstIpPrefs),
            // public --> private
            refineFirst(dstIpPrivate.diff(srcIpPrivate), publicSrcIpPrefs),
            // public --> public
            refineAll(dstIpPrivate.nor(srcIpPrivate), publicDstIpPrefs, publicSrcIpPrefs)));
  }

  public BddRefiner getFlowPreference(FlowPreference preference) {
    return _refinerCache.computeIfAbsent(preference, this::generateFlowPreference);
  }

  private BddRefiner generateFlowPreference(FlowPreference preference) {
    return switch (preference) {
      case DEBUGGING ->
          refineAll(
              // application preferences
              refineFirst(_icmpConstraints, _udpConstraints, _tcpConstraints),
              _ipConstraints,
              _defaultPacketLength);
      case APPLICATION, TESTFILTER ->
          refineAll(
              // application preferences
              refineFirst(_tcpConstraints, _udpConstraints, _icmpConstraints),
              _ipConstraints,
              _defaultPacketLength);
      case TRACEROUTE ->
          refineAll(
              // application preferences
              refineFirst(_udpConstraints, _tcpConstraints, _icmpConstraints),
              _ipConstraints,
              _defaultPacketLength);
    };
  }
}
