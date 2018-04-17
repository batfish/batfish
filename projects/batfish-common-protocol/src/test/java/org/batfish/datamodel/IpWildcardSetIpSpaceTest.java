package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class IpWildcardSetIpSpaceTest {
  private static final IpSpace ipSpace =
      IpWildcardSetIpSpace.builder()
          .including(
              new IpWildcard(Prefix.parse("1.1.1.0/24")),
              new IpWildcard(Prefix.parse("1.1.2.0/24")))
          .excluding(new IpWildcard(Prefix.parse("1.1.1.1/32")))
          .build();

  @Test
  public void testContainsIp() {
    assertThat(ipSpace, containsIp(new Ip("1.1.1.0")));
    assertThat(ipSpace, not(containsIp(new Ip("1.1.1.1"))));
    assertThat(ipSpace, containsIp(new Ip("1.1.2.0")));
    assertThat(ipSpace, not(containsIp(new Ip("1.1.3.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace notIpSpace = ipSpace.complement();
    assertThat(notIpSpace, not(containsIp(new Ip("1.1.1.0"))));
    assertThat(notIpSpace, containsIp(new Ip("1.1.1.1")));
    assertThat(notIpSpace, not(containsIp(new Ip("1.1.2.0"))));
    assertThat(notIpSpace, containsIp(new Ip("1.1.3.0")));
  }
}
