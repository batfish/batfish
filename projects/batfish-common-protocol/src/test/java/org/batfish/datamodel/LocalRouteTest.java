package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link LocalRoute} */
public class LocalRouteTest {
  @Test
  public void testJavaSerialization() {
    LocalRoute lr = new LocalRoute(ConcreteInterfaceAddress.parse("1.1.1.1/24"), "Ethernet0");
    assertThat(SerializationUtils.clone(lr), equalTo(lr));
  }

  @Test
  public void testJsonSerialization() {
    LocalRoute lr = new LocalRoute(ConcreteInterfaceAddress.parse("1.1.1.1/24"), "Ethernet0");
    assertThat(BatfishObjectMapper.clone(lr, LocalRoute.class), equalTo(lr));
  }

  @Test
  public void testToBuilder() {
    LocalRoute lr = new LocalRoute(ConcreteInterfaceAddress.parse("1.1.1.1/24"), "Ethernet0");
    assertThat(lr, equalTo(lr.toBuilder().build()));
  }

  @Test
  public void testEquals() {
    LocalRoute.Builder lr =
        LocalRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setSourcePrefixLength(24)
            .setNextHopInterface("Ethernet0");
    new EqualsTester()
        /*
         * Note: connected routes by definition are routing and forwarding so setting these values in the
         * builder has no effect
         */
        .addEqualityGroup(
            new LocalRoute(ConcreteInterfaceAddress.parse("1.1.1.1/24"), "Ethernet0"),
            lr.build(),
            lr.setNonRouting(true).build(),
            lr.setNonForwarding(false).build())
        .addEqualityGroup(new LocalRoute(ConcreteInterfaceAddress.parse("1.1.2.1/24"), "Ethernet0"))
        .addEqualityGroup(
            new LocalRoute(ConcreteInterfaceAddress.parse("1.1.2.1/24"), "Ethernet1"),
            new LocalRoute(Prefix.parse("1.1.2.1/32"), "Ethernet1", 24, 0, Route.UNSET_ROUTE_TAG))
        .addEqualityGroup(
            new LocalRoute(Prefix.parse("1.1.2.1/32"), "Ethernet1", 24, 123, Route.UNSET_ROUTE_TAG))
        .addEqualityGroup(
            new LocalRoute(Prefix.parse("1.1.2.1/32"), "Ethernet1", 25, 123, Route.UNSET_ROUTE_TAG))
        .addEqualityGroup(new LocalRoute(Prefix.parse("1.1.2.1/32"), "Ethernet1", 25, 123, 1L))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
