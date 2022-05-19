package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDFlowConstraintGenerator.RefineAll.refineAll;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.RefineFirst.refineFirst;
import static org.batfish.datamodel.PacketHeaderConstraintsUtil.DEFAULT_PACKET_LENGTH;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
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

  public interface PreferenceRefiner {
    BDD refine(BDD bdd);
  }

  static final class RefineFirst implements PreferenceRefiner {
    private final @Nullable BDD _guard;
    private final List<PreferenceRefiner> _children;

    private RefineFirst(BDD guard, List<PreferenceRefiner> children) {
      _guard = guard;
      _children = ImmutableList.copyOf(children);
    }

    static RefineFirst refineFirst(List<PreferenceRefiner> children) {
      return new RefineFirst(null, children);
    }

    static RefineFirst refineFirst(BDD guard, List<PreferenceRefiner> children) {
      return new RefineFirst(guard, children);
    }

    static RefineFirst refineFirst(BDD guard, PreferenceRefiner... children) {
      return new RefineFirst(guard, ImmutableList.copyOf(children));
    }

    static RefineFirst refineFirst(PreferenceRefiner... children) {
      return new RefineFirst(null, ImmutableList.copyOf(children));
    }

    public BDD refine(BDD bdd) {
      BDD tmp = _guard == null ? bdd : _guard.and(bdd);
      if (tmp.isZero()) {
        return tmp;
      }
      for (PreferenceRefiner child : _children) {
        BDD res = child.refine(tmp);
        if (!res.isZero()) {
          return res;
        }
      }
      return tmp;
    }
  }

  static final class RefineAll implements PreferenceRefiner {
    private final @Nullable BDD _guard;
    private final List<PreferenceRefiner> _children;

    private RefineAll(@Nullable BDD guard, List<PreferenceRefiner> children) {
      _guard = guard;
      _children = ImmutableList.copyOf(children);
    }

    static RefineAll refineAll(BDD guard, PreferenceRefiner... children) {
      return new RefineAll(guard, ImmutableList.copyOf(children));
    }

    static RefineAll refineAll(PreferenceRefiner... children) {
      return new RefineAll(null, ImmutableList.copyOf(children));
    }

    @Override
    public BDD refine(BDD bdd) {
      BDD tmp = _guard == null ? bdd.id() : _guard.and(bdd);
      if (tmp.isZero()) {
        return tmp;
      }

      for (PreferenceRefiner child : _children) {
        BDD tmp2 = child.refine(tmp);
        if (tmp2.isZero()) {
          continue;
        }
        //        tmp.free();
        tmp = tmp2;
      }
      return tmp;
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
  private final PreferenceRefiner _icmpConstraints;
  private final PreferenceRefiner _udpConstraints;
  private final PreferenceRefiner _tcpConstraints;
  private final PreferenceRefiner _defaultPacketLength;
  private final PreferenceRefiner _ipConstraints;
  private final BDD _udpTraceroute;

  private final EnumMap<FlowPreference, PreferenceRefiner> _refinerCache =
      new EnumMap<>(FlowPreference.class);

  BDDFlowConstraintGenerator(BDDPacket pkt) {
    _bddPacket = pkt;
    _bddOps = new BDDOps(pkt.getFactory());
    _defaultPacketLength = refine(_bddPacket.getPacketLength().value(DEFAULT_PACKET_LENGTH));
    _udpTraceroute = computeUdpTraceroute();
    _icmpConstraints = computeICMPConstraint();
    _udpConstraints = computeUDPConstraints();
    _tcpConstraints = computeTCPConstraints();
    _ipConstraints = computeIpConstraints();
  }

  private PreferenceRefiner computeICMPConstraint() {
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

  private PreferenceRefiner tcpNonEphemeralPortPreferences(BDDInteger tcpPort) {
    return refineFirst(
        refine(tcpPort.value(NamedPort.HTTP.number())),
        refine(tcpPort.value(NamedPort.HTTPS.number())),
        refine(tcpPort.value(NamedPort.SSH.number())),
        // at least not zero if possible
        refine(tcpPort.value(0).not()));
  }

  private PreferenceRefiner tcpFlagPreferences() {
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
        refine(_bddOps.and(syn, notAck, notCwr, notEce, notPsh, notUrg, notFin, notRst)),
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
  private PreferenceRefiner computeTCPConstraints() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD tcp = _bddPacket.getIpProtocol().value(IpProtocol.TCP);

    BDD srcPortEphemeral = emphemeralPort(srcPort);
    BDD dstPortEphemeral = emphemeralPort(dstPort);

    PreferenceRefiner nonEphemeralDstPortPreferences = tcpNonEphemeralPortPreferences(dstPort);
    PreferenceRefiner nonEphemeralSrcPortPreferences = tcpNonEphemeralPortPreferences(srcPort);

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

  private PreferenceRefiner udpNonEphemeralPortPreferences(BDDInteger tcpPort) {
    return refineFirst(
        refine(tcpPort.value(NamedPort.DOMAIN.number())),
        refine(tcpPort.value(NamedPort.SNMP.number())),
        refine(tcpPort.value(NamedPort.SNMPTRAP.number())),
        // at least not zero if possible
        refine(tcpPort.value(0).not()));
  }

  private BDD computeUdpTraceroute() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD udp = _bddPacket.getIpProtocol().value(IpProtocol.UDP);
    return _bddOps.and(
        udp,
        dstPort.range(UDP_TRACEROUTE_FIRST_PORT, UDP_TRACEROUTE_LAST_PORT),
        srcPort.geq(NamedPort.EPHEMERAL_LOWEST.number()));
  }

  // Get UDP packets with special named ports, trying to find cases where only one side is
  // ephemeral.
  private PreferenceRefiner computeUDPConstraints() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD udp = _bddPacket.getIpProtocol().value(IpProtocol.UDP);

    BDD srcPortEphemeral = emphemeralPort(srcPort);
    BDD dstPortEphemeral = emphemeralPort(dstPort);

    PreferenceRefiner nonEphemeralDstPortPreferences = udpNonEphemeralPortPreferences(dstPort);
    PreferenceRefiner nonEphemeralSrcPortPreferences = udpNonEphemeralPortPreferences(srcPort);
    return refineFirst(
        udp,
        // Try for UDP traceroute.
        refine(_udpTraceroute),
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

  private static PreferenceRefiner publicIpPreferences(BDDInteger ipInteger) {
    return refineFirst(
        // First, one of the special IPs.
        refine(ipInteger.value(Ip.parse("8.8.8.8").asLong())),
        refine(ipInteger.value(Ip.parse("1.1.1.1").asLong())),
        // Next, at least don't start with 0.
        refine(ipInteger.geq(Ip.parse("1.0.0.0").asLong())),
        // Next, try to be in class A.
        refine(ipInteger.leq(Ip.parse("126.255.255.254").asLong())));
  }

  private PreferenceRefiner computeIpConstraints() {
    BDD srcIpPrivate = isPrivateIp(_bddOps, _bddPacket.getSrcIpSpaceToBDD());
    BDD dstIpPrivate = isPrivateIp(_bddOps, _bddPacket.getDstIpSpaceToBDD());

    return refineAll(
        // 0. Try to not use documentation IPs if that is possible.
        refine(isDocumentationIp(_bddOps, _bddPacket.getSrcIpSpaceToBDD()).not()),
        refine(isDocumentationIp(_bddOps, _bddPacket.getDstIpSpaceToBDD()).not()),

        // First, try to nudge src and dst IP apart. E.g., if one is private the other should be
        // public.
        refineFirst(
            refineFirst(
                srcIpPrivate.diff(dstIpPrivate), publicIpPreferences(_bddPacket.getDstIp())),
            refineFirst(
                dstIpPrivate.diff(srcIpPrivate), publicIpPreferences(_bddPacket.getSrcIp()))));
  }

  public PreferenceRefiner getFlowPreference(FlowPreference preference) {
    return _refinerCache.computeIfAbsent(preference, this::generateFlowPreference);
  }

  private PreferenceRefiner generateFlowPreference(FlowPreference preference) {
    switch (preference) {
      case DEBUGGING:
        return refineAll(
            // application preferences
            refineFirst(_icmpConstraints, _udpConstraints, _tcpConstraints),
            _ipConstraints,
            _defaultPacketLength);
      case APPLICATION:
      case TESTFILTER:
        return refineAll(
            // application preferences
            refineFirst(_tcpConstraints, _udpConstraints, _icmpConstraints),
            _ipConstraints,
            _defaultPacketLength);
      case TRACEROUTE:
        return refineAll(
            // application preferences
            refineFirst(_udpConstraints, _tcpConstraints, _icmpConstraints),
            _ipConstraints,
            _defaultPacketLength);
      default:
        throw new BatfishException("Not supported flow preference");
    }
  }
}
