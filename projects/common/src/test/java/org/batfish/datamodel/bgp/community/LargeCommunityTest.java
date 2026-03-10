package org.batfish.datamodel.bgp.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;

import com.google.common.testing.EqualsTester;
import java.math.BigInteger;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link LargeCommunity} */
public class LargeCommunityTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
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
  public void testJavaSerialization() {
    LargeCommunity lc = LargeCommunity.of(1L, 2L, 3L);
    assertThat(SerializationUtils.clone(lc), equalTo(lc));
  }

  @Test
  public void testJsonSerialization() {
    LargeCommunity lc = LargeCommunity.of(1L, 2L, 3L);
    assertThat(SerializationUtils.clone(lc), equalTo(lc));
  }

  @Test
  public void testParse() {
    assertThat(LargeCommunity.parse("large:1:1:1"), equalTo(LargeCommunity.of(1L, 1L, 1L)));
  }

  @Test
  public void testParseTooFewParts() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("1:1:1");
  }

  @Test
  public void testParseTooManyParts() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("1:1:1:1");
  }

  @Test
  public void testParseNegativeGlobalAdmin() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("-1:1:1");
  }

  @Test
  public void testParseLargeGlobalAdmin() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("large:4294967296:1:1");
  }

  @Test
  public void testParseNegativeLocal1() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("large:1:-1:1");
  }

  @Test
  public void testParseLargeLocal1() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("large:1:4294967296:1");
  }

  @Test
  public void testParseNegativeLocal2() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("large:1:1:-1");
  }

  @Test
  public void testParseLargeLocal2() {
    thrown.expect(IllegalArgumentException.class);
    LargeCommunity.parse("large:1:1:4294967296");
  }

  @Test
  public void testNotTransitive() {
    assertFalse(LargeCommunity.parse("large:1:1:1").isTransitive());
  }

  @Test
  public void testMatchString() {
    LargeCommunity lc = LargeCommunity.parse("large:1:2:3");
    assertThat(lc.toString(), equalTo("large:1:2:3"));
    assertThat(lc.matchString(), equalTo(lc.toString()));
  }

  @Test
  public void testToBigInt() {
    assertThat(
        LargeCommunity.of(4294967295L, 4294967294L, 4294967293L).asBigInt(),
        equalTo(
            BigInteger.valueOf(4294967295L)
                .shiftLeft(64)
                .or(BigInteger.valueOf(4294967294L).shiftLeft(32))
                .or(BigInteger.valueOf(4294967293L))));
  }
}
