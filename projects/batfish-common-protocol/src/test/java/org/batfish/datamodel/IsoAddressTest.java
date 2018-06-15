package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.BatfishException;
import org.junit.Test;

public class IsoAddressTest {

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

  @Test(expected = BatfishException.class)
  public void testRejectInvalidChar() {
    new IsoAddress("AA.CCCC.CCCC.CCCC.GG");
  }

  @Test(expected = BatfishException.class)
  public void testRejectOddDigits() {
    new IsoAddress("AA.BBBC.CCCC.CCCC.CDD");
  }

  @Test(expected = BatfishException.class)
  public void testRejectTooLarge() {
    new IsoAddress("AA.BBBB.BBBB.BBBB.BBBB.BBBB.BBBB.BBCC.CCCC.CCCC.CCDD");
  }

  @Test(expected = BatfishException.class)
  public void testRejectTooSmall() {
    new IsoAddress("AA.CCCC.CCCC.CCDD");
  }
}
