package org.batfish.datamodel.ospf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

/** Test of {@link OspfAddresses}. */
public final class OspfAddressesTest {
  @Test
  public void testJsonSerialization() {
    OspfAddresses obj =
        OspfAddresses.of(ImmutableList.of(ConcreteInterfaceAddress.parse("1.1.1.1/32")));

    // test (de)serialization
    assertThat(obj, equalTo(BatfishObjectMapper.clone(obj, OspfAddresses.class)));
  }

  @Test
  public void testJavaSerialization() {
    OspfAddresses obj =
        OspfAddresses.of(ImmutableList.of(ConcreteInterfaceAddress.parse("1.1.1.1/32")));

    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            OspfAddresses.of(ImmutableList.of()), OspfAddresses.of(ImmutableList.of()))
        .addEqualityGroup(
            OspfAddresses.of(ImmutableList.of(ConcreteInterfaceAddress.parse("1.1.1.1/32"))))
        .testEquals();
  }
}
