package org.batfish.common.bdd;

import static org.batfish.datamodel.HeaderSpace.DEFAULT_PACKET_LENGTH;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstPort;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIcmpCode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIcmpType;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIpProtocol;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcPort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.TcpFlags;
import org.junit.Test;

public class BDDPacketTest {
  @Test
  public void testAllocateBDDBit() {
    BDDPacket pkt = new BDDPacket();
    int varNum = pkt.getFactory().varNum();
    BDD bdd = pkt.allocateBDDBit("foo");
    assertThat(bdd, notNullValue());
    assertThat(pkt.getFactory().varNum(), equalTo(varNum + 1));
  }

  @Test
  public void testAllocateBDDBit_beforePacketVars() {
    BDDPacket pkt = new BDDPacket();
    BDD bdd = pkt.allocateBDDBit("foo", true);
    assertThat(bdd, notNullValue());
    assertThat(bdd.var(), lessThan(pkt.getDstIp().value(0).var()));
  }

  @Test
  public void testAllocateBDDInteger() {
    BDDPacket pkt = new BDDPacket();
    int varNum = pkt.getFactory().varNum();
    BDDInteger var = pkt.allocateBDDInteger("foo", 5);
    assertThat(var, notNullValue());
    assertThat(pkt.getFactory().varNum(), equalTo(varNum + 5));
  }

  @Test
  public void testAllocateBDDInteger_beforePacketVars() {
    BDDPacket pkt = new BDDPacket();
    BDDInteger var = pkt.allocateBDDInteger("foo", 5, true);
    assertThat(var, notNullValue());
    assertThat(var.value(0).var(), lessThan(pkt.getDstIp().value(0).var()));
  }

  @Test
  public void testGetFlow_empty() {
    BDDPacket pkt = new BDDPacket();
    assertThat(pkt.getFlow(pkt.getFactory().zero()), equalTo(Optional.empty()));
  }

  @Test
  public void testGetFlow_not_sane() {
    BDDPacket pkt = new BDDPacket();
    BDDPacketLength length = pkt.getPacketLength();
    BDDIpProtocol protocol = pkt.getIpProtocol();
    BDDInteger srcPort = pkt.getSrcPort();
    BDDInteger dstPort = pkt.getDstPort();
    assertThat(pkt.getFlow(length.value(19)), equalTo(Optional.empty()));
    assertThat(
        pkt.getFlow(length.value(27).and(protocol.value(IpProtocol.UDP))),
        equalTo(Optional.empty()));
    assertThat(
        pkt.getFlow(length.value(39).and(protocol.value(IpProtocol.TCP))),
        equalTo(Optional.empty()));
    assertThat(
        pkt.getFlow(length.value(63).and(protocol.value(IpProtocol.ICMP))),
        equalTo(Optional.empty()));
    assertThat(
        pkt.getFlow(srcPort.value(0).and(protocol.value(IpProtocol.TCP))),
        equalTo(Optional.empty()));
    assertThat(
        pkt.getFlow(dstPort.value(0).and(protocol.value(IpProtocol.TCP))),
        equalTo(Optional.empty()));
    assertThat(
        pkt.getFlow(srcPort.value(0).and(protocol.value(IpProtocol.UDP))),
        equalTo(Optional.empty()));
    assertThat(
        pkt.getFlow(dstPort.value(0).and(protocol.value(IpProtocol.UDP))),
        equalTo(Optional.empty()));
  }

