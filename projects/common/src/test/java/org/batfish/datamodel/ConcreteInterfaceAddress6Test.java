package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link ConcreteInterfaceAddress6} */
public class ConcreteInterfaceAddress6Test {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    ConcreteInterfaceAddress6 cia = ConcreteInterfaceAddress6.parse("2001:db8::1/64");
    new EqualsTester()
        .addEqualityGroup(cia, cia, ConcreteInterfaceAddress6.parse("2001:db8::1/64"))
        .addEqualityGroup(ConcreteInterfaceAddress6.parse("2001:db8::2/64"))
        .addEqualityGroup(ConcreteInterfaceAddress6.parse("2001:db8::1/128"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testParse() {
    assertThat(
        ConcreteInterfaceAddress6.parse("2001:db8::1/64"),
        equalTo(ConcreteInterfaceAddress6.create(Ip6.parse("2001:db8::1"), 64)));
  }

  @Test
  public void testTryParse() {
    assertThat(
        ConcreteInterfaceAddress6.tryParse("2001:db8::1/64"),
        equalTo(Optional.of(ConcreteInterfaceAddress6.create(Ip6.parse("2001:db8::1"), 64))));
    assertThat(ConcreteInterfaceAddress6.tryParse("garbade"), equalTo(Optional.empty()));
  }

  @Test
  public void testJavaSerialization() {
    ConcreteInterfaceAddress6 cia = ConcreteInterfaceAddress6.parse("2001:db8::1/64");
    assertThat(SerializationUtils.clone(cia), equalTo(cia));
  }

  @Test
  public void testJsonSerialization() {
    ConcreteInterfaceAddress6 cia = ConcreteInterfaceAddress6.parse("2001:db8::1/64");
    assertThat(BatfishObjectMapper.clone(cia, ConcreteInterfaceAddress6.class), equalTo(cia));
  }

  @Test
  public void testInvalidNumBitsLow() {
    thrown.expect(IllegalArgumentException.class);
    ConcreteInterfaceAddress6.create(Ip6.parse("2001:db8::1"), 0);
  }

  @Test
  public void testInvalidNumBitsHigh() {
    thrown.expect(IllegalArgumentException.class);
    ConcreteInterfaceAddress6.create(Ip6.parse("2001:db8::1"), 129);
  }

  @Test
  public void testGetPrefix() {
    assertThat(
        ConcreteInterfaceAddress6.parse("2001:db8::55/64").getPrefix(),
        equalTo(Prefix6.parse("2001:db8::/64")));
  }
}
