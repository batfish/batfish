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

/** Tests of {@link LargeCommunity} */
class LargeCommunityTest {

  @Test
  void testEquals() {
    LargeCommunity lc = LargeCommunity.of(1L, 2L, 3L);
    new EqualsTester()
        .addEqualityGroup(lc, lc, LargeCommunity.of(1L, 2L, 3L))
        .addEqualityGroup(LargeCommunity.of(2L, 2L, 3L))
        .addEqualityGroup(LargeCommunity.of(1L, 3L, 3L))
        .addEqualityGroup(LargeCommunity.of(1L, 2L, 4L))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    LargeCommunity lc = LargeCommunity.of(1L, 2L, 3L);
    assertThat(SerializationUtils.clone(lc), equalTo(lc));
  }

  @Test
  void testJsonSerialization() {
    LargeCommunity lc = LargeCommunity.of(1L, 2L, 3L);
    assertThat(BatfishObjectMapper.clone(lc, LargeCommunity.class), equalTo(lc));
  }

  @Test
  void testJsonSerializationAsCommunity() {
    LargeCommunity lc = LargeCommunity.of(1L, 2L, 3L);
    assertThat(BatfishObjectMapper.clone(lc, Community.class), equalTo(lc));
  }

  @Test
  void testParse() {
    assertThat(LargeCommunity.parse("large:1:1:1"), equalTo(LargeCommunity.of(1L, 1L, 1L)));
  }

  @Test
  void testParseTooFewParts() {
    assertThrows(IllegalArgumentException.class, () -> LargeCommunity.parse("1:1:1"));
  }

  @Test
  void testParseTooManyParts() {
    assertThrows(IllegalArgumentException.class, () -> LargeCommunity.parse("1:1:1:1"));
  }

  @Test
  void testParseNegativeGlobalAdmin() {
    assertThrows(IllegalArgumentException.class, () -> LargeCommunity.parse("-1:1:1"));
  }

  @Test
  void testParseLargeGlobalAdmin() {
    assertThrows(
        IllegalArgumentException.class, () -> LargeCommunity.parse("large:4294967296:1:1"));
  }

  @Test
  void testParseNegativeLocal1() {
    assertThrows(IllegalArgumentException.class, () -> LargeCommunity.parse("large:1:-1:1"));
  }

  @Test
  void testParseLargeLocal1() {
    assertThrows(
        IllegalArgumentException.class, () -> LargeCommunity.parse("large:1:4294967296:1"));
  }

  @Test
  void testParseNegativeLocal2() {
    assertThrows(IllegalArgumentException.class, () -> LargeCommunity.parse("large:1:1:-1"));
  }

  @Test
  void testParseLargeLocal2() {
    assertThrows(
        IllegalArgumentException.class, () -> LargeCommunity.parse("large:1:1:4294967296"));
  }

  @Test
  void testNotTransitive() {
    assertFalse(LargeCommunity.parse("large:1:1:1").isTransitive());
  }

  @Test
  void testMatchString() {
    LargeCommunity lc = LargeCommunity.parse("large:1:2:3");
    assertThat(lc.toString(), equalTo("large:1:2:3"));
    assertThat(lc.matchString(), equalTo(lc.toString()));
  }

  @Test
  void testToBigInt() {
    assertThat(
        LargeCommunity.of(4294967295L, 4294967294L, 4294967293L).asBigInt(),
        equalTo(
            BigInteger.valueOf(4294967295L)
                .shiftLeft(64)
                .or(BigInteger.valueOf(4294967294L).shiftLeft(32))
                .or(BigInteger.valueOf(4294967293L))));
  }
}
