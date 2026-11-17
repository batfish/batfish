package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.junit.jupiter.api.Test;

/** Test of {@link HmmRoute}. */
final class HmmRouteTest {

  @Test
  void testJavaSerialization() {
    HmmRoute obj =
        HmmRoute.builder()
            .setNetwork(Prefix.strict("192.0.2.1/32"))
            .setNextHop(NextHopInterface.of("foo"))
            .setAdmin(100)
            .setTag(1000L)
            .build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testJacksonSerialization() {
    HmmRoute obj =
        HmmRoute.builder()
            .setNetwork(Prefix.strict("192.0.2.1/32"))
            .setNextHop(NextHopInterface.of("foo"))
            .setAdmin(100)
            .setTag(1000L)
            .build();
    assertEquals(obj, BatfishObjectMapper.clone(obj, AbstractRoute.class));
  }

  @Test
  void testEquals() {
    HmmRoute obj =
        HmmRoute.builder()
            .setNetwork(Prefix.strict("192.0.2.1/32"))
            .setNextHop(NextHopInterface.of("foo"))
            .build();
    new EqualsTester()
        .addEqualityGroup(obj, obj.toBuilder().build())
        .addEqualityGroup(obj.toBuilder().setNetwork(Prefix.strict("192.0.2.2/32")).build())
        .addEqualityGroup(obj.toBuilder().setNextHop(NextHopInterface.of("bar")).build())
        .addEqualityGroup(obj.toBuilder().setAdmin(5).build())
        .addEqualityGroup(obj.toBuilder().setTag(6L).build())
        .testEquals();
  }

  @Test
  void testWrongNextHopType() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                HmmRoute.builder()
                    .setNetwork(Prefix.strict("192.0.2.1/32"))
                    .setNextHop(NextHopDiscard.instance())
                    .build());
    assertThat(
        exception.getMessage(),
        containsString("NextHop type must be NextHopInterface, but was: NextHopDiscard"));
  }

  @Test
  void testInvalidNextHopInterface() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                HmmRoute.builder()
                    .setNetwork(Prefix.strict("192.0.2.1/32"))
                    .setNextHop(NextHopInterface.of("foo", Ip.parse("1.1.1.1")))
                    .build());
    assertThat(
        exception.getMessage(), containsString("Cannot set non-null next-hop IP for HmmRoute"));
  }

  @Test
  void testInvalidPrefixLength() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                HmmRoute.builder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHop(NextHopInterface.of("foo"))
                    .build());
    assertThat(exception.getMessage(), containsString("Invalid prefix length for HmmRoute: 0"));
  }

  @Test
  void testMissingNetwork() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> HmmRoute.builder().setNextHop(NextHopInterface.of("foo")).build());
    assertThat(exception.getMessage(), containsString("Missing network"));
  }

  @Test
  void testMissingNextHop() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> HmmRoute.builder().setNetwork(Prefix.strict("192.0.2.1/32")).build());
    assertThat(exception.getMessage(), containsString("Missing nextHop"));
  }
}
