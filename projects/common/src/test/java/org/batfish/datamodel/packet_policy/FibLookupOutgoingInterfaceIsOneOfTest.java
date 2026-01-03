package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link FibLookupOutgoingInterfaceIsOneOf} */
public class FibLookupOutgoingInterfaceIsOneOfTest {
  @Test
  public void testEquals() {
    FibLookupOutgoingInterfaceIsOneOf expr =
        new FibLookupOutgoingInterfaceIsOneOf(IngressInterfaceVrf.instance(), ImmutableSet.of());
    new EqualsTester()
        .addEqualityGroup(
            expr,
            expr,
            new FibLookupOutgoingInterfaceIsOneOf(
                IngressInterfaceVrf.instance(), ImmutableSet.of()))
        .addEqualityGroup(
            new FibLookupOutgoingInterfaceIsOneOf(new LiteralVrfName("foo"), ImmutableSet.of()))
        .addEqualityGroup(
            new FibLookupOutgoingInterfaceIsOneOf(
                IngressInterfaceVrf.instance(), ImmutableSet.of("Iface")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    FibLookupOutgoingInterfaceIsOneOf expr =
        new FibLookupOutgoingInterfaceIsOneOf(IngressInterfaceVrf.instance(), ImmutableSet.of());
    assertThat(SerializationUtils.clone(expr), equalTo(expr));
  }

  @Test
  public void testJsonSerialization() {
    FibLookupOutgoingInterfaceIsOneOf expr =
        new FibLookupOutgoingInterfaceIsOneOf(
            IngressInterfaceVrf.instance(), ImmutableSet.of("iface"));
    assertThat(
        BatfishObjectMapper.clone(expr, FibLookupOutgoingInterfaceIsOneOf.class), equalTo(expr));
  }
}
