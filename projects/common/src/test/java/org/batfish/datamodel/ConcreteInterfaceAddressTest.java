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

/** Tests of {@link ConcreteInterfaceAddress} */
public class ConcreteInterfaceAddressTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    ConcreteInterfaceAddress cia = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    new EqualsTester()
        .addEqualityGroup(cia, cia, ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .addEqualityGroup(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
        .addEqualityGroup(ConcreteInterfaceAddress.parse("1.1.1.1/23"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testParse() {
    assertThat(
        ConcreteInterfaceAddress.parse("1.1.1.1/24"),
        equalTo(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24)));
  }

  @Test
  public void testTryParse() {
    assertThat(
        ConcreteInterfaceAddress.tryParse("1.1.1.1/24"),
        equalTo(Optional.of(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24))));
    assertThat(ConcreteInterfaceAddress.tryParse("garbade"), equalTo(Optional.empty()));
  }

  @Test
  public void testJavaSerialization() {
    ConcreteInterfaceAddress cia = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    assertThat(SerializationUtils.clone(cia), equalTo(cia));
  }

  @Test
  public void testJsonSerialization() {
    ConcreteInterfaceAddress cia = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    assertThat(BatfishObjectMapper.clone(cia, ConcreteInterfaceAddress.class), equalTo(cia));
  }

  @Test
  public void testInvalidNumBitsLow() {
    thrown.expect(IllegalArgumentException.class);
    ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 0);
  }

  @Test
  public void testInvalidNumBitsHigh() {
    thrown.expect(IllegalArgumentException.class);
    ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 33);
  }

  @Test
  public void testGetPrefix() {
    assertThat(
        ConcreteInterfaceAddress.parse("1.1.1.55/24").getPrefix(),
        equalTo(Prefix.parse("1.1.1.0/24")));
  }
}
