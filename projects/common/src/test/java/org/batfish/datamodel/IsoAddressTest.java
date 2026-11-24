package org.batfish.datamodel;

import static org.batfish.datamodel.IsoAddress.invalidCharsMessage;
import static org.batfish.datamodel.IsoAddress.invalidLengthMessage;
import static org.batfish.datamodel.IsoAddress.trim;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class IsoAddressTest {

  @Test
  void testCanonicalization() {
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
  void testRejectInvalidChar() {
    String invalidIsoAddressStr = "AA.CCCC.CCCC.CCCC.GG";

    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new IsoAddress(invalidIsoAddressStr);
            });
    assertThat(
        exception.getMessage(),
        containsString(invalidCharsMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr))));
  }

  @Test
  void testRejectOddDigits() {
    String invalidIsoAddressStr = "AA.BBBC.CCCC.CCCC.CDD";

    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new IsoAddress(invalidIsoAddressStr);
            });
    assertThat(
        exception.getMessage(),
        containsString(invalidLengthMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr))));
  }

  @Test
  void testRejectTooLarge() {
    String invalidIsoAddressStr = "AA.BBBB.BBBB.BBBB.BBBB.BBBB.BBBB.BBCC.CCCC.CCCC.CCDD";
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new IsoAddress(invalidIsoAddressStr);
            });
    assertThat(
        exception.getMessage(),
        containsString(invalidLengthMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr))));
  }

  @Test
  void testRejectTooSmall() {
    String invalidIsoAddressStr = "AA.CCCC.CCCC.CCDD";

    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new IsoAddress(invalidIsoAddressStr);
            });
    assertThat(
        exception.getMessage(),
        containsString(invalidLengthMessage(invalidIsoAddressStr, trim(invalidIsoAddressStr))));
  }
}
