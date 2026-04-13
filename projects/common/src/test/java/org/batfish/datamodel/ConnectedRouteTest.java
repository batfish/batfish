package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link ConnectedRoute} */
public class ConnectedRouteTest {
  @Test
  public void testJavaSerialization() {
    ConnectedRoute cr = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0", 3, 4L);
    assertThat(SerializationUtils.clone(cr), equalTo(cr));
  }

  @Test
  public void testJsonSerialization() {
    ConnectedRoute cr = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0", 3, 4L);
    assertThat(BatfishObjectMapper.clone(cr, ConnectedRoute.class), equalTo(cr));
  }

  @Test
  public void testToBuilder() {
    ConnectedRoute cr = new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Ethernet0", 3, 4L);
    assertThat(cr, equalTo(cr.toBuilder().build()));
  }

  @Test
  public void testEquals() {
    ConnectedRoute.Builder cr =
        ConnectedRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("Ethernet0");
    new EqualsTester()
        /*
         * Note: connected routes by definition are routing and forwarding so setting these values in the
         * builder has no effect
         */
        .addEqualityGroup(
            cr.build(),
            cr.build(),
            cr.setNonRouting(true).build(),
            cr.setNonForwarding(true).build())
        .addEqualityGroup(new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet0"))
        .addEqualityGroup(new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet1"))
        .addEqualityGroup(new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet1", 123))
        .addEqualityGroup(new ConnectedRoute(Prefix.parse("1.1.2.0/24"), "Ethernet1", 123, 5L))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  /** admin and tag are stored as unsigned 32-bit ints. Test boundary values round-trip. */
  @Test
  public void testUnsignedAdminAndTagRoundTrip() {
    // MAX values (0xFFFFFFFF as unsigned int)
    ConnectedRoute maxRoute =
        new ConnectedRoute(
            Prefix.parse("1.0.0.0/8"),
            "Ethernet0",
            AbstractRoute.MAX_ADMIN_DISTANCE,
            AbstractRoute.MAX_TAG);
    assertThat(maxRoute.getAdministrativeCost(), equalTo(AbstractRoute.MAX_ADMIN_DISTANCE));
    assertThat(maxRoute.getTag(), equalTo(AbstractRoute.MAX_TAG));

    // toBuilder round-trip preserves MAX values
    assertThat(maxRoute.toBuilder().build(), equalTo(maxRoute));

    // JSON round-trip preserves MAX values
    assertThat(BatfishObjectMapper.clone(maxRoute, ConnectedRoute.class), equalTo(maxRoute));

    // Java serialization round-trip preserves MAX values
    assertThat(SerializationUtils.clone(maxRoute), equalTo(maxRoute));

    // Large unsigned values (> Integer.MAX_VALUE but < MAX)
    long largeAdmin = 0x80000000L; // 2147483648
    long largeTag = 0xFFFFFFFEL; // 4294967294
    ConnectedRoute largeRoute =
        new ConnectedRoute(Prefix.parse("2.0.0.0/8"), "Ethernet0", largeAdmin, largeTag);
    assertThat(largeRoute.getAdministrativeCost(), equalTo(largeAdmin));
    assertThat(largeRoute.getTag(), equalTo(largeTag));
    assertThat(largeRoute.toBuilder().build(), equalTo(largeRoute));
  }

  /** MAX_TAG and UNSET_ROUTE_TAG must be distinguishable despite both mapping to int -1. */
  @Test
  public void testMaxTagNotEqualToUnsetTag() {
    ConnectedRoute withMaxTag =
        new ConnectedRoute(Prefix.parse("1.0.0.0/8"), "Ethernet0", 0, AbstractRoute.MAX_TAG);
    ConnectedRoute withUnsetTag = new ConnectedRoute(Prefix.parse("1.0.0.0/8"), "Ethernet0", 0);

    assertThat(withMaxTag.getTag(), equalTo(AbstractRoute.MAX_TAG));
    assertThat(withUnsetTag.getTag(), equalTo(Route.UNSET_ROUTE_TAG));
    assertThat(
        "MAX_TAG and UNSET_ROUTE_TAG must be distinguishable",
        withMaxTag,
        not(equalTo(withUnsetTag)));
  }

  /** Tags outside the valid u32 range should be rejected. */
  @Test
  public void testRejectsOutOfRangeTag() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ConnectedRoute(Prefix.parse("1.0.0.0/8"), "Ethernet0", 0, 1L << 32));
  }
}
