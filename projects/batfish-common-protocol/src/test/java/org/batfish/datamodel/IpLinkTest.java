package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link IpLink}. */
public final class IpLinkTest {

  @Test
  public void testEquals() {
    IpLink link1 = new IpLink(Ip.ZERO, Ip.ZERO);
    IpLink link2 = new IpLink(Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP);
    IpLink link3 = new IpLink(Ip.FIRST_CLASS_B_PRIVATE_IP, Ip.FIRST_CLASS_A_PRIVATE_IP);

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(link1, link1, new IpLink(Ip.ZERO, Ip.ZERO))
        .addEqualityGroup(link2)
        .addEqualityGroup(link3)
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    IpLink link = new IpLink(Ip.ZERO, Ip.ZERO);

    assertEquals(link, BatfishObjectMapper.clone(link, IpLink.class));
  }
}
