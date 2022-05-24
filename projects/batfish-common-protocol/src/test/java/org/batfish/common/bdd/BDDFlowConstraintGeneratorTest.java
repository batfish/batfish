package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_10;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_172;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_192;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.RefineAll.refineAll;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.RefineFirst.refineFirst;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.isPrivateIp;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.refine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class BDDFlowConstraintGeneratorTest {
  @Test
  public void testIsPrivateIp() {
    BDDPacket pkt = new BDDPacket();
    BDDOps ops = new BDDOps(pkt.getFactory());
    BDD isPrivateIp = isPrivateIp(ops, pkt.getDstIpSpaceToBDD());

    IpSpaceToBDD dstIp = pkt.getDstIpSpaceToBDD();
    BDD privateSubnet10 = dstIp.toBDD(PRIVATE_SUBNET_10);
    BDD privateSubnet172 = dstIp.toBDD(PRIVATE_SUBNET_172);
    BDD privateSubnet192 = dstIp.toBDD(PRIVATE_SUBNET_192);

    assertEquals(privateSubnet10.or(privateSubnet172).or(privateSubnet192), isPrivateIp);
  }

  @Test
  public void testRefineFirst() {
    BDDPacket pkt = new BDDPacket();
    BDDFactory factory = pkt.getFactory();

    IpSpaceToBDD dst = pkt.getDstIpSpaceToBDD();
    BDD p8 = dst.toBDD(Prefix.parse("1.0.0.0/8"));
    BDD p16 = dst.toBDD(Prefix.parse("1.1.0.0/16"));
    BDD port1 = pkt.getDstPort().value(1);
    BDD port2 = pkt.getDstPort().value(2);

    BDD ip1_1 = dst.toBDD(Ip.parse("1.1.1.1"));
    BDD ip1_2 = dst.toBDD(Ip.parse("1.2.2.2"));
    BDD ip2 = dst.toBDD(Ip.parse("2.2.2.2"));
    BDDIpProtocol ipProtocol = pkt.getIpProtocol();
    BDD tcp = ipProtocol.value(IpProtocol.TCP);

    BDDFlowConstraintGenerator.BddRefiner refiner =
        refineFirst(
            // guard
            tcp,
            // child refiners
            refine(p16.and(port1)),
            refine(p8.and(port2)));

    // p16 matches and takes precedence
    assertEquals(factory.andAll(tcp, ip1_1, port1), refiner.refine(ip1_1));

    // p8 matches
    assertEquals(factory.andAll(tcp, ip1_2, port2), refiner.refine(ip1_2));

    // neither child matches
    assertEquals(factory.andAll(tcp, ip2), refiner.refine(ip2));

    // guard doesn't match
    assertTrue(refiner.refine(ipProtocol.value(IpProtocol.ICMP)).isZero());
  }

  @Test
  public void testRefineAll() {
    BDDPacket pkt = new BDDPacket();
    BDDFactory factory = pkt.getFactory();
    BDDIpProtocol ipProtocol = pkt.getIpProtocol();
    BDD tcp = ipProtocol.value(IpProtocol.TCP);
    BDD syn = pkt.getTcpSyn();
    BDD notSyn = syn.not();
    BDD ack = pkt.getTcpAck();
    BDD notAck = ack.not();

    BDDFlowConstraintGenerator.BddRefiner refiner = refineAll(tcp, refine(syn), refine(ack));

    // both children match
    assertEquals(factory.andAll(tcp, syn, ack), refiner.refine(factory.one()));

    // only first child matches
    assertEquals(factory.andAll(tcp, syn, notAck), refiner.refine(notAck));

    // only second child matches
    assertEquals(factory.andAll(tcp, notSyn, ack), refiner.refine(notSyn));

    // neither child matches
    assertEquals(factory.andAll(tcp, notSyn, notAck), refiner.refine(notSyn.and(notAck)));

    // guard does not match
    assertTrue(refiner.refine(ipProtocol.value(IpProtocol.UDP)).isZero());
  }
}
