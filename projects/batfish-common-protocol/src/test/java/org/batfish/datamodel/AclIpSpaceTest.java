package org.batfish.datamodel;

import static org.batfish.datamodel.AclIpSpace.intersection;
import static org.batfish.datamodel.AclIpSpace.union;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;

public class AclIpSpaceTest {
  /*
   * Permit everything in 1.1.0.0/16 except for 1.1.1.0/24.
   */
  private static final IpSpace _aclIpSpace =
      AclIpSpace.builder()
          .thenRejecting(Prefix.parse("1.1.1.0/24").toIpSpace())
          .thenPermitting(Prefix.parse("1.1.0.0/16").toIpSpace())
          .build();

  @Test
  public void testContainsIp() {
    assertThat(_aclIpSpace, not(containsIp(Ip.parse("1.1.1.0"))));
    assertThat(_aclIpSpace, containsIp(Ip.parse("1.1.0.0")));
    assertThat(_aclIpSpace, not(containsIp(Ip.parse("1.0.0.0"))));
  }

  @Test
  public void testComplement() {
    IpSpace notIpSpace = _aclIpSpace.complement();
    assertThat(notIpSpace, containsIp(Ip.parse("1.1.1.0")));
    assertThat(notIpSpace, not(containsIp(Ip.parse("1.1.0.0"))));
    assertThat(notIpSpace, containsIp(Ip.parse("1.0.0.0")));

    assertThat(notIpSpace.complement(), sameInstance(_aclIpSpace));
  }

  @Test
  public void testIntersection() {
    IpIpSpace ipSpace = Ip.parse("1.2.3.4").toIpSpace();
    assertThat(intersection(null, null), nullValue());
    assertThat(
        intersection(null, UniverseIpSpace.INSTANCE, null), equalTo(UniverseIpSpace.INSTANCE));
    assertThat(intersection(ipSpace, null, UniverseIpSpace.INSTANCE), equalTo(ipSpace));
    assertThat(intersection(EmptyIpSpace.INSTANCE, ipSpace), equalTo(EmptyIpSpace.INSTANCE));
  }

  @Test
  public void testUnion() {
    IpIpSpace ipSpace = Ip.parse("1.2.3.4").toIpSpace();
    assertThat(union(null, null), nullValue());
    assertThat(union(EmptyIpSpace.INSTANCE), equalTo(EmptyIpSpace.INSTANCE));
    assertThat(union(EmptyIpSpace.INSTANCE, ipSpace), equalTo(ipSpace));
    assertThat(union(UniverseIpSpace.INSTANCE, ipSpace), equalTo(UniverseIpSpace.INSTANCE));
    assertThat(union(ipSpace, UniverseIpSpace.INSTANCE), equalTo(UniverseIpSpace.INSTANCE));
  }

  @Test
  public void testUnionNested() {
    IpIpSpace a = Ip.parse("1.2.3.4").toIpSpace();
    IpIpSpace b = Ip.parse("1.2.3.5").toIpSpace();
    IpIpSpace c = Ip.parse("1.2.3.6").toIpSpace();
    assertThat(union(a, b, c), equalTo(union(union(a, b), c)));
    assertThat(union(a, b, c), equalTo(union(a, union(b, c))));
  }

  @Test
  public void testStopWhenEmpty() {
    IpSpace space =
        AclIpSpace.builder()
            .thenPermitting(Prefix.parse("1.2.3.4/32").toIpSpace())
            .thenRejecting(UniverseIpSpace.INSTANCE)
            .thenPermitting(Prefix.parse("2.0.0.0/8").toIpSpace())
            .build();

    IpSpace expected =
        AclIpSpace.builder()
            .thenPermitting(Prefix.parse("1.2.3.4/32").toIpSpace())
            .thenRejecting(UniverseIpSpace.INSTANCE)
            .build();
    assertThat(space, equalTo(expected));
  }
}
