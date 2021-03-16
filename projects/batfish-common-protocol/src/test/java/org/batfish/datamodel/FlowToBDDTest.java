package org.batfish.datamodel;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.batfish.datamodel.FlowToBDD.flowHeadersToBdd;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.junit.Test;

public class FlowToBDDTest {
  private static final Flow BASE_FLOW =
      Flow.builder()
          .setIngressNode("node")
          .setSrcIp(Ip.FIRST_CLASS_A_PRIVATE_IP)
          .setDstIp(Ip.FIRST_CLASS_B_PRIVATE_IP)
          .setIpProtocol(IpProtocol.OSPF)
          .setDscp(5)
          .setEcn(3)
          .setFragmentOffset(4)
          .setPacketLength(71)
          .build();
  private final BDDPacket _packet = new BDDPacket();

  /** Test that BASE_FLOW is converted to BDD correctly. */
  @Test
  public void testFlowHeadersToBddOspf() {
    BDD headerBdd = flowHeadersToBdd(BASE_FLOW, _packet);
    assertThat(
        headerBdd.imp(_packet.getSrcIp().value(Ip.FIRST_CLASS_A_PRIVATE_IP.asLong())), isOne());
    assertThat(
        headerBdd.imp(_packet.getDstIp().value(Ip.FIRST_CLASS_B_PRIVATE_IP.asLong())), isOne());
    assertThat(headerBdd.imp(_packet.getIpProtocol().value(IpProtocol.OSPF)), isOne());
    assertThat(headerBdd.imp(_packet.getDscp().value(5)), isOne());
    assertThat(headerBdd.imp(_packet.getEcn().value(3)), isOne());
    assertThat(headerBdd.imp(_packet.getFragmentOffset().value(4)), isOne());
    // TODO: packet length is not modeled in BDD
    // Protocol-specific values are not constrained.
    assertThat(headerBdd.exist(_packet.getSrcPort().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getDstPort().getVars()), equalTo(headerBdd));
    assertThat(
        headerBdd.exist(_packet.getIcmpCode().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(
        headerBdd.exist(_packet.getIcmpType().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpAck()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpCwr()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpEce()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpFin()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpPsh()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpRst()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpSyn()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpUrg()), equalTo(headerBdd));
  }

  /** Test that ICMP packets are converted correctly. */
  @Test
  public void testFlowHeadersToBddIcmp() {
    Flow flow =
        BASE_FLOW.toBuilder().setIpProtocol(IpProtocol.ICMP).setIcmpCode(5).setIcmpType(7).build();
    BDD headerBdd = flowHeadersToBdd(flow, _packet);
    assertThat(headerBdd.imp(_packet.getIpProtocol().value(IpProtocol.ICMP)), isOne());
    assertThat(headerBdd.imp(_packet.getIcmpCode().value(5)), isOne());
    assertThat(headerBdd.imp(_packet.getIcmpType().value(7)), isOne());
    // TCP/UDP fields are not constrained
    assertThat(headerBdd.exist(_packet.getSrcPort().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getDstPort().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpAck()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpCwr()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpEce()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpFin()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpPsh()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpRst()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpSyn()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpUrg()), equalTo(headerBdd));
  }

  @Test
  public void testFlowHeadersToBddTcp() {
    Flow flow =
        BASE_FLOW.toBuilder()
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(22)
            .setDstPort(55)
            .setTcpFlags(TcpFlags.builder().setAck(true).setSyn(true).build())
            .build();
    BDD headerBdd = flowHeadersToBdd(flow, _packet);
    assertThat(headerBdd.imp(_packet.getIpProtocol().value(IpProtocol.TCP)), isOne());
    assertThat(headerBdd.imp(_packet.getSrcPort().value(22)), isOne());
    assertThat(headerBdd.imp(_packet.getDstPort().value(55)), isOne());
    assertThat(headerBdd.imp(_packet.getTcpAck()), isOne());
    assertThat(headerBdd.imp(_packet.getTcpCwr().not()), isOne());
    assertThat(headerBdd.imp(_packet.getTcpEce().not()), isOne());
    assertThat(headerBdd.imp(_packet.getTcpFin().not()), isOne());
    assertThat(headerBdd.imp(_packet.getTcpPsh().not()), isOne());
    assertThat(headerBdd.imp(_packet.getTcpRst().not()), isOne());
    assertThat(headerBdd.imp(_packet.getTcpSyn()), isOne());
    assertThat(headerBdd.imp(_packet.getTcpUrg().not()), isOne());
    // ICMP code and type are not constrained
    assertThat(
        headerBdd.exist(_packet.getIcmpCode().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(
        headerBdd.exist(_packet.getIcmpType().getBDDInteger().getVars()), equalTo(headerBdd));
  }

  @Test
  public void testFlowHeadersToBddUdp() {
    Flow flow =
        BASE_FLOW.toBuilder().setIpProtocol(IpProtocol.UDP).setSrcPort(22).setDstPort(55).build();
    BDD headerBdd = flowHeadersToBdd(flow, _packet);
    assertThat(headerBdd.imp(_packet.getIpProtocol().value(IpProtocol.UDP)), isOne());
    assertThat(headerBdd.imp(_packet.getSrcPort().value(22)), isOne());
    assertThat(headerBdd.imp(_packet.getDstPort().value(55)), isOne());
    // ICMP code and type, TCP flags are not constrained
    assertThat(
        headerBdd.exist(_packet.getIcmpCode().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(
        headerBdd.exist(_packet.getIcmpType().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpAck()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpCwr()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpEce()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpFin()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpPsh()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpRst()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpSyn()), equalTo(headerBdd));
    assertThat(headerBdd.exist(_packet.getTcpUrg()), equalTo(headerBdd));
  }
}
