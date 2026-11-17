package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.RouteDistinguisher.from;
import static org.batfish.datamodel.bgp.RouteDistinguisher.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link RouteDistinguisher} */
class RouteDistinguisherTest {

  @Test
  void testEquals() {
    RouteDistinguisher rd = RouteDistinguisher.from(0L, 1);
    new EqualsTester()
        .addEqualityGroup(rd, rd, RouteDistinguisher.from(0L, 1))
        .addEqualityGroup(RouteDistinguisher.from(0, 1L))
        .addEqualityGroup(RouteDistinguisher.from(Ip.ZERO, 1))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(RouteDistinguisher.from(0, 0L)),
        equalTo(RouteDistinguisher.from(0, 0L)));
  }

  @Test
  void testJsonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(RouteDistinguisher.from(0, 0L), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(0, 0L)));

    // Note that there is some type ambiguity and (0L, 0) will be converted into (0, 0L)
    assertThat(
        BatfishObjectMapper.clone(RouteDistinguisher.from(0L, 0), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(0, 0L)));

    assertThat(
        BatfishObjectMapper.clone(RouteDistinguisher.from(1L << 17, 2), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(1L << 17, 2)));
    assertThat(
        BatfishObjectMapper.clone(
            RouteDistinguisher.from(Ip.parse("8.8.8.8"), 999), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(Ip.parse("8.8.8.8"), 999)));
  }

  /** Test that generic creator is equivalent to specific creators */
  @Test
  void testFromInference() {
    assertThat(from(1L, 1L), equalTo(from(1, 1L)));
    assertThat(from(1L, 65555L), equalTo(from(1, 65555L)));
    assertThat(from(65555L, 1L), equalTo(from(65555L, 1)));
  }

  @Test
  void testInvalidCombo() {
    assertThrows(IllegalArgumentException.class, () -> from(65555L, 65555L));
  }

  @Test
  void testParsing() {
    assertThat(parse("1:1").getValue(), equalTo(4294967297L));
    assertThat(parse("131072:1").getValue(), equalTo(8589934593L));
    assertThat(parse("0.0.0.0:1").getValue(), equalTo(1L));
  }

  @Test
  void testFromInvalidAsn1NegativeType0() {
    assertThrows(IllegalArgumentException.class, () -> from(-1, 22L));
  }

  @Test
  void testFromInvalidAsn1TooLargeType0() {
    assertThrows(IllegalArgumentException.class, () -> from(1 << 17, 22L));
  }

  @Test
  void testFromInvalidIdNegativeType0() {
    assertThrows(IllegalArgumentException.class, () -> from(1, -1L));
  }

  @Test
  void testFromInvalidIdTooLargeType0() {
    assertThrows(IllegalArgumentException.class, () -> from(65500, -1L));
  }

  @Test
  void testFromInvalidIdNegativeType1() {
    assertThrows(IllegalArgumentException.class, () -> from(Ip.parse("1.1.1.1"), -1));
  }

  @Test
  void testFromInvalidIdTooLargeType1() {
    assertThrows(IllegalArgumentException.class, () -> from(Ip.parse("1.1.1.1"), 1 << 17));
  }

  @Test
  void testFromInvalidAsnNegativeType2() {
    assertThrows(IllegalArgumentException.class, () -> from(-1L, 2));
  }

  @Test
  void testFromInvalidAsnTooLargeType2() {
    assertThrows(IllegalArgumentException.class, () -> from(1L << 33, 2));
  }

  @Test
  void testFromInvalidIdNegativeType2() {
    assertThrows(IllegalArgumentException.class, () -> from(1L, -1));
  }

  @Test
  void testFromInvalidIdTooLargeType2() {
    assertThrows(IllegalArgumentException.class, () -> from(1L, 65536));
  }

  @Test
  void testParsingInvalidNegativeAsn() {
    assertThrows(IllegalArgumentException.class, () -> parse("-1:22"));
  }

  @Test
  void testParsingInvalidIdTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> parse("65555:65555"));
  }

  @Test
  void testParsingInvalidNegative() {
    assertThrows(IllegalArgumentException.class, () -> parse("65555:-1"));
  }

  @Test
  void testParsingInvalidIdNegativeType1() {
    assertThrows(IllegalArgumentException.class, () -> parse("1.1.1.1:-1"));
  }

  @Test
  void testParsingInvalidTooLargeType1() {
    assertThrows(IllegalArgumentException.class, () -> parse("1.1.1.1:65555"));
  }
}
