package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IpWildcardTest {
  @Test
  public void testConstructors() {
    assertThat(
        new IpWildcard(Ip.parse("1.2.3.4")),
        equalTo(new IpWildcard(Ip.create(0x01020304L), Ip.ZERO)));

    assertThat(new IpWildcard("1.2.3.4"), equalTo(new IpWildcard(Ip.create(0x01020304L), Ip.ZERO)));

    assertThat(new IpWildcard("1.2.3.4/8"), equalTo(new IpWildcard("1.0.0.0/8")));
  }

  @Test
  public void testContains() {
    IpSpace ipWildcard = new IpWildcard(Ip.create(0x01010001L), Ip.create(0x0000FF00L)).toIpSpace();
    assertThat(ipWildcard, containsIp(Ip.parse("1.1.1.1")));
    assertThat(ipWildcard, containsIp(Ip.parse("1.1.255.1")));
    assertThat(ipWildcard, not(containsIp(Ip.parse("1.1.0.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace =
        new IpWildcard(Ip.create(0x01010001L), Ip.create(0x0000FF00L)).toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(Ip.parse("1.1.1.1"))));
    assertThat(ipSpace, not(containsIp(Ip.parse("1.1.255.1"))));
    assertThat(ipSpace, containsIp(Ip.parse("1.1.0.0")));
  }

  @Test
  public void testIntersects() {
    /*
     * The second Ip of an IpWildcard indicates which bits of the first Ip
     * are significant (i.e. not wild). In this example, since the significant bits of
     * wc1 and wc2 don't overlap, they should intersect (i.e. their bitwise OR is included in each).
     */
    IpWildcard wc1 = new IpWildcard(Ip.create(0x00b0000aL), Ip.create(0x00FF00FFL).inverted());
    IpWildcard wc2 = new IpWildcard(Ip.create(0x000cd000L), Ip.create(0x0000FF00L).inverted());
    assertTrue("wildcards should overlap", wc1.intersects(wc2));
  }

  @Test
  public void testNotIntersects() {
    /*
     * Since the significant regions of wc1 and wc2 overlap and are not equal, there is no
     * intersection between them.
     */
    IpWildcard wc1 = new IpWildcard(Ip.create(0x00000F00L), Ip.create(0x0000FF00L).inverted());
    IpWildcard wc2 = new IpWildcard(Ip.create(0x0000F000L), Ip.create(0x0000FF00L).inverted());
    assertTrue("wildcards should not overlap", !wc1.intersects(wc2));
  }

  @Test
  public void testSupersetOf() {
    IpWildcard wc1 = new IpWildcard("1.2.0.0/16");
    IpWildcard wc2 = new IpWildcard("1.2.3.0/24");

    assertTrue("IpWildcard.supersetOf should not be strict", wc1.supersetOf(wc1));

    assertTrue("wc1 should be a superset of wc2", wc1.supersetOf(wc2));
    assertTrue("wc2 should not be a superset of wc1", !wc2.supersetOf(wc1));

    wc1 = new IpWildcard(Ip.create(0x12005600L), Ip.create(0xFF00FF00L).inverted());
    wc2 = new IpWildcard(Ip.create(0x12345600L), Ip.create(0xFFFFFF00L).inverted());
    assertTrue("wc1 should be a superset of wc2", wc1.supersetOf(wc2));
    assertTrue("wc2 should not be a superset of wc1", !wc2.supersetOf(wc1));
  }

  @Test
  public void testSuperset_ANY() {
    assertTrue("ANY should be a superset of itself", IpWildcard.ANY.supersetOf(IpWildcard.ANY));
  }
}
