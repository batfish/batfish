package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests of {@link AsSet} */
public class AsSetTest {

  @Test
  public void testToString() {
    // For single ASN, should just return string of that ASN
    long asn = 12345;
    assertThat(AsSet.of(asn).toString(), equalTo(String.valueOf(asn)));

    // For set of ASNs, should return {asn1, asn2, ... lastAsn}
    assertThat(AsSet.of(1, 2, 3).toString(), equalTo("{1, 2, 3}"));
  }
}
