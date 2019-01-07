package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link AssignIpAddressFromPool}. */
public class AssignIpAddressFromPoolTest {
  @Test
  public void testEquals() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    new EqualsTester()
        .addEqualityGroup(
            new AssignIpAddressFromPool(DEST_NAT, DESTINATION, ip1, ip2),
            new AssignIpAddressFromPool(DEST_NAT, DESTINATION, ip1, ip2))
        .addEqualityGroup(new AssignIpAddressFromPool(SOURCE_NAT, DESTINATION, ip1, ip2))
        .addEqualityGroup(new AssignIpAddressFromPool(DEST_NAT, SOURCE, ip1, ip2))
        .addEqualityGroup(new AssignIpAddressFromPool(DEST_NAT, DESTINATION, ip2, ip2))
        .addEqualityGroup(new AssignIpAddressFromPool(DEST_NAT, DESTINATION, ip1, ip1))
        .testEquals();
  }
}
