package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link FibLookupOutgoingInterfaceMatchesOneOf} */
public class FibLookupOutgoingInterfaceMatchesOneOfTest {
  @Test
  public void testEquals() {
    FibLookupOutgoingInterfaceMatchesOneOf expr =
        new FibLookupOutgoingInterfaceMatchesOneOf(
            IngressInterfaceVrf.instance(), ImmutableSet.of());
    new EqualsTester()
        .addEqualityGroup(
            expr,
            expr,
            new FibLookupOutgoingInterfaceMatchesOneOf(
                IngressInterfaceVrf.instance(), ImmutableSet.of()))
        .addEqualityGroup(
            new FibLookupOutgoingInterfaceMatchesOneOf(
                new LiteralVrfName("foo"), ImmutableSet.of()))
        .addEqualityGroup(
            new FibLookupOutgoingInterfaceMatchesOneOf(
                IngressInterfaceVrf.instance(), ImmutableSet.of("Iface")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    FibLookupOutgoingInterfaceMatchesOneOf expr =
        new FibLookupOutgoingInterfaceMatchesOneOf(
            IngressInterfaceVrf.instance(), ImmutableSet.of());
    assertThat(SerializationUtils.clone(expr), equalTo(expr));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    FibLookupOutgoingInterfaceMatchesOneOf expr =
        new FibLookupOutgoingInterfaceMatchesOneOf(
            IngressInterfaceVrf.instance(), ImmutableSet.of());
    assertThat(
        BatfishObjectMapper.clone(expr, FibLookupOutgoingInterfaceMatchesOneOf.class),
        equalTo(expr));
  }
}
