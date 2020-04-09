package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link ParseWarning}. */
public class ParseWarningTest {
  @Test
  public void testParseWarningsEquals() {
    ParseWarning pw = new ParseWarning(1, "", "", "");
    new EqualsTester()
        .addEqualityGroup(pw, pw, new ParseWarning(1, "", "", ""))
        .addEqualityGroup(new ParseWarning(2, "", "", ""))
        .addEqualityGroup(new ParseWarning(1, "a", "", ""))
        .addEqualityGroup(new ParseWarning(1, "", "a", ""))
        .addEqualityGroup(new ParseWarning(1, "", "", "a"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testParseWarningsJavaSerialization() {
    ParseWarning pw = new ParseWarning(1, "", "", "");
    assertThat(SerializationUtils.clone(pw), equalTo(pw));
  }

  @Test
  public void testParseWarningsJsonSerialization() {
    ParseWarning pw = new ParseWarning(1, "", "", null);
    assertThat(BatfishObjectMapper.clone(pw, ParseWarning.class), equalTo(pw));
  }
}
