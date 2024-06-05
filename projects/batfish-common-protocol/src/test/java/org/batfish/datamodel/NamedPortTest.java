package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link NamedPort}. */
public class NamedPortTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testFromNumberKnown() {
    assertThat(NamedPort.fromNumber(22), equalTo(NamedPort.SSH));
  }

  @Test
  public void testFromNumberUnknown() {
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("missing enumeration for protocol number");
    NamedPort.fromNumber(123456789);
  }

  @Test
  public void testNameFromNumber() {
    assertThat(NamedPort.nameFromNumber(22), equalTo("SSH(22)"));
    assertThat(NamedPort.nameFromNumber(123456789), equalTo("123456789"));
  }
}
