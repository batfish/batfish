package org.batfish.datamodel;

import static org.batfish.datamodel.AsSet.confed;
import static org.batfish.datamodel.AsSet.confedEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link AsSet} */
public class AsSetTest {

  @Test
  public void testToString() {
    // For single ASN, should just return string of that ASN
    long asn = 12345;
    assertThat(AsSet.of(asn).toString(), equalTo(String.valueOf(asn)));

    // For set of ASNs, should return {asn1,asn2,...lastAsn}
    assertThat(AsSet.of(1, 2, 3).toString(), equalTo("{1,2,3}"));
  }

  @Test
  public void testConfed() {
    AsSet set = confedEmpty();
    assertTrue(set.isEmpty());
    assertTrue(set.isConfederationAsSet());

    set = confed(1L);
    assertFalse(set.isEmpty());
    assertTrue(set.isConfederationAsSet());
  }

  @Test
  public void testEquals() {
    AsSet set = AsSet.of(1);
    new EqualsTester()
        .addEqualityGroup(set, set, AsSet.of(1))
        .addEqualityGroup(AsSet.of(2))
        .addEqualityGroup(confed(2))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    AsSet set = AsSet.of(1);
    assertThat(BatfishObjectMapper.clone(set, AsSet.class), equalTo(set));
    // test maximum ASN
    set = AsSet.of(4294967295L);
    assertThat(BatfishObjectMapper.clone(set, AsSet.class), equalTo(set));
    set = AsSet.confed(1);
    assertThat(BatfishObjectMapper.clone(set, AsSet.class), equalTo(set));
    set = AsSet.of(1, 2, 3);
    assertThat(BatfishObjectMapper.clone(set, AsSet.class), equalTo(set));
  }

  @Test
  public void testJsonSerializationFriendly() throws JsonProcessingException {
    assertThat(
        BatfishObjectMapper.mapper().readValue("4294967295", AsSet.class),
        equalTo(AsSet.of(4294967295L)));
  }
}
