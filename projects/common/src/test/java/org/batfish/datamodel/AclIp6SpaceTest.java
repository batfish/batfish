package org.batfish.datamodel;

import static org.batfish.datamodel.AclIp6Space.difference;
import static org.batfish.datamodel.AclIp6Space.intersection;
import static org.batfish.datamodel.AclIp6Space.union;
import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class AclIp6SpaceTest {
  /*
   * Permit everything in 2001:db8::/32 except for 2001:db8:1::/48.
   */
  private static final Ip6Space _aclIp6Space =
      AclIp6Space.builder()
          .thenRejecting(Prefix6.parse("2001:db8:1::/48").toIp6Space())
          .thenPermitting(Prefix6.parse("2001:db8::/32").toIp6Space())
          .build();

  @Test
  public void testContainsIp() {
    assertThat(_aclIp6Space, not(containsIp6(Ip6.parse("2001:db8:1::1"))));
    assertThat(_aclIp6Space, containsIp6(Ip6.parse("2001:db8:2::1")));
    assertThat(_aclIp6Space, not(containsIp6(Ip6.parse("2001:db9::1"))));
  }

  @Test
  public void testComplement() {
    Ip6Space notIp6Space = _aclIp6Space.complement();
    assertThat(notIp6Space, containsIp6(Ip6.parse("2001:db8:1::1")));
    assertThat(notIp6Space, not(containsIp6(Ip6.parse("2001:db8:2::1"))));
    assertThat(notIp6Space, containsIp6(Ip6.parse("2001:db9::1")));
    assertThat(notIp6Space.complement(), equalTo(_aclIp6Space));
  }

  @Test
  public void testIntersection() {
    Ip6Ip6Space ip6Space = Ip6Ip6Space.create(Ip6.parse("2001:db8:2::1"));
    assertThat(intersection(null, null), nullValue());
    assertThat(
        intersection(null, UniverseIp6Space.INSTANCE, null), equalTo(UniverseIp6Space.INSTANCE));
    assertThat(intersection(ip6Space, null, UniverseIp6Space.INSTANCE), equalTo(ip6Space));
    assertThat(intersection(EmptyIp6Space.INSTANCE, ip6Space), equalTo(EmptyIp6Space.INSTANCE));
  }

  @Test
  public void testUnion() {
    Ip6Ip6Space ip6Space = Ip6Ip6Space.create(Ip6.parse("2001:db8:2::1"));
    assertThat(union(null, null), nullValue());
    assertThat(union(EmptyIp6Space.INSTANCE), equalTo(EmptyIp6Space.INSTANCE));
    assertThat(union(EmptyIp6Space.INSTANCE, ip6Space), equalTo(ip6Space));
    assertThat(union(UniverseIp6Space.INSTANCE, ip6Space), equalTo(UniverseIp6Space.INSTANCE));
    assertThat(union(ip6Space, UniverseIp6Space.INSTANCE), equalTo(UniverseIp6Space.INSTANCE));
  }

  @Test
  public void testUnionNested() {
    Ip6Ip6Space a = Ip6Ip6Space.create(Ip6.parse("2001:db8::1"));
    Ip6Ip6Space b = Ip6Ip6Space.create(Ip6.parse("2001:db8::2"));
    Ip6Ip6Space c = Ip6Ip6Space.create(Ip6.parse("2001:db8::3"));
    assertThat(union(a, b, c), equalTo(union(union(a, b), c)));
    assertThat(union(a, b, c), equalTo(union(a, union(b, c))));
  }

  @Test
  public void testDifference() {
    Ip6Ip6Space ip6Space = Ip6Ip6Space.create(Ip6.parse("2001:db8::1"));

    // Test null cases
    assertThat(difference(null, null), nullValue());
    assertThat(difference(ip6Space, null), equalTo(ip6Space));

    // Test with EmptyIp6Space
    assertThat(difference(EmptyIp6Space.INSTANCE, ip6Space), equalTo(EmptyIp6Space.INSTANCE));
    assertThat(difference(ip6Space, EmptyIp6Space.INSTANCE), equalTo(ip6Space));

    // Test with specific IPs
    Ip6Ip6Space ip1 = Ip6Ip6Space.create(Ip6.parse("2001:db8::1"));
    Ip6Ip6Space ip2 = Ip6Ip6Space.create(Ip6.parse("2001:db8::2"));
    Ip6Space diff = difference(ip1, ip2);

    assertThat(diff, containsIp6(Ip6.parse("2001:db8::1")));
    assertThat(diff, not(containsIp6(Ip6.parse("2001:db8::2"))));
  }

  @Test
  public void testStopWhenEmpty() {
    Ip6Space space =
        AclIp6Space.builder()
            .thenPermitting(Prefix6.parse("2001:db8::1/128").toIp6Space())
            .thenRejecting(UniverseIp6Space.INSTANCE)
            .thenPermitting(Prefix6.parse("2001:db9::/32").toIp6Space())
            .build();

    Ip6Space expected =
        AclIp6Space.builder()
            .thenPermitting(Prefix6.parse("2001:db8::1/128").toIp6Space())
            .thenRejecting(UniverseIp6Space.INSTANCE)
            .build();
    assertThat(space, equalTo(expected));
  }

  @Test
  public void testLineEvaluationOrder() {
    // Create an ACL that permits 2001:db8::1 but denies 2001:db8::/32
    Ip6Space space =
        AclIp6Space.builder()
            .thenPermitting(Ip6Ip6Space.create(Ip6.parse("2001:db8::1")))
            .thenRejecting(Prefix6.parse("2001:db8::/32").toIp6Space())
            .build();

    // The first matching line should determine the result
    assertThat(space, containsIp6(Ip6.parse("2001:db8::1"))); // First line permits this
    assertThat(space, not(containsIp6(Ip6.parse("2001:db8::2")))); // Second line denies this
    assertThat(space, not(containsIp6(Ip6.parse("2001:db9::1")))); // No line matches this
  }

  @Test
  public void testOf() {
    AclIp6SpaceLine line1 = AclIp6SpaceLine.permit(Ip6Ip6Space.create(Ip6.parse("2001:db8::1")));
    AclIp6SpaceLine line2 = AclIp6SpaceLine.reject(Prefix6.parse("2001:db8::/32").toIp6Space());

    Ip6Space space = AclIp6Space.of(ImmutableList.of(line1, line2));

    assertThat(space, containsIp6(Ip6.parse("2001:db8::1")));
    assertThat(space, not(containsIp6(Ip6.parse("2001:db8::2"))));
  }

  @Test
  public void testSerialization() {
    Ip6Space original =
        AclIp6Space.builder()
            .thenPermitting(Ip6Ip6Space.create(Ip6.parse("2001:db8::1")))
            .thenRejecting(Prefix6.parse("2001:db8::/32").toIp6Space())
            .build();
    assertThat(BatfishObjectMapper.clone(original, Ip6Space.class), equalTo(original));
    assertThat(SerializationUtils.clone(original), equalTo(original));
  }

  @Test
  public void testEquals() {
    Ip6Ip6Space ip1 = Ip6Ip6Space.create(Ip6.parse("2001:db8::1"));
    Ip6Ip6Space ip2 = Ip6Ip6Space.create(Ip6.parse("2001:db8::2"));
    PrefixIp6Space prefix1 = new PrefixIp6Space(Prefix6.parse("2001:db8::/32"));

    new EqualsTester()
        .addEqualityGroup(
            AclIp6Space.builder().thenPermitting(ip1).build(),
            AclIp6Space.builder().thenPermitting(ip1).build())
        .addEqualityGroup(AclIp6Space.builder().thenPermitting(ip2).build())
        .addEqualityGroup(AclIp6Space.builder().thenRejecting(ip1).build())
        .addEqualityGroup(AclIp6Space.builder().thenPermitting(prefix1).thenRejecting(ip1).build())
        .testEquals();
  }
}
