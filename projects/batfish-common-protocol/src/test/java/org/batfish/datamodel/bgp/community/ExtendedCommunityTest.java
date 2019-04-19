package org.batfish.datamodel.bgp.community;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
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
    ExtendedCommunity ec = ExtendedCommunity.of(1, 2L, 123L);
    new EqualsTester()
        .addEqualityGroup(
            ec,
            ec,
            ExtendedCommunity.of(1, 2L, 123L),
            ExtendedCommunity.of(1, Ip.parse("0.0.0.2"), 123L))
        .addEqualityGroup(ExtendedCommunity.of(2, 2L, 123L))
        .addEqualityGroup(ExtendedCommunity.of(1, 3L, 123L))
        .addEqualityGroup(ExtendedCommunity.of(1, 2L, 124L))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ExtendedCommunity ec = ExtendedCommunity.of(1, 2L, 123L);
    assertThat(SerializationUtils.clone(ec), equalTo(ec));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    ExtendedCommunity ec = ExtendedCommunity.of(1, 2L, 123L);
    assertThat(BatfishObjectMapper.clone(ec, ExtendedCommunity.class), equalTo(ec));
  }

  @Test
  public void testParse() {
    assertThat(ExtendedCommunity.parse("1:1:1"), equalTo(ExtendedCommunity.of(1, 1L, 1L)));
    assertThat(ExtendedCommunity.parse("1:65535:1"), equalTo(ExtendedCommunity.of(1, 65535L, 1L)));
    assertThat(ExtendedCommunity.parse("1:656L:1"), equalTo(ExtendedCommunity.of(1, 656L, 1L)));
    assertThat(ExtendedCommunity.parse("1:0.0.0.1:1"), equalTo(ExtendedCommunity.of(1, 1L, 1L)));
    assertThat(ExtendedCommunity.parse("1:1.1:1"), equalTo(ExtendedCommunity.of(1, 65537, 1L)));
    assertThat(ExtendedCommunity.parse("target:1L:1"), equalTo(ExtendedCommunity.of(514, 1L, 1L)));
    assertThat(ExtendedCommunity.parse("origin:1L:1"), equalTo(ExtendedCommunity.of(515, 1L, 1L)));
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
    ExtendedCommunity.parse("1:656L:65536");
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
}
