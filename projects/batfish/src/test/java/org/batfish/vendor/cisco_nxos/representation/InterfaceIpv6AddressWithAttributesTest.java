package org.batfish.vendor.cisco_nxos.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip6;
import org.junit.Test;

/** Test of {@link InterfaceIpv6AddressWithAttributes}. */
public final class InterfaceIpv6AddressWithAttributesTest {

  @Test
  public void testJavaSerialization() {
    InterfaceIpv6AddressWithAttributes obj =
        new InterfaceIpv6AddressWithAttributes(Ip6.parse("1::2"), 64);
    obj.setTag(123L);
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    Ip6 address6 = Ip6.parse("1::2");
    int prefixLength = 64;
    InterfaceIpv6AddressWithAttributes a =
        new InterfaceIpv6AddressWithAttributes(address6, prefixLength);
    InterfaceIpv6AddressWithAttributes b =
        new InterfaceIpv6AddressWithAttributes(address6, prefixLength);
    b.setTag(123L);
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(a, a, new InterfaceIpv6AddressWithAttributes(address6, prefixLength))
        .addEqualityGroup(new InterfaceIpv6AddressWithAttributes(Ip6.parse("1::3"), prefixLength))
        .addEqualityGroup(new InterfaceIpv6AddressWithAttributes(Ip6.parse("1::3"), 65))
        .addEqualityGroup(b)
        .testEquals();
  }
}
