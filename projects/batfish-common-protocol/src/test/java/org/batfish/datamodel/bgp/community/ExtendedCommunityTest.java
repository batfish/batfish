package org.batfish.datamodel.bgp.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import java.math.BigInteger;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link ExtendedCommunity} */
public final class ExtendedCommunityTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    ExtendedCommunity ec = ExtendedCommunity.of(0, 2L, 123L);
    new EqualsTester()
        .addEqualityGroup(ec, ec, ExtendedCommunity.of(0, 2L, 123L))
        .addEqualityGroup(
            ExtendedCommunity.of(1 << 8, 2L, 123L),
            ExtendedCommunity.of(1 << 8, Ip.parse("0.0.0.2"), 123L))
        .addEqualityGroup(ExtendedCommunity.of(0, 3L, 123L))
        .addEqualityGroup(ExtendedCommunity.of(0, 2L, 124L))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ExtendedCommunity ec = ExtendedCommunity.of(1, 2L, 123L);
    assertThat(SerializationUtils.clone(ec), equalTo(ec));
  }

  @Test
  public void testJsonSerialization() {
    ExtendedCommunity ec = ExtendedCommunity.of(1, 2L, 123L);
    assertThat(BatfishObjectMapper.clone(ec, ExtendedCommunity.class), equalTo(ec));
  }

  @Test
  public void testParse() {
    assertThat(ExtendedCommunity.parse("1:1:1"), equalTo(ExtendedCommunity.of(1, 1L, 1L)));
    assertThat(ExtendedCommunity.parse("1:65535:1"), equalTo(ExtendedCommunity.of(1, 65535L, 1L)));
    assertThat(ExtendedCommunity.parse("1:656L:1"), equalTo(ExtendedCommunity.of(1, 656L, 1L)));
    assertThat(ExtendedCommunity.parse("1:0.0.0.1:1"), equalTo(ExtendedCommunity.of(1, 1L, 1L)));
    assertThat(
        ExtendedCommunity.parse("512:1.1:1"), equalTo(ExtendedCommunity.of(0x02 << 8, 65537, 1L)));
    assertThat(ExtendedCommunity.parse("target:1L:1"), equalTo(ExtendedCommunity.of(514, 1L, 1L)));
    assertThat(ExtendedCommunity.parse("origin:1L:1"), equalTo(ExtendedCommunity.of(515, 1L, 1L)));
  }

  @Test
  public void testOfWithInvalidType() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.of(4 << 8, 1, 1);
  }

  @Test
  public void testOfWithInvalidTypeAdminCombo() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.of(0, 1 << 16, 1);
  }

  @Test
  public void testParseTooFewParts() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("1:1");
  }

  @Test
  public void testParseTooManyParts() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("1:1:1:1");
  }

  @Test
  public void testParseNegativeType() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("-1:1:1");
  }

  @Test
  public void testParseLargeType() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("65536:1:1");
  }

  @Test
  public void testInvalidType() {
    thrown.expect(IllegalArgumentException.class);
    // this will make typeByte 0x04
    ExtendedCommunity.parse("1024:1:1");
  }

  @Test
  public void testParseNegativeGlobalAdmin() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("1:-1:1");
  }

  @Test
  public void testParseLargeGlobalAdmin() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("1:4294967296:1");
  }

  @Test
  public void testParseNegativeLocalAdmin() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("1:1:-1");
  }

  @Test
  public void testParseLargeLocalAdmin() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("1:1:4294967296");
  }

  @Test
  public void testParseLargeGlobalAdminBecauseLsuffix() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("origin:656L:65536");
  }

  @Test
  public void testIsTransitive() {
    assertFalse(ExtendedCommunity.parse("1:1:1").isTransitive());
    assertTrue(ExtendedCommunity.parse("16384:1:1").isTransitive());
  }

  @Test
  public void testMatchString() {
    ExtendedCommunity ec = ExtendedCommunity.parse("origin:65555L:1");
    assertThat(ec.matchString(), equalTo("65555:1"));
  }

  @Test
  public void testToString() {
    assertThat(ExtendedCommunity.parse("origin:65555:1").toString(), equalTo("515:65555L:1"));
    assertThat(ExtendedCommunity.parse("origin:1L:1").toString(), equalTo("515:1L:1"));
    assertThat(ExtendedCommunity.parse("origin:1:1").toString(), equalTo("3:1:1"));
    assertThat(ExtendedCommunity.parse("origin:0.0.0.0:1").toString(), equalTo("259:0L:1"));
  }

  @Test
  public void testToBigInt() {
    assertThat(ExtendedCommunity.of(0, 0, 0).asBigInt(), equalTo(BigInteger.ZERO));
    assertThat(
        ExtendedCommunity.of((0x40 << 8) + 1, 65535, 4294967295L).asBigInt(),
        equalTo(
            BigInteger.valueOf(16385)
                .shiftLeft(48)
                .or(BigInteger.valueOf(65535).shiftLeft(32))
                .or(BigInteger.valueOf(4294967295L))));
    assertThat(
        ExtendedCommunity.of((0x41 << 8) + 1, 4294967295L, 65535).asBigInt(),
        equalTo(
            BigInteger.valueOf(16641)
                .shiftLeft(48)
                .or(BigInteger.valueOf(4294967295L).shiftLeft(16))
                .or(BigInteger.valueOf(65535))));
  }

  @Test
  public void testTargetCreation() {
    assertThat(ExtendedCommunity.target(1, 65555), equalTo(ExtendedCommunity.of(0x02, 1, 65555)));
    assertThat(
        ExtendedCommunity.target(65555, 1),
        equalTo(ExtendedCommunity.of(0x02 << 8 | 0x02, 65555, 1)));
    assertThat(ExtendedCommunity.target(1, 65555).toString(), equalTo("2:1:65555"));
    assertThat(ExtendedCommunity.target(65555, 1).toString(), equalTo("514:65555L:1"));
  }

  @Test
  public void testIsRouteOrigin() {
    assertTrue(ExtendedCommunity.of(0x0003, 1, 1).isRouteOrigin());
    assertTrue(ExtendedCommunity.of(0x0103, 1, 1).isRouteOrigin());
    assertTrue(ExtendedCommunity.of(0x0203, 1, 1).isRouteOrigin());
    assertFalse(ExtendedCommunity.of(0x0303, 1, 1).isRouteOrigin());
    assertFalse(ExtendedCommunity.of(0x0002, 1, 1).isRouteOrigin());
  }

  @Test
  public void testIsRouteTarget() {
    assertTrue(ExtendedCommunity.target(1, 1).isRouteTarget());
    assertTrue(ExtendedCommunity.parse("514:65555L:1").isRouteTarget());
    assertTrue(ExtendedCommunity.of(0x0002, 1, 1).isRouteTarget());
    assertTrue(ExtendedCommunity.of(0x0102, 1, 1).isRouteTarget());
    assertTrue(ExtendedCommunity.of(0x0202, 1, 1).isRouteTarget());
    assertFalse(ExtendedCommunity.of(0x0302, 1, 1).isRouteTarget());
    assertFalse(ExtendedCommunity.of(0x0003, 1, 1).isRouteTarget());
    assertFalse(ExtendedCommunity.of(1, 1, 1).isRouteTarget());
  }

  @Test
  public void testIsVpnDistinguisher() {
    assertTrue(ExtendedCommunity.of(0x0010, 1, 1).isVpnDistinguisher());
    assertTrue(ExtendedCommunity.of(0x0110, 1, 1).isVpnDistinguisher());
    assertFalse(ExtendedCommunity.of(0x0003, 1, 1).isVpnDistinguisher());
    assertFalse(ExtendedCommunity.of(0x0210, 1, 1).isVpnDistinguisher());
  }

  @Test
  public void testGetGlobalAdmin() {
    assertThat(ExtendedCommunity.of(1, 2, 3).getGlobalAdministrator(), equalTo(2L));
  }

  @Test
  public void testGetLocalAdmin() {
    assertThat(ExtendedCommunity.of(1, 2, 3).getLocalAdministrator(), equalTo(3L));
  }
}
