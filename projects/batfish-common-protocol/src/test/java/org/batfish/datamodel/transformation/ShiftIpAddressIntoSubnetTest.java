package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link ShiftIpAddressIntoSubnet}. */
public class ShiftIpAddressIntoSubnetTest {
  @Test
  public void testEquals() {
    Prefix subnet1 = Prefix.parse("1.1.1.1/30");
    Prefix subnet2 = Prefix.parse("2.2.2.2/30");
    new EqualsTester()
        .addEqualityGroup(
            new ShiftIpAddressIntoSubnet(DESTINATION, subnet1),
            new ShiftIpAddressIntoSubnet(DESTINATION, subnet1))
        .addEqualityGroup(new ShiftIpAddressIntoSubnet(SOURCE, subnet1))
        .addEqualityGroup(new ShiftIpAddressIntoSubnet(DESTINATION, subnet2))
        .testEquals();
  }
}
