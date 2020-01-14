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
          .setPacketLength(11)
          .build();
  private static final BDDPacket PACKET = new BDDPacket();

  /** Test that BASE_FLOW is converted to BDD correctly. */
  @Test
  public void testFlowHeadersToBddOspf() {
    BDD headerBdd = flowHeadersToBdd(BASE_FLOW, PACKET);
    assertThat(
        headerBdd.imp(PACKET.getSrcIp().value(Ip.FIRST_CLASS_A_PRIVATE_IP.asLong())), isOne());
    assertThat(
        headerBdd.imp(PACKET.getDstIp().value(Ip.FIRST_CLASS_B_PRIVATE_IP.asLong())), isOne());
    assertThat(headerBdd.imp(PACKET.getIpProtocol().value(IpProtocol.OSPF)), isOne());
    assertThat(headerBdd.imp(PACKET.getDscp().value(5)), isOne());
    assertThat(headerBdd.imp(PACKET.getEcn().value(3)), isOne());
    assertThat(headerBdd.imp(PACKET.getFragmentOffset().value(4)), isOne());
    // TODO: packet length is not modeled in BDD
    // Protocol-specific values are not constrained.
    assertThat(headerBdd.exist(PACKET.getSrcPort().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getDstPort().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getIcmpCode().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getIcmpType().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpAck()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpCwr()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpEce()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpFin()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpPsh()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpRst()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpSyn()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpUrg()), equalTo(headerBdd));
  }

  /** Test that ICMP packets are converted correctly. */
  @Test
  public void testFlowHeadersToBddIcmp() {
    Flow flow =
        BASE_FLOW.toBuilder().setIpProtocol(IpProtocol.ICMP).setIcmpCode(5).setIcmpType(7).build();
    BDD headerBdd = flowHeadersToBdd(flow, PACKET);
    assertThat(headerBdd.imp(PACKET.getIpProtocol().value(IpProtocol.ICMP)), isOne());
    assertThat(headerBdd.imp(PACKET.getIcmpCode().value(5)), isOne());
    assertThat(headerBdd.imp(PACKET.getIcmpType().value(7)), isOne());
    // TCP/UDP fields are not constrained
    assertThat(headerBdd.exist(PACKET.getSrcPort().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getDstPort().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpAck()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpCwr()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpEce()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpFin()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpPsh()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpRst()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpSyn()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpUrg()), equalTo(headerBdd));
  }

  @Test
  public void testFlowHeadersToBddTcp() {
    Flow flow =
        BASE_FLOW
            .toBuilder()
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(22)
            .setDstPort(55)
            .setTcpFlags(TcpFlags.builder().setAck(true).setSyn(true).build())
            .build();
    BDD headerBdd = flowHeadersToBdd(flow, PACKET);
    assertThat(headerBdd.imp(PACKET.getIpProtocol().value(IpProtocol.TCP)), isOne());
    assertThat(headerBdd.imp(PACKET.getSrcPort().value(22)), isOne());
    assertThat(headerBdd.imp(PACKET.getDstPort().value(55)), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpAck()), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpCwr().not()), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpEce().not()), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpFin().not()), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpPsh().not()), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpRst().not()), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpSyn()), isOne());
    assertThat(headerBdd.imp(PACKET.getTcpUrg().not()), isOne());
    // ICMP code and type are not constrained
    assertThat(headerBdd.exist(PACKET.getIcmpCode().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getIcmpType().getBDDInteger().getVars()), equalTo(headerBdd));
  }

  @Test
  public void testFlowHeadersToBddUdp() {
    Flow flow =
        BASE_FLOW.toBuilder().setIpProtocol(IpProtocol.UDP).setSrcPort(22).setDstPort(55).build();
    BDD headerBdd = flowHeadersToBdd(flow, PACKET);
    assertThat(headerBdd.imp(PACKET.getIpProtocol().value(IpProtocol.UDP)), isOne());
    assertThat(headerBdd.imp(PACKET.getSrcPort().value(22)), isOne());
    assertThat(headerBdd.imp(PACKET.getDstPort().value(55)), isOne());
    // ICMP code and type, TCP flags are not constrained
    assertThat(headerBdd.exist(PACKET.getIcmpCode().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getIcmpType().getBDDInteger().getVars()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpAck()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpCwr()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpEce()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpFin()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpPsh()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpRst()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpSyn()), equalTo(headerBdd));
    assertThat(headerBdd.exist(PACKET.getTcpUrg()), equalTo(headerBdd));
  }
}
