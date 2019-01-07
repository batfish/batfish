package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Prefix;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ShiftIpAddressIntoSubnet}. */
public class ShiftIpAddressIntoSubnetTest {
  /** */
  @Rule public ExpectedException _exception = ExpectedException.none();

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

  @Test
  public void testPrefixLength() {
    // /31 subnets and shorter are allowed
    new ShiftIpAddressIntoSubnet(DESTINATION, Prefix.parse("1.1.1.1/0"));
    new ShiftIpAddressIntoSubnet(DESTINATION, Prefix.parse("1.1.1.1/10"));
    new ShiftIpAddressIntoSubnet(DESTINATION, Prefix.parse("1.1.1.1/31"));

    // /32 subnets are not allowed
    _exception.expect(IllegalArgumentException.class);
    new ShiftIpAddressIntoSubnet(DESTINATION, Prefix.parse("1.1.1.1/32"));
  }
}
