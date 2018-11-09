package org.batfish.common.bdd;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
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
    APPLICATION
  }

  private BDDPacket _bddPacket;
  private BDD _icmpFlow;
  private BDD _udpFlow;
  private BDD _tcpFlow;

  BDDFlowConstraintGenerator(BDDPacket pkt) {
    _bddPacket = pkt;
    _icmpFlow = computeICMPConstraint();
    _udpFlow = computeUDPConstraint();
    _tcpFlow = computeTCPConstraint();
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
    return _bddPacket
        .getIpProtocol()
        .value(IpProtocol.ICMP.number())
        .and(
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
    BDD result = _bddPacket.getFactory().zero();
    for (NamedPort namedPort : NamedPort.values()) {
      result = result.or(_bddPacket.getDstPort().value(namedPort.number()));
    }
    result = _bddPacket.getSrcPort().geq(NamedPort.EPHEMERAL_LOWEST.number()).and(result);
    BDD result2 = _bddPacket.getFactory().zero();
    for (NamedPort namedPort : NamedPort.values()) {
      result2 = result2.or(_bddPacket.getSrcPort().value(namedPort.number()));
    }
    result2 = _bddPacket.getDstPort().geq(NamedPort.EPHEMERAL_LOWEST.number()).and(result);
    return _bddPacket.getIpProtocol().value(IpProtocol.TCP.number()).and(result.or(result2));
  }

  // Get UDP packets for traceroute:
  // 1. Considers both directions of a UDP flow.
  // 2. Set dst (src, respectively) port to the range 33434-33534 (common ports used by traceroute),
  // and src (dst, respectively) port to a ephemeral port
  BDD computeUDPConstraint() {
    BDD bdd1 =
        _bddPacket
            .getDstPort()
            .geq(33434)
            .and(_bddPacket.getDstPort().leq(33534))
            .and(_bddPacket.getSrcPort().geq(NamedPort.EPHEMERAL_LOWEST.number()));
    BDD bdd2 =
        _bddPacket
            .getSrcPort()
            .geq(33434)
            .and(_bddPacket.getSrcPort().leq(33534))
            .and(_bddPacket.getDstPort().geq(NamedPort.EPHEMERAL_LOWEST.number()));

    return _bddPacket.getIpProtocol().value(IpProtocol.UDP.number()).and(bdd1.or(bdd2));
  }

  public List<BDD> generateFlowPreference(FlowPreference preference) {
    switch (preference) {
      case DEBUGGING:
        return ImmutableList.of(_icmpFlow, _udpFlow, _tcpFlow);
      case APPLICATION:
        return ImmutableList.of(_tcpFlow, _udpFlow, _icmpFlow);
      default:
        throw new BatfishException("Not supported flow preference");
    }
  }
}
