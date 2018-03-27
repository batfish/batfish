package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class IpWildcardTest {

  @Test
  public void testContains() {
    IpWildcard ipWildcard = new IpWildcard(new Ip(0x01010001L), new Ip(0x0000FF00L));
    assertThat(ipWildcard, containsIp(new Ip("1.1.1.1")));
    assertThat(ipWildcard, containsIp(new Ip("1.1.255.1")));
    assertThat(ipWildcard, not(containsIp(new Ip("1.1.0.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace ipSpace = new IpWildcard(new Ip(0x01010001L), new Ip(0x0000FF00L)).complement();
    assertThat(ipSpace, not(containsIp(new Ip("1.1.1.1"))));
    assertThat(ipSpace, not(containsIp(new Ip("1.1.255.1"))));
    assertThat(ipSpace, containsIp(new Ip("1.1.0.0")));
  }

  @Test
  public void testIntersects() {
    IpWildcard wc1 = new IpWildcard(new Ip(0x00b0000aL), new Ip(0x00FF00FFL).inverted());
    IpWildcard wc2 = new IpWildcard(new Ip(0x000cd000L), new Ip(0x0000FF00L).inverted());
    assertThat("wildcards should overlap", wc1.intersects(wc2));
  }

  @Test
  public void testNotIntersects() {
    IpWildcard wc1 = new IpWildcard(new Ip(0x00000F00L), new Ip(0x0000FF00L).inverted());
    IpWildcard wc2 = new IpWildcard(new Ip(0x0000F000L), new Ip(0x0000FF00L).inverted());
    assertThat("wildcards should not overlap", !wc1.intersects(wc2));
  }
}
