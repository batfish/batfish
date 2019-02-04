package org.batfish.datamodel.ospf;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.junit.Test;

/** Tests of {@link OspfSessionProperties} */
public class OspfSessionPropertiesTest {
  @Test
  public void testEquals() {
    IpLink l1 = new IpLink(Ip.parse("1.1.1.2"), Ip.parse("1.1.1.3"));
    IpLink l2 = new IpLink(Ip.parse("1.1.1.3"), Ip.parse("1.1.1.4"));
    new EqualsTester()
        .addEqualityGroup(new OspfSessionProperties(l1), new OspfSessionProperties(l1))
        .addEqualityGroup(new OspfSessionProperties(l2))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