  @Test
  public void testGetFlow_ICMP() {
    BDDPacket pkt = new BDDPacket();
    Ip dstIp = Ip.parse("123.45.78.0");
    Ip srcIp = Ip.parse("255.255.255.255");

    int icmpCode = 0x00;
    int icmpType = 0x01;
    IpProtocol ipProtocol = IpProtocol.ICMP;

    BDD bdd =
        pkt.getDstIp()
            .value(dstIp.asLong())
            .and(pkt.getSrcIp().value(srcIp.asLong()))
            .and(pkt.getIpProtocol().value(ipProtocol))
            .and(pkt.getIcmpCode().value(icmpCode))
            .and(pkt.getIcmpType().value(icmpType))
            .randomFullSatOne(7654321);

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(dstIp));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasIpProtocol(ipProtocol));
    assertThat(flow, hasIcmpCode(icmpCode));
    assertThat(flow, hasIcmpType(icmpType));
    assertThat(flow.getDstPort(), nullValue());
    assertThat(flow.getSrcPort(), nullValue());
    assertThat(flow.getTcpFlags(), nullValue());
  }

  @Test
  public void testGetFlow_TCP() {
    BDDPacket pkt = new BDDPacket();
    Ip dstIp = Ip.parse("123.45.78.0");
    int dstPort = 0xA0;

    Ip srcIp = Ip.parse("255.255.255.255");
    int srcPort = 0xFF;

    IpProtocol ipProtocol = IpProtocol.TCP;

    BDD bdd =
        pkt.getDstIp()
            .value(dstIp.asLong())
            .and(pkt.getSrcIp().value(srcIp.asLong()))
            .and(pkt.getIpProtocol().value(ipProtocol))
            .and(pkt.getDstPort().value(dstPort))
            .and(pkt.getSrcPort().value(srcPort))
            .and(pkt.getTcpAck().not())
            .and(pkt.getTcpCwr())
            .and(pkt.getTcpEce().not())
            .and(pkt.getTcpFin())
            .and(pkt.getTcpPsh().not())
            .and(pkt.getTcpRst())
            .and(pkt.getTcpSyn().not())
            .and(pkt.getTcpUrg())
            .randomFullSatOne(7654321);

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(dstIp));
    assertThat(flow, hasDstPort(dstPort));
    assertThat(flow, hasIpProtocol(ipProtocol));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasSrcPort(srcPort));
    assertThat(
        flow.getTcpFlags(),
        equalTo(new TcpFlags(false, true, false, true, false, true, false, true)));
    assertThat(flow.getIcmpType(), nullValue());
    assertThat(flow.getIcmpCode(), nullValue());
  }

  @Test
  public void testGetFlow_UDP() {
    BDDPacket pkt = new BDDPacket();
    Ip dstIp = Ip.parse("123.45.78.0");
    int dstPort = 0xA0;

    Ip srcIp = Ip.parse("255.255.255.255");
    int srcPort = 0xFF;

    IpProtocol ipProtocol = IpProtocol.UDP;

    BDD bdd =
        pkt.getDstIp()
            .value(dstIp.asLong())
            .and(pkt.getSrcIp().value(srcIp.asLong()))
            .and(pkt.getIpProtocol().value(ipProtocol))
            .and(pkt.getDstPort().value(dstPort))
            .and(pkt.getSrcPort().value(srcPort))
            .randomFullSatOne(7654321);

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(dstIp));
    assertThat(flow, hasDstPort(dstPort));
    assertThat(flow, hasIpProtocol(ipProtocol));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasSrcPort(srcPort));
    assertThat(flow.getIcmpType(), nullValue());
    assertThat(flow.getIcmpCode(), nullValue());
    assertThat(flow.getTcpFlags(), nullValue());
  }

  @Test
  public void testGetFlow_packetLength() {
    BDDPacket pkt = new BDDPacket();

    // getFlow prefers DEFAULT_PACKET_LENGTH
    assertEquals(
        DEFAULT_PACKET_LENGTH, pkt.getFlow(pkt.getFactory().one()).get().getPacketLength());

    BDD bdd = pkt.getPacketLength().value(50);
    assertEquals(50, pkt.getFlow(bdd).get().getPacketLength());
  }

  @Test
  public void testGetFlowPreference1() {
    BDDPacket pkt = new BDDPacket();
    Ip dstIp = Ip.parse("123.45.78.0");
    Ip srcIp = Ip.parse("1.2.3.4");

    BDD bdd = pkt.getDstIp().value(dstIp.asLong()).and(pkt.getSrcIp().value(srcIp.asLong()));

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(dstIp));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasIpProtocol(IpProtocol.ICMP));
    assertThat(flow, hasIcmpType(8));
    assertThat(flow, hasIcmpCode(0));
  }

  @Test
  public void testGetFlowPreference2() {
    BDDPacket pkt = new BDDPacket();
    Ip dstIp = Ip.parse("123.45.78.0");
    Ip srcIp = Ip.parse("1.2.3.4");

    BDD bdd = pkt.getDstIp().value(dstIp.asLong()).and(pkt.getSrcIp().value(srcIp.asLong()));

    BDD icmpbdd = pkt.getIpProtocol().value(IpProtocol.ICMP);

    bdd = bdd.and(icmpbdd.not());

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(dstIp));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasIpProtocol(IpProtocol.UDP));
  }

  @Test
  public void testGetFlowPreference3() {
    BDDPacket pkt = new BDDPacket();
    Ip dstIp = Ip.parse("123.45.78.0");
    Ip srcIp = Ip.parse("1.2.3.4");

    BDD bdd = pkt.getDstIp().value(dstIp.asLong()).and(pkt.getSrcIp().value(srcIp.asLong()));

    BDD icmpbdd = pkt.getIpProtocol().value(IpProtocol.ICMP);

    BDD udpbdd = pkt.getIpProtocol().value(IpProtocol.UDP);

    bdd = bdd.and(icmpbdd.not()).and(udpbdd.not());

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(dstIp));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasIpProtocol(IpProtocol.TCP));
    assertThat(flow, not(hasDstPort(0)));
    assertThat(flow, not(hasSrcPort(0)));
  }

  @Test
  public void testGetFlowPreference_preferenceApplication() {
    BDDPacket pkt = new BDDPacket();
    Ip dstIp = Ip.parse("123.45.78.0");
    Ip srcIp = Ip.parse("1.2.3.4");

    BDD bdd = pkt.getDstIp().value(dstIp.asLong()).and(pkt.getSrcIp().value(srcIp.asLong()));

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd, FlowPreference.APPLICATION);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(dstIp));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasIpProtocol(IpProtocol.TCP));
    assertThat(flow, hasDstPort(80));
    assertThat(flow, hasSrcPort(NamedPort.EPHEMERAL_LOWEST.number()));
  }

  @Test
  public void testGetFlowPreference_preferenceTestFilter() {
    BDDPacket pkt = new BDDPacket();
    Ip srcIp = Ip.parse("1.2.3.4");

    BDD bdd = pkt.getSrcIp().value(srcIp.asLong());

    Optional<Flow.Builder> flowBuilder = pkt.getFlow(bdd, FlowPreference.TESTFILTER);
    assertTrue("Unsat", flowBuilder.isPresent());
    Flow flow = flowBuilder.get().setIngressNode("ingressNode").build();

    assertThat(flow, hasDstIp(Ip.parse("10.0.0.0")));
    assertThat(flow, hasSrcIp(srcIp));
    assertThat(flow, hasIpProtocol(IpProtocol.TCP));
    assertThat(flow, hasDstPort(80));
    assertThat(flow, hasSrcPort(NamedPort.EPHEMERAL_LOWEST.number()));
  }

  @Test
  public void testSwapSourceAndDestinationFields() {
    BDDPacket pkt = new BDDPacket();
    BDDInteger dstIp = pkt.getDstIp();
    BDDInteger srcIp = pkt.getSrcIp();
    BDDInteger dstPort = pkt.getDstPort();
    BDDInteger srcPort = pkt.getSrcPort();

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");
    Ip ip4 = Ip.parse("4.4.4.4");

    BiFunction<BDDInteger, BDDInteger, Function<BDDInteger, BDD>> mkBdd =
        (ipVar1, ipVar2) ->
            portVar ->
                (ipVar1.value(ip1.asLong()).and(ipVar2.value(ip2.asLong())).and(portVar.value(0)))
                    .or(
                        ipVar1
                            .value(ip3.asLong())
                            .and(ipVar2.value(ip4.asLong()))
                            .and(ipVar1.value(1)));

    BDD orig = mkBdd.apply(dstIp, srcIp).apply(dstPort);
    BDD swapped = mkBdd.apply(srcIp, dstIp).apply(srcPort);
    assertThat(pkt.swapSourceAndDestinationFields(orig), equalTo(swapped));
  }

  @Test
  public void testGetRepresentativeFlowIgnoresIrrelevantBits() {
    BDDPacket pkt = new BDDPacket();
    // A random flow that cannot have ports, icmp type/code, or tcp flags.
    BDD randomAssignment = pkt.getIpProtocol().value(IpProtocol.OSPF).randomFullSatOne(76451234);
    Flow flow =
        pkt.getFlow(randomAssignment)
            .map(fb -> fb.setIngressNode("n").setIngressVrf("v").build())
            .orElse(null);
    assertThat(flow, notNullValue());
    assertThat(flow.getDstPort(), nullValue());
    assertThat(flow.getSrcPort(), nullValue());
    assertThat(flow.getIcmpType(), nullValue());
    assertThat(flow.getIcmpCode(), nullValue());
    assertThat(flow.getTcpFlags(), nullValue());
  }
}
