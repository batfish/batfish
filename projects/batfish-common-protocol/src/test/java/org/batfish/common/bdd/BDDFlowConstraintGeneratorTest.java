package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_10;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_172;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.PRIVATE_SUBNET_192;
import static org.batfish.common.bdd.BDDFlowConstraintGenerator.isPrivateIp;
import static org.junit.Assert.assertEquals;

import net.sf.javabdd.BDD;
import org.junit.Test;

public class BDDFlowConstraintGeneratorTest {
  private static BDDPacket PKT = new BDDPacket();

  @Test
  public void testIsPrivateIp() {
    BDD isPrivateIp = isPrivateIp(PKT.getDstIpSpaceToBDD());

    IpSpaceToBDD dstIp = PKT.getDstIpSpaceToBDD();
    BDD privateSubnet10 = dstIp.toBDD(PRIVATE_SUBNET_10);
    BDD privateSubnet172 = dstIp.toBDD(PRIVATE_SUBNET_172);
    BDD privateSubnet192 = dstIp.toBDD(PRIVATE_SUBNET_192);

    assertEquals(privateSubnet10.or(privateSubnet172).or(privateSubnet192), isPrivateIp);
  }
}
