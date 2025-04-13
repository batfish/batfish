package org.batfish.datamodel.bgp.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
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
        .addEqualityGroup(ec, ExtendedCommunity.of(0, 2L, 123L))
        .addEqualityGroup(
            ExtendedCommunity.of(1 << 8, 2L, 123L),
            ExtendedCommunity.of(1 << 8, Ip.parse("0.0.0.2"), 123L))
        .addEqualityGroup(ExtendedCommunity.of(0, 3L, 123L))
        .addEqualityGroup(ExtendedCommunity.of(0, 2L, 124L))
        .addEqualityGroup(ExtendedCommunity.opaque(true, 1, 2))
        .addEqualityGroup(ExtendedCommunity.opaque(false, 1, 2))
        .addEqualityGroup(ExtendedCommunity.opaque(false, 3, 2))
        .addEqualityGroup(ExtendedCommunity.opaque(false, 3, 4))
        .addEqualityGroup(ExtendedCommunity.encapsulation(7))
        .addEqualityGroup(ExtendedCommunity.encapsulation(8))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    for (ExtendedCommunity ec :
        ImmutableList.of(
            ExtendedCommunity.of(1, 2L, 123L),
            ExtendedCommunity.opaque(true, 1, 2),
            ExtendedCommunity.opaque(false, 255, 0xFFFFFFFFFFFFL),
            ExtendedCommunity.encapsulation(7))) {
      assertThat(SerializationUtils.clone(ec), equalTo(ec));
      assertThat(BatfishObjectMapper.clone(ec, Community.class), equalTo(ec));
    }
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
    assertThat(
        ExtendedCommunity.parse("encapsulation:0L:7"),
        equalTo(ExtendedCommunity.encapsulation(7L)));
    assertThat(
        ExtendedCommunity.parse("0x43:0x4:0x5"), equalTo(ExtendedCommunity.opaque(false, 4, 5L)));
    assertThat(
        ExtendedCommunity.parse("0x3:0x4:0x5"), equalTo(ExtendedCommunity.opaque(true, 4, 5L)));
    assertThat(
        ExtendedCommunity.parse("0X3:0X4:0X5"), equalTo(ExtendedCommunity.opaque(true, 4, 5L)));
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
  public void testParseGenericNegativeType() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("0x-1:0x2:0x3");
  }

  @Test
  public void testParseGenericNegativeSubtype() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("0x3:0x-4:0x5");
  }

  @Test
  public void testParseGenericNegativeValue() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.parse("0x3:0x4:0x-5");
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
  public void testGetLAInapplicable() {
    ExtendedCommunity community = ExtendedCommunity.opaque(true, 0x00, 65559);
    thrown.expect(UnsupportedOperationException.class);
    community.getLocalAdministrator();
  }

  @Test
  public void testGetGAInapplicable() {
    ExtendedCommunity community = ExtendedCommunity.opaque(true, 0x00, 65559);
    thrown.expect(UnsupportedOperationException.class);
    community.getGlobalAdministrator();
  }

  @Test
  public void testIsTransitive() {
    assertTrue(ExtendedCommunity.parse("1:1:1").isTransitive());
    assertFalse(ExtendedCommunity.parse("16384:1:1").isTransitive());
  }

  @Test
  public void testMatchString() {
    ExtendedCommunity ec = ExtendedCommunity.parse("origin:65555L:1");
    assertThat(ec.matchString(), equalTo("65555:1"));

    // check no local/global administrator case
    assertThat(
        ExtendedCommunity.opaque(true, 0x00, 65559).matchString(), equalTo("0x3:0x0:0x10017"));
  }

  @Test
  public void testToString() {
    assertThat(ExtendedCommunity.parse("origin:65555:1").toString(), equalTo("515:65555L:1"));
    assertThat(ExtendedCommunity.parse("origin:1L:1").toString(), equalTo("515:1L:1"));
    assertThat(ExtendedCommunity.parse("origin:1:1").toString(), equalTo("3:1:1"));
    assertThat(ExtendedCommunity.parse("origin:0.0.0.0:1").toString(), equalTo("259:0L:1"));

    // check no local/global administrator case
    assertThat(ExtendedCommunity.opaque(true, 0x00, 65559).toString(), equalTo("0x3:0x0:0x10017"));
    assertThat(
        ExtendedCommunity.opaque(false, 0x00, 65559).toString(), equalTo("0x43:0x0:0x10017"));
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
  public void testOpaqueCreation() {
    ExtendedCommunity communityTransitive = ExtendedCommunity.opaque(true, 0x00, 65559);
    assertThat(communityTransitive.getValue(), equalTo(65559L));
    ExtendedCommunity communityIntransitive = ExtendedCommunity.opaque(false, 0x01, 2);
    assertThat(communityIntransitive.getValue(), equalTo(2L));
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
  public void testIsOpaque() {
    assertFalse(ExtendedCommunity.of(0x0010, 1, 1).isOpaque());
    assertFalse(ExtendedCommunity.of(0x0110, 1, 1).isOpaque());
    assertTrue(ExtendedCommunity.of(0x0300, 1, 1).isOpaque());
    assertTrue(ExtendedCommunity.opaque(false, 1, 1).isOpaque());
  }

  @Test
  public void testGetGlobalAdmin() {
    assertThat(ExtendedCommunity.of(1, 2, 3).getGlobalAdministrator(), equalTo(2L));
    assertThat(
        ExtendedCommunity.of(1, 42995L, 4294967295L).getGlobalAdministrator(), equalTo(42995L));
    assertThat(
        ExtendedCommunity.of(0x01 << 8 | 1, 4294967295L, 1).getGlobalAdministrator(),
        equalTo(4294967295L));
    assertThat(
        ExtendedCommunity.of(0x01 << 8 | 1, 1, 42995L).getGlobalAdministrator(), equalTo(1L));
  }

  @Test
  public void testGetLocalAdmin() {
    assertThat(ExtendedCommunity.of(1, 2, 3).getLocalAdministrator(), equalTo(3L));
    assertThat(
        ExtendedCommunity.of(1, 42995L, 4294967295L).getLocalAdministrator(), equalTo(4294967295L));
    assertThat(
        ExtendedCommunity.of(0x01 << 8 | 1, 4294967295L, 1).getLocalAdministrator(), equalTo(1L));
    assertThat(
        ExtendedCommunity.of(0x01 << 8 | 1, 1, 42995L).getLocalAdministrator(), equalTo(42995L));
  }

  @Test
  public void testGetSubType() {
    assertThat(ExtendedCommunity.of(0x0201, 2, 3).getSubtype(), equalTo(0x01));
    assertThat(ExtendedCommunity.opaque(true, 0x04, 3).getSubtype(), equalTo(0x04));
    assertThat(ExtendedCommunity.target(1, 3).getSubtype(), equalTo(0x02));
  }

  @Test
  public void testWellKnownCommunities() {
    /*
     * The following byte arrays are copied directly out of RFC's level bit representation. Would not recommend using
     * this type of test otherwise.
     */
    assertThat(
        ExtendedCommunity.ORIGIN_VALIDATION_STATE_VALID.asBigInt().toByteArray(),
        equalTo(new byte[] {0x43, 0, 0, 0, 0, 0, 0, 0}));
    assertThat(
        ExtendedCommunity.ORIGIN_VALIDATION_STATE_NOT_FOUND.asBigInt().toByteArray(),
        equalTo(new byte[] {0x43, 0, 0, 0, 0, 0, 0, 1}));
    assertThat(
        ExtendedCommunity.ORIGIN_VALIDATION_STATE_INVALID.asBigInt().toByteArray(),
        equalTo(new byte[] {0x43, 0, 0, 0, 0, 0, 0, 2}));
  }

  @Test
  public void testEncapsulationCreation() {
    ExtendedCommunity community = ExtendedCommunity.encapsulation(7);
    assertThat(community.getValue(), equalTo(7L));
    assertThat(community.getSubtype(), equalTo(0x0C));
    assertTrue(community.isTransitive());
    assertTrue(community.isOpaque());
  }

  @Test
  public void testEncapsulationInvalidTunnelType() {
    thrown.expect(IllegalArgumentException.class);
    ExtendedCommunity.encapsulation(0x10000L); // Exceeds 16-bit limit
  }

  @Test
  public void testIsEncapsulation() {
    assertTrue(ExtendedCommunity.encapsulation(7).isEncapsulation());
    assertTrue(ExtendedCommunity.of(0x030C, 0, 7).isEncapsulation());
    assertFalse(ExtendedCommunity.of(0x030D, 0, 7).isEncapsulation()); // Wrong subtype
    assertFalse(ExtendedCommunity.of(0x430C, 0, 7).isEncapsulation()); // Wrong type
    assertFalse(ExtendedCommunity.opaque(true, 0x0D, 7).isEncapsulation());
    assertFalse(ExtendedCommunity.target(1, 1).isEncapsulation());
  }
}
