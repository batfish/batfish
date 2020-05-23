package org.batfish.common.bdd;

import static org.batfish.datamodel.PacketHeaderConstraintsUtil.DEFAULT_PACKET_LENGTH;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import java.util.List;
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
  private final List<BDD> _icmpConstraints;
  private final List<BDD> _udpConstraints;
  private final List<BDD> _tcpConstraints;
  private final BDD _defaultPacketLength;
  private final List<BDD> _ipConstraints;
  private final BDD _udpTraceroute;

  BDDFlowConstraintGenerator(BDDPacket pkt) {
    Span span = GlobalTracer.get().buildSpan("construct BDDFlowConstraintGenerator").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
      _bddPacket = pkt;
      _bddOps = new BDDOps(pkt.getFactory());
      _defaultPacketLength = _bddPacket.getPacketLength().value(DEFAULT_PACKET_LENGTH);
      _udpTraceroute = computeUdpTraceroute();
      _icmpConstraints = computeICMPConstraint();
      _udpConstraints = computeUDPConstraints();
      _tcpConstraints = computeTCPConstraints();
      _ipConstraints = computeIpConstraints();
    }
  }

  private List<BDD> computeICMPConstraint() {
    BDD icmp = _bddPacket.getIpProtocol().value(IpProtocol.ICMP);
    BDDIcmpType type = _bddPacket.getIcmpType();
    BDD codeZero = _bddPacket.getIcmpCode().value(0);
    // Prefer ICMP Echo_Request, then anything with code 0, then anything ICMP/
    return ImmutableList.of(
        _bddOps.and(icmp, type.value(IcmpType.ECHO_REQUEST), codeZero),
        _bddOps.and(icmp, codeZero),
        icmp);
  }

  private BDD emphemeralPort(BDDInteger portInteger) {
    return portInteger.geq(NamedPort.EPHEMERAL_LOWEST.number());
  }

  private List<BDD> tcpPortPreferences(BDD tcp, BDDInteger tcpPort) {
    return ImmutableList.of(
        _bddOps.and(tcp, tcpPort.value(NamedPort.HTTP.number())),
        _bddOps.and(tcp, tcpPort.value(NamedPort.HTTPS.number())),
        _bddOps.and(tcp, tcpPort.value(NamedPort.SSH.number())),
        // at least not zero if possible
        _bddOps.and(tcp, tcpPort.value(0).not()));
  }

  private List<BDD> tcpFlagPreferences(BDD tcp) {
    return ImmutableList.of(
        // Force all the rarely used flags off
        _bddOps.and(tcp, _bddPacket.getTcpCwr().not()),
        _bddOps.and(tcp, _bddPacket.getTcpEce().not()),
        _bddOps.and(tcp, _bddPacket.getTcpPsh().not()),
        _bddOps.and(tcp, _bddPacket.getTcpUrg().not()),
        // Less rarely used flags
        _bddOps.and(tcp, _bddPacket.getTcpFin().not()),
        // Sometimes used flags
        _bddOps.and(tcp, _bddPacket.getTcpRst().not()),
        // Prefer SYN, SYN_ACK, ACK
        _bddOps.and(tcp, _bddPacket.getTcpSyn(), _bddPacket.getTcpAck().not()),
        _bddOps.and(tcp, _bddPacket.getTcpAck(), _bddPacket.getTcpSyn()),
        _bddOps.and(tcp, _bddPacket.getTcpAck()));
  }

  // Get TCP packets with special named ports, trying to find cases where only one side is
  // ephemeral.
  private List<BDD> computeTCPConstraints() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD tcp = _bddPacket.getIpProtocol().value(IpProtocol.TCP);

    BDD srcPortEphemeral = emphemeralPort(srcPort);
    BDD dstPortEphemeral = emphemeralPort(dstPort);

    return ImmutableList.<BDD>builder()
        // First, try to nudge src and dst port apart. E.g., if one is ephemeral the other is not.
        .add(_bddOps.and(tcp, srcPortEphemeral, dstPortEphemeral.not()))
        .add(_bddOps.and(tcp, srcPortEphemeral.not(), dstPortEphemeral))
        // Next, execute port preferences.
        .addAll(tcpPortPreferences(tcp, srcPort))
        .addAll(tcpPortPreferences(tcp, dstPort))
        // Next execute flag preferences.
        .addAll(tcpFlagPreferences(tcp))
        // Anything TCP.
        .add(tcp)
        .build();
  }

  private List<BDD> udpPortPreferences(BDD udp, BDDInteger tcpPort) {
    return ImmutableList.of(
        _bddOps.and(udp, tcpPort.value(NamedPort.DOMAIN.number())),
        _bddOps.and(udp, tcpPort.value(NamedPort.SNMP.number())),
        _bddOps.and(udp, tcpPort.value(NamedPort.SNMPTRAP.number())),
        // at least not zero if possible
        _bddOps.and(udp, tcpPort.value(0).not()));
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
  private List<BDD> computeUDPConstraints() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD udp = _bddPacket.getIpProtocol().value(IpProtocol.UDP);

    BDD srcPortEphemeral = emphemeralPort(srcPort);
    BDD dstPortEphemeral = emphemeralPort(dstPort);

    return ImmutableList.<BDD>builder()
        // Try for UDP traceroute.
        .add(_udpTraceroute)
        // Next, try to nudge src and dst port apart. E.g., if one is ephemeral the other is not.
        .add(_bddOps.and(udp, srcPortEphemeral, dstPortEphemeral.not()))
        .add(_bddOps.and(udp, srcPortEphemeral.not(), dstPortEphemeral))
        // Next, execute port preferences
        .addAll(udpPortPreferences(udp, srcPort))
        .addAll(udpPortPreferences(udp, dstPort))
        // Anything UDP.
        .add(udp)
        .build();
  }

  @VisibleForTesting
  static BDD isPrivateIp(IpSpaceToBDD ip) {
    return BDDOps.orNull(
        ip.toBDD(PRIVATE_SUBNET_10), ip.toBDD(PRIVATE_SUBNET_172), ip.toBDD(PRIVATE_SUBNET_192));
  }

  @VisibleForTesting
  static BDD isDocumentationIp(IpSpaceToBDD ip) {
    return BDDOps.orNull(
        ip.toBDD(RESERVED_DOCUMENTATION_192),
        ip.toBDD(RESERVED_DOCUMENTATION_198),
        ip.toBDD(RESERVED_DOCUMENTATION_203));
  }

  private static List<BDD> ipPreferences(BDDInteger ipInteger) {
    return ImmutableList.of(
        // First, one of the special IPs.
        ipInteger.value(Ip.parse("8.8.8.8").asLong()),
        ipInteger.value(Ip.parse("1.1.1.1").asLong()),
        // Next, at least don't start with 0.
        ipInteger.geq(Ip.parse("1.0.0.0").asLong()),
        // Next, try to be in class A.
        ipInteger.leq(Ip.parse("126.255.255.254").asLong()));
  }

  private List<BDD> computeIpConstraints() {
    BDD srcIpPrivate = isPrivateIp(_bddPacket.getSrcIpSpaceToBDD());
    BDD dstIpPrivate = isPrivateIp(_bddPacket.getDstIpSpaceToBDD());

    return ImmutableList.<BDD>builder()
        // 0. Try to not use documentation IPs if that is possible.
        .add(isDocumentationIp(_bddPacket.getSrcIpSpaceToBDD()).not())
        .add(isDocumentationIp(_bddPacket.getDstIpSpaceToBDD()).not())
        // First, try to nudge src and dst IP apart. E.g., if one is private the other should be
        // public.
        .add(_bddOps.and(srcIpPrivate, dstIpPrivate.not()))
        .add(_bddOps.and(srcIpPrivate.not(), dstIpPrivate))
        // Next, execute IP preferences
        .addAll(ipPreferences(_bddPacket.getSrcIp()))
        .addAll(ipPreferences(_bddPacket.getDstIp()))
        .build();
  }

  public List<BDD> generateFlowPreference(FlowPreference preference) {
    switch (preference) {
      case DEBUGGING:
        return ImmutableList.<BDD>builder()
            .addAll(_icmpConstraints)
            .addAll(_udpConstraints)
            .addAll(_tcpConstraints)
            .add(_defaultPacketLength)
            .addAll(_ipConstraints)
            .build();
      case APPLICATION:
      case TESTFILTER:
        return ImmutableList.<BDD>builder()
            .addAll(_tcpConstraints)
            .addAll(_udpConstraints)
            .addAll(_icmpConstraints)
            .add(_defaultPacketLength)
            .addAll(_ipConstraints)
            .build();
      case TRACEROUTE:
        return ImmutableList.<BDD>builder()
            .addAll(_udpConstraints)
            .addAll(_tcpConstraints)
            .addAll(_icmpConstraints)
            .add(_defaultPacketLength)
            .addAll(_ipConstraints)
            .build();
      default:
        throw new BatfishException("Not supported flow preference");
    }
  }
}
