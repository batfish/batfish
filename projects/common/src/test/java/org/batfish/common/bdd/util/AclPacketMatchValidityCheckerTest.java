package org.batfish.common.bdd.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

public class AclPacketMatchValidityCheckerTest {
  @Test
  public void testAclHasPacketInvariants() {
    BDDPacket pkt = new BDDPacket();
    AclPacketMatchValidityChecker checker =
        new AclPacketMatchValidityChecker(
            pkt,
            new IpAccessListToBddImpl(
                pkt,
                BDDSourceManager.empty(pkt),
                new HeaderSpaceToBDD(pkt, Collections.emptyMap()),
                Collections.emptyMap()));
    // The fullSat functions constrain every bit, so violate invariants.
    assertFalse(checker.aclMeetsPacketMatchInvariants(pkt.getFactory().one().fullSatOne()));
    assertFalse(
        checker.aclMeetsPacketMatchInvariants(pkt.getFactory().one().randomFullSatOne(7654321)));

    BDD icmp = pkt.getIpProtocol().value(IpProtocol.ICMP);
    BDD tcp = pkt.getIpProtocol().value(IpProtocol.TCP);
    BDD udp = pkt.getIpProtocol().value(IpProtocol.UDP);
    BDD ospf = pkt.getIpProtocol().value(IpProtocol.OSPF);
    BDD code5 = pkt.getIcmpCode().value(5);
    BDD type6 = pkt.getIcmpType().value(6);
    BDD dport7 = pkt.getDstPort().value(7);
    BDD sport8 = pkt.getSrcPort().value(8);
    BDD tcpFlag = pkt.getTcpAck();

    // Some valid packets
    assertTrue(checker.aclMeetsPacketMatchInvariants(icmp));
    assertTrue(checker.aclMeetsPacketMatchInvariants(icmp.and(type6).and(code5)));
    assertTrue(checker.aclMeetsPacketMatchInvariants(tcp));
    assertTrue(checker.aclMeetsPacketMatchInvariants(tcp.and(dport7).and(sport8).and(tcpFlag)));
    assertTrue(checker.aclMeetsPacketMatchInvariants(udp));
    assertTrue(checker.aclMeetsPacketMatchInvariants(udp.and(dport7).and(sport8)));
    assertTrue(checker.aclMeetsPacketMatchInvariants(ospf));

    // Some invalid packets
    assertFalse(checker.aclMeetsPacketMatchInvariants(icmp.and(dport7)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(icmp.and(sport8)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(icmp.and(tcpFlag)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(udp.and(code5)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(udp.and(type6)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(udp.and(tcpFlag)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(tcp.and(code5)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(tcp.and(type6)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(ospf.and(code5)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(ospf.and(type6)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(ospf.and(dport7)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(ospf.and(sport8)));
    assertFalse(checker.aclMeetsPacketMatchInvariants(ospf.and(tcpFlag)));
  }
}
