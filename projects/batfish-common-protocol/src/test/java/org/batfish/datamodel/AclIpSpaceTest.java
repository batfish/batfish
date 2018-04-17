package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class AclIpSpaceTest {
  /*
   * Permit everything in 1.1.0.0/16 except for 1.1.1.0/24.
   */
  private static final AclIpSpace _aclIpSpace =
      AclIpSpace.builder()
          .thenRejecting(Prefix.parse("1.1.1.0/24"))
          .thenPermitting(Prefix.parse("1.1.0.0/16"))
          .build();

  @Test
  public void testContainsIp() {
    assertThat(_aclIpSpace, not(containsIp(new Ip("1.1.1.0"))));
    assertThat(_aclIpSpace, containsIp(new Ip("1.1.0.0")));
    assertThat(_aclIpSpace, not(containsIp(new Ip("1.0.0.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace notIpSpace = _aclIpSpace.complement();
    assertThat(notIpSpace, containsIp(new Ip("1.1.1.0")));
    assertThat(notIpSpace, not(containsIp(new Ip("1.1.0.0"))));
    assertThat(notIpSpace, containsIp(new Ip("1.0.0.0")));
  }
}
