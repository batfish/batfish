package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.batfish.common.BatfishException;
import org.junit.jupiter.api.Test;

/** Tests of {@link NamedPort}. */
class NamedPortTest {

  @Test
  void testFromNumberKnown() {
    assertThat(NamedPort.fromNumber(22), equalTo(NamedPort.SSH));
  }

  @Test
  void testFromNumberUnknown() {
    Throwable exception =
        assertThrows(BatfishException.class, () -> NamedPort.fromNumber(123456789));
    assertThat(exception.getMessage(), containsString("missing enumeration for protocol number"));
  }

  @Test
  void testNameFromNumber() {
    assertThat(NamedPort.nameFromNumber(22), equalTo("SSH(22)"));
    assertThat(NamedPort.nameFromNumber(123456789), equalTo("123456789"));
  }
}
