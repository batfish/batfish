package org.batfish.datamodel;

import static org.batfish.datamodel.IsoAddress.invalidCharsMessage;
import static org.batfish.datamodel.IsoAddress.invalidLengthMessage;
import static org.batfish.datamodel.IsoAddress.trim;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class IsoAddressTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCanonicalization() {
    String even = "AaccccccCcccccdD";
    String evenCanonical = "AA.CCCC.CCCC.CCCC.DD";
    String odd = "AABBCCCCCCCCCCCCDD";
    String oddCanonical = "AA.BBCC.CCCC.CCCC.CCDD";

    // check that canonicalization produces expected results
    assertThat(new IsoAddress(even).toString(), equalTo(evenCanonical));
    assertThat(new IsoAddress(odd).toString(), equalTo(oddCanonical));

    // confirm canonicalized forms are unchanged by canonicalization
    assertThat(new IsoAddress(evenCanonical).toString(), equalTo(evenCanonical));
    assertThat(new IsoAddress(oddCanonical).toString(), equalTo(oddCanonical));
  }

  @Test
  public void testRejectInvalidChar() {
    String invalidIsoAddressStr = "AA.CCCC.CCCC.CCCC.GG";

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(invalidCharsMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr)));
    new IsoAddress(invalidIsoAddressStr);
  }

  @Test
  public void testRejectOddDigits() {
    String invalidIsoAddressStr = "AA.BBBC.CCCC.CCCC.CDD";

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(invalidLengthMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr)));
    new IsoAddress(invalidIsoAddressStr);
  }

  @Test
  public void testRejectTooLarge() {
    String invalidIsoAddressStr = "AA.BBBB.BBBB.BBBB.BBBB.BBBB.BBBB.BBCC.CCCC.CCCC.CCDD";
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(invalidLengthMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr)));
    new IsoAddress(invalidIsoAddressStr);
  }

  @Test
  public void testRejectTooSmall() {
    String invalidIsoAddressStr = "AA.CCCC.CCCC.CCDD";

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage(invalidLengthMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr)));
    new IsoAddress(invalidIsoAddressStr);
  }
}
