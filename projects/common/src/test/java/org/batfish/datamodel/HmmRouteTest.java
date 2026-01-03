package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link HmmRoute}. */
public final class HmmRouteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testJavaSerialization() {
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
  public void testJacksonSerialization() {
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
  public void testEquals() {
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
  public void testWrongNextHopType() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("NextHop type must be NextHopInterface, but was: NextHopDiscard");
    HmmRoute.builder()
        .setNetwork(Prefix.strict("192.0.2.1/32"))
        .setNextHop(NextHopDiscard.instance())
        .build();
  }

  @Test
  public void testInvalidNextHopInterface() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Cannot set non-null next-hop IP for HmmRoute");
    HmmRoute.builder()
        .setNetwork(Prefix.strict("192.0.2.1/32"))
        .setNextHop(NextHopInterface.of("foo", Ip.parse("1.1.1.1")))
        .build();
  }

  @Test
  public void testInvalidPrefixLength() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid prefix length for HmmRoute: 0");
    HmmRoute.builder().setNetwork(Prefix.ZERO).setNextHop(NextHopInterface.of("foo")).build();
  }

  @Test
  public void testMissingNetwork() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Missing network");
    HmmRoute.builder().setNextHop(NextHopInterface.of("foo")).build();
  }

  @Test
  public void testMissingNextHop() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Missing nextHop");
    HmmRoute.builder().setNetwork(Prefix.strict("192.0.2.1/32")).build();
  }
}
