package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDUtils.checkVariablesDisjointAndFree;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.sf.javabdd.BDD;
import org.junit.Test;

/** Test of {@link BDDUtils}. */
public final class BDDUtilsTest {
  @Test
  public void testCheckVariablesDisjointAndFree() {
    BDDPacket pkt = new BDDPacket();
    BDD srcIp = pkt.getSrcIp().getVars();
    BDD dstIp = pkt.getDstIp().getVars();
    BDD srcPort = pkt.getSrcPort().getVars();
    BDD srcIpAndPort = srcIp.and(srcPort);
    BDD srcAndDstIp = srcIp.and(dstIp);

    // equal sets
    assertFalse(checkVariablesDisjointAndFree(srcIp.id(), srcIp.id()));

    // disjoint sets
    assertTrue(checkVariablesDisjointAndFree(srcIp.id(), srcPort.id()));
    assertTrue(checkVariablesDisjointAndFree(srcPort.id(), srcIp.id()));

    // subset
    assertFalse(checkVariablesDisjointAndFree(srcPort.id(), srcIpAndPort.id()));
    assertFalse(checkVariablesDisjointAndFree(srcIpAndPort.id(), srcPort.id()));

    // non-empty intersection, but not a subset
    assertFalse(checkVariablesDisjointAndFree(srcIpAndPort.id(), srcAndDstIp.id()));
    assertFalse(checkVariablesDisjointAndFree(srcAndDstIp.id(), srcIpAndPort.id()));
  }
}
