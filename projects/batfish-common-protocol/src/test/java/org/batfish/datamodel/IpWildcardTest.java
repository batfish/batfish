package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link IpWildcard}. */
public class IpWildcardTest {
  @Test
  public void testConstructionAndEquality() {
    new EqualsTester()
        .addEqualityGroup(
            IpWildcard.parse("1.2.3.4"),
            IpWildcard.parse("1.2.3.4"),
            IpWildcard.parse("1.2.3.4/32"),
            IpWildcard.ipWithWildcardMask(Ip.parse("1.2.3.4"), 0L),
            IpWildcard.parse("1.2.3.4:0.0.0.0"))
        .addEqualityGroup(
            IpWildcard.parse("1.2.3.4/8"),
            IpWildcard.parse("1.2.3.4/8"),
            IpWildcard.ipWithWildcardMask(Ip.parse("1.2.3.4"), 0x00FFFFFFL),
            IpWildcard.parse("1.2.3.4:0.255.255.255"))
        .addEqualityGroup(
            IpWildcard.create(Prefix.ZERO),
            IpWildcard.parse("0.0.0.0:255.255.255.255"),
            IpWildcard.parse("1.2.3.4:255.255.255.255"))
        .addEqualityGroup(
            IpWildcard.ipWithWildcardMask(Ip.parse("1.2.3.4"), 0xF0F0F0F0L),
            IpWildcard.ipWithWildcardMask(Ip.parse("65.66.67.68"), 0xF0F0F0F0L),
            IpWildcard.ipWithWildcardMask(Ip.parse("97.98.99.100"), 0xF0F0F0F0L),
            IpWildcard.ipWithWildcardMask(Ip.parse("129.130.131.132"), 0xF0F0F0F0L),
            IpWildcard.ipWithWildcardMask(Ip.parse("1.66.99.132"), 0xF0F0F0F0L))
        .testEquals();
  }

  @Test
  public void testContains() {
    IpSpace ipWildcard =
        IpWildcard.ipWithWildcardMask(Ip.create(0x01010001L), Ip.create(0x0000FF00L)).toIpSpace();
    assertThat(ipWildcard, containsIp(Ip.parse("1.1.1.1")));
    assertThat(ipWildcard, containsIp(Ip.parse("1.1.255.1")));
    assertThat(ipWildcard, not(containsIp(Ip.parse("1.1.0.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace =
        IpWildcard.ipWithWildcardMask(Ip.create(0x01010001L), Ip.create(0x0000FF00L))
            .toIpSpace()
            .complement();
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
    IpWildcard wc1 =
        IpWildcard.ipWithWildcardMask(Ip.create(0x00b0000aL), Ip.create(0x00FF00FFL).inverted());
    IpWildcard wc2 =
        IpWildcard.ipWithWildcardMask(Ip.create(0x000cd000L), Ip.create(0x0000FF00L).inverted());
    assertTrue("wildcards should overlap", wc1.intersects(wc2));
  }

  @Test
  public void testNotIntersects() {
    /*
     * Since the significant regions of wc1 and wc2 overlap and are not equal, there is no
     * intersection between them.
     */
    IpWildcard wc1 =
        IpWildcard.ipWithWildcardMask(Ip.create(0x00000F00L), Ip.create(0x0000FF00L).inverted());
    IpWildcard wc2 =
        IpWildcard.ipWithWildcardMask(Ip.create(0x0000F000L), Ip.create(0x0000FF00L).inverted());
    assertFalse(wc1.intersects(wc2));
  }

  @Test
  public void testSupersetOf() {
    IpWildcard wc1 = IpWildcard.parse("1.2.0.0/16");
    IpWildcard wc2 = IpWildcard.parse("1.2.3.0/24");

    assertTrue("IpWildcard.supersetOf should not be strict", wc1.supersetOf(wc1));

    assertTrue("wc1 should be a superset of wc2", wc1.supersetOf(wc2));
    assertFalse("wc2 should not be a superset of wc1", wc2.supersetOf(wc1));

    wc1 = IpWildcard.ipWithWildcardMask(Ip.create(0x12005600L), Ip.create(0xFF00FF00L).inverted());
    wc2 = IpWildcard.ipWithWildcardMask(Ip.create(0x12345600L), Ip.create(0xFFFFFF00L).inverted());
    assertTrue("wc1 should be a superset of wc2", wc1.supersetOf(wc2));
    assertFalse("wc2 should not be a superset of wc1", wc2.supersetOf(wc1));
  }

  @Test
  public void testSuperset_ANY() {
    assertTrue("ANY should be a superset of itself", IpWildcard.ANY.supersetOf(IpWildcard.ANY));
  }

  @Test
  public void testToString() {
    assertThat(IpWildcard.parse("1.2.3.4").toString(), equalTo("1.2.3.4"));
    assertThat(IpWildcard.parse("1.2.3.4/0").toString(), equalTo("0.0.0.0/0"));
    assertThat(IpWildcard.parse("1.2.3.4/8").toString(), equalTo("1.0.0.0/8"));
    assertThat(IpWildcard.parse("1.2.3.4/31").toString(), equalTo("1.2.3.4/31"));
    assertThat(IpWildcard.parse("1.2.3.4/32").toString(), equalTo("1.2.3.4"));
    assertThat(IpWildcard.parse("1.2.3.4:255.0.255.0").toString(), equalTo("0.2.0.4:255.0.255.0"));
  }
}
