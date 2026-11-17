package org.batfish.datamodel.bgp.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import java.math.BigInteger;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link StandardCommunity} */
class StandardCommunityTest {

  @Test
  void testEquals() {
    StandardCommunity sc = StandardCommunity.of(1L);
    new EqualsTester()
        .addEqualityGroup(sc, sc, StandardCommunity.of(1L), StandardCommunity.of(0, 1))
        .addEqualityGroup(StandardCommunity.of(2))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testBoundaryConditions() {
    assertThat(StandardCommunity.of(65535, 65535), equalTo(StandardCommunity.of(4294967295L)));
  }

  @Test
  void testOfNegative() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.of(-1));
  }

  @Test
  void testOfTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.of(1L << 32));
  }

  @Test
  void testOfTwoFirstNegative() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.of(-1, 2));
  }

  @Test
  void testOfTwoSecondNegative() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.of(23, -1));
  }

  @Test
  void testOfTwoFirstTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.of(1 << 16, 1));
  }

  @Test
  void testOfTwoSecondTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.of(1, 1 << 16));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(StandardCommunity.of(1)), equalTo(StandardCommunity.of(1)));
  }

  @Test
  void testJsonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(StandardCommunity.of(1), StandardCommunity.class),
        equalTo(StandardCommunity.of(1)));
  }

  @Test
  void testParse() {
    assertThat(StandardCommunity.parse("0:1"), equalTo(StandardCommunity.of(1)));
    assertThat(StandardCommunity.parse("1:1"), equalTo(StandardCommunity.of(65537)));
  }

  @Test
  void testParseGarbage() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.parse("0:x"));
  }

  @Test
  void testParseToMany() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.parse("1:1:1"));
  }

  @Test
  void testParseFirstNegative() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.parse("-1:1"));
  }

  @Test
  void testParseSecondNegative() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.parse("1:-1"));
  }

  @Test
  void testParseFirstTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.parse("65555:1"));
  }

  @Test
  void testParseSecondTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> StandardCommunity.parse("1:65555"));
  }

  @Test
  void testToString() {
    assertThat(StandardCommunity.of(1, 1).toString(), equalTo("1:1"));
  }

  @Test
  void testMatchString() {
    StandardCommunity sc = StandardCommunity.parse("2:3");
    assertThat(sc.toString(), equalTo("2:3"));
    assertThat(sc.matchString(), equalTo(sc.toString()));
  }

  @Test
  void testNotTransitive() {
    assertFalse(StandardCommunity.of(1, 1).isTransitive());
  }

  @Test
  void testToBigInt() {
    assertThat(StandardCommunity.of(0).asBigInt(), equalTo(BigInteger.ZERO));
    assertThat(
        StandardCommunity.of(65535, 65535).asBigInt(), equalTo(BigInteger.valueOf(4294967295L)));
  }

  @Test
  void testAsLong() {
    StandardCommunity c = StandardCommunity.of(65535, 65534);
    assertThat(c.asLong(), equalTo((long) 65535 << 16 | 65534));
  }

  @Test
  void testHighAndLow() {
    StandardCommunity c = StandardCommunity.of(65535, 65534);
    assertThat(c.high(), equalTo(65535));
    assertThat(c.low(), equalTo(65534));
  }
}
