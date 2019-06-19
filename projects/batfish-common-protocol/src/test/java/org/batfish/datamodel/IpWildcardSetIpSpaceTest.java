package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IpWildcardSetIpSpaceTest {
  private static final IpSpace ipSpace =
      IpWildcardSetIpSpace.builder()
          .including(IpWildcard.parse("1.1.1.0/24"), IpWildcard.parse("1.1.2.0/24"))
          .excluding(IpWildcard.parse("1.1.1.1/32"))
          .build();

  @Test
  public void testContainsIp() {
    assertThat(ipSpace, containsIp(Ip.parse("1.1.1.0")));
    assertThat(ipSpace, not(containsIp(Ip.parse("1.1.1.1"))));
    assertThat(ipSpace, containsIp(Ip.parse("1.1.2.0")));
    assertThat(ipSpace, not(containsIp(Ip.parse("1.1.3.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace notIpSpace = ipSpace.complement();
    assertThat(notIpSpace, not(containsIp(Ip.parse("1.1.1.0"))));
    assertThat(notIpSpace, containsIp(Ip.parse("1.1.1.1")));
    assertThat(notIpSpace, not(containsIp(Ip.parse("1.1.2.0"))));
    assertThat(notIpSpace, containsIp(Ip.parse("1.1.3.0")));
  }
}
