package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class IpWildcardTest {
  @Test
  public void testConstructors() {
    assertThat(
        new IpWildcard(new Ip("1.2.3.4")), equalTo(new IpWildcard(new Ip(0x01020304L), Ip.ZERO)));

    assertThat(new IpWildcard("1.2.3.4"), equalTo(new IpWildcard(new Ip(0x01020304L), Ip.ZERO)));

    assertThat(new IpWildcard("1.2.3.4/8"), equalTo(new IpWildcard("1.0.0.0/8")));
  }

  @Test
  public void testContains() {
    IpSpace ipWildcard = new IpWildcard(new Ip(0x01010001L), new Ip(0x0000FF00L)).toIpSpace();
    assertThat(ipWildcard, containsIp(new Ip("1.1.1.1")));
    assertThat(ipWildcard, containsIp(new Ip("1.1.255.1")));
    assertThat(ipWildcard, not(containsIp(new Ip("1.1.0.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace =
        new IpWildcard(new Ip(0x01010001L), new Ip(0x0000FF00L)).toIpSpace().complement();
    assertThat(ipSpace, not(containsIp(new Ip("1.1.1.1"))));
    assertThat(ipSpace, not(containsIp(new Ip("1.1.255.1"))));
    assertThat(ipSpace, containsIp(new Ip("1.1.0.0")));
  }

  @Test
  public void testIntersects() {
    /*
     * The second Ip of an IpWildcard indicates which bits of the first Ip
     * are significant (i.e. not wild). In this example, since the significant bits of
     * wc1 and wc2 don't overlap, they should intersect (i.e. their bitwise OR is included in each).
     */
    IpWildcard wc1 = new IpWildcard(new Ip(0x00b0000aL), new Ip(0x00FF00FFL).inverted());
    IpWildcard wc2 = new IpWildcard(new Ip(0x000cd000L), new Ip(0x0000FF00L).inverted());
    assertThat("wildcards should overlap", wc1.intersects(wc2));
  }

  @Test
  public void testNotIntersects() {
    /*
     * Since the significant regions of wc1 and wc2 overlap and are not equal, there is no
     * intersection between them.
     */
    IpWildcard wc1 = new IpWildcard(new Ip(0x00000F00L), new Ip(0x0000FF00L).inverted());
    IpWildcard wc2 = new IpWildcard(new Ip(0x0000F000L), new Ip(0x0000FF00L).inverted());
    assertThat("wildcards should not overlap", !wc1.intersects(wc2));
  }

  @Test
  public void testSupersetOf() {
    IpWildcard wc1 = new IpWildcard("1.2.0.0/16");
    IpWildcard wc2 = new IpWildcard("1.2.3.0/24");

    assertThat("IpWildcard.supersetOf should not be strict", wc1.supersetOf(wc1));

    assertThat("wc1 should be a superset of wc2", wc1.supersetOf(wc2));
    assertThat("wc2 should not be a superset of wc1", !wc2.supersetOf(wc1));

    wc1 = new IpWildcard(new Ip(0x12005600L), new Ip(0xFF00FF00L).inverted());
    wc2 = new IpWildcard(new Ip(0x12345600L), new Ip(0xFFFFFF00L).inverted());
    assertThat("wc1 should be a superset of wc2", wc1.supersetOf(wc2));
    assertThat("wc2 should not be a superset of wc1", !wc2.supersetOf(wc1));
  }

  @Test
  public void testSuperset_ANY() {
    assertThat("ANY should be a superset of itself", IpWildcard.ANY.supersetOf(IpWildcard.ANY));
  }
}
