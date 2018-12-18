package org.batfish.datamodel;

import static org.batfish.datamodel.AclIpSpace.intersection;
import static org.batfish.datamodel.AclIpSpace.union;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public class AclIpSpaceTest {
  /*
   * Permit everything in 1.1.0.0/16 except for 1.1.1.0/24.
   */
  private static final AclIpSpace _aclIpSpace =
      AclIpSpace.builder()
          .thenRejecting(Prefix.parse("1.1.1.0/24").toIpSpace())
          .thenPermitting(Prefix.parse("1.1.0.0/16").toIpSpace())
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

  @Test
  public void testIntersection() {
    IpIpSpace ipSpace = new Ip("1.2.3.4").toIpSpace();
    assertThat(intersection(null, null), nullValue());
    assertThat(
        intersection(null, UniverseIpSpace.INSTANCE, null), equalTo(UniverseIpSpace.INSTANCE));
    assertThat(intersection(ipSpace, null, UniverseIpSpace.INSTANCE), equalTo(ipSpace));
    assertThat(intersection(EmptyIpSpace.INSTANCE, ipSpace), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testUnion() {
    IpIpSpace ipSpace = new Ip("1.2.3.4").toIpSpace();
    assertThat(union(null, null), nullValue());
    assertThat(union(EmptyIpSpace.INSTANCE), equalTo(EmptyIpSpace.INSTANCE));
    assertThat(union(EmptyIpSpace.INSTANCE, ipSpace), equalTo(ipSpace));
  }

  @Test
  public void testStopWhenEmpty() {
    AclIpSpace space =
        AclIpSpace.builder()
            .thenPermitting(Prefix.parse("1.2.3.4/32").toIpSpace())
            .thenRejecting(UniverseIpSpace.INSTANCE)
            .thenPermitting(Prefix.parse("2.0.0.0/8").toIpSpace())
            .build();

    AclIpSpace expected =
        AclIpSpace.builder()
            .thenPermitting(Prefix.parse("1.2.3.4/32").toIpSpace())
            .thenRejecting(UniverseIpSpace.INSTANCE)
            .build();
    assertThat(space, equalTo(expected));
  }
}
