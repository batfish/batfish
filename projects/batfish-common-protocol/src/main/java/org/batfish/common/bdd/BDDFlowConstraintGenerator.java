package org.batfish.common.bdd;

import static org.batfish.datamodel.PacketHeaderConstraintsUtil.DEFAULT_PACKET_LENGTH;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.ip.Ip;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;

/** This class generates common useful flow constraints as BDDs. */
public final class BDDFlowConstraintGenerator {
  /**
   * Difference preferences of flow constraints: DEBUGGING: 1. ICMP 2. UDP 3. TCP APPLICATION : 1.
   * TCP 2. UDP 3. ICMP
   */
  public enum FlowPreference {
    DEBUGGING,
    APPLICATION,
    TESTFILTER
  }

  private final BDDPacket _bddPacket;
  private final BDDOps _bddOps;
  private final BDD _icmpFlow;
  private final BDD _udpFlow;
  private final BDD _tcpFlow;
  private final BDD _defaultPacketLength;
  private final List<BDD> _testFilterPrefBdds;

  BDDFlowConstraintGenerator(BDDPacket pkt) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("construct BDDFlowConstraintGenerator").startActive()) {
      assert span != null; // avoid unused warning
      _bddPacket = pkt;
      _bddOps = new BDDOps(pkt.getFactory());
      _defaultPacketLength = _bddPacket.getPacketLength().value(DEFAULT_PACKET_LENGTH);
      _icmpFlow = computeICMPConstraint();
      _udpFlow = computeUDPConstraint();
      _tcpFlow = computeTCPConstraint();
      _testFilterPrefBdds = computeTestFilterPreference();
    }
  }

  public BDD getUDPFlow() {
    return _udpFlow;
  }

  public BDD getTCPFlow() {
    return _tcpFlow;
  }

  public BDD getICMPFlow() {
    return _icmpFlow;
  }

  // Get ICMP echo request packets
  BDD computeICMPConstraint() {
    return _bddOps.and(
        _defaultPacketLength,
        _bddPacket.getIpProtocol().value(IpProtocol.ICMP),
        _bddPacket
            .getIcmpType()
            .value(IcmpType.ECHO_REQUEST)
            .and(_bddPacket.getIcmpCode().value(0)));
  }

  // Get TCP packets with names ports:
  // 1. Considers both directions of a TCP flow.
  // 2. Set src (dst, respectively) port to a ephemeral port, and dst (src, respectively) port to a
  // named port
  BDD computeTCPConstraint() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD bdd1 =
        _bddPacket
            .getFactory()
            .orAll(
                Arrays.stream(NamedPort.values())
                    .map(namedPort -> dstPort.value(namedPort.number()))
                    .collect(Collectors.toList()));
    bdd1 = bdd1.and(srcPort.geq(NamedPort.EPHEMERAL_LOWEST.number()));
    BDD bdd2 = _bddPacket.swapSourceAndDestinationFields(bdd1);
    BDD tcp = _bddPacket.getIpProtocol().value(IpProtocol.TCP);
    return _bddOps.and(_defaultPacketLength, tcp, bdd1.or(bdd2));
  }

  // Get UDP packets for traceroute:
  // 1. Considers both directions of a UDP flow.
  // 2. Set dst (src, respectively) port to the range 33434-33534 (common ports used by traceroute),
  // and src (dst, respectively) port to a ephemeral port
  BDD computeUDPConstraint() {
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDD bdd1 = dstPort.range(33434, 33534).and(srcPort.geq(NamedPort.EPHEMERAL_LOWEST.number()));
    BDD bdd2 = _bddPacket.swapSourceAndDestinationFields(bdd1);
    BDD udp = _bddPacket.getIpProtocol().value(IpProtocol.UDP);
    return _bddOps.and(_defaultPacketLength, udp, bdd1.or(bdd2));
  }

  private List<BDD> computeTestFilterPreference() {
    BDDInteger dstIp = _bddPacket.getDstIp();
    BDDInteger dstPort = _bddPacket.getDstPort();
    BDDInteger srcPort = _bddPacket.getSrcPort();
    BDDIpProtocol ipProtocol = _bddPacket.getIpProtocol();

    BDD defaultDstIpBdd = dstIp.value(Ip.parse("8.8.8.8").asLong());
    BDD tcpBdd = ipProtocol.value(IpProtocol.TCP);
    BDD udpBdd = ipProtocol.value(IpProtocol.UDP);
    BDD defaultSrcPortBdd = srcPort.value(NamedPort.EPHEMERAL_LOWEST.number());
    BDD defaultDstPortBdd = dstPort.value(NamedPort.HTTP.number());

    BDD one = _bddPacket.getFactory().one();
    // generate all combinations in order to enforce the following logic: when a field in the input
    // bdd contains the default value for that field, then use that value; otherwise use a value
    // in BDD of the field.
    Builder<BDD> builder = ImmutableList.builder();
    for (BDD dstIpBdd : ImmutableList.of(defaultDstIpBdd, one)) {
      for (BDD ipProtocolBdd : ImmutableList.of(tcpBdd, udpBdd)) {
        for (BDD srcPortBdd : ImmutableList.of(defaultSrcPortBdd, one)) {
          for (BDD dstPortBdd : ImmutableList.of(defaultDstPortBdd, one)) {
            builder.add(
                _bddOps.and(_defaultPacketLength, dstIpBdd, ipProtocolBdd, srcPortBdd, dstPortBdd));
          }
        }
      }
    }
    builder.add(defaultDstIpBdd);
    builder.add(_defaultPacketLength);
    return builder.build();
  }

  public List<BDD> generateFlowPreference(FlowPreference preference) {
    switch (preference) {
      case DEBUGGING:
        return ImmutableList.of(_icmpFlow, _udpFlow, _tcpFlow, _defaultPacketLength);
      case APPLICATION:
        return ImmutableList.of(_tcpFlow, _udpFlow, _icmpFlow, _defaultPacketLength);
      case TESTFILTER:
        return _testFilterPrefBdds;
      default:
        throw new BatfishException("Not supported flow preference");
    }
  }
}
