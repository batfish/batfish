package org.batfish.common.bdd;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;

public class BDDFlowConstraintGenerator {
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

  // Get TCP packets with names ports
  BDD computeTCPConstraint() {
    BDD result = _bddPacket.getFactory().zero();
    for (NamedPort namedPort : NamedPort.values()) {
      result = result.or(_bddPacket.getDstPort().value(namedPort.number()));
    }
    result = _bddPacket.getSrcPort().geq(1024).and(result);
    BDD result2 = _bddPacket.getFactory().zero();
    for (NamedPort namedPort : NamedPort.values()) {
      result2 = result2.or(_bddPacket.getSrcPort().value(namedPort.number()));
    }
    result2 = _bddPacket.getDstPort().geq(1024).and(result);
    return _bddPacket.getIpProtocol().value(IpProtocol.TCP.number()).and(result.or(result2));
  }

  // Get UDP packets for traceroute
  BDD computeUDPConstraint() {
    BDD bdd1 =
        _bddPacket
            .getDstPort()
            .geq(33434)
            .and(_bddPacket.getDstPort().leq(33534))
            .and(_bddPacket.getSrcPort().geq(1024));
    BDD bdd2 =
        _bddPacket
            .getSrcPort()
            .geq(33434)
            .and(_bddPacket.getSrcPort().leq(33534))
            .and(_bddPacket.getDstPort().geq(1024));

    return _bddPacket.getIpProtocol().value(IpProtocol.UDP.number()).and(bdd1.or(bdd2));
  }

  public List<BDD> generateFlowPreference(FlowPreference preference) {
    switch (preference) {
      case DEBUGGING:
        return ImmutableList.of(_icmpFlow, _udpFlow, _tcpFlow);
      case APPLICATION:
        return ImmutableList.of(_tcpFlow, _udpFlow, _icmpFlow);
      default:
        return ImmutableList.of();
    }
  }
}
