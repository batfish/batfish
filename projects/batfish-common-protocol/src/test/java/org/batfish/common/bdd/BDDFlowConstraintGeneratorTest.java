package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_10;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_172;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_192;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.isPrivateIp;
import static org.junit.Assert.assertEquals;

import net.sf.javabdd.BDD;
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
}
