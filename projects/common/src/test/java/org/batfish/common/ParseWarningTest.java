package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link ParseWarning}. */
class ParseWarningTest {
  @Test
  void testParseWarningsEquals() {
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
  void testToString() {
    assertThat(
        new ParseWarning(1, "text", "ctx", "comment").toString(),
        equalTo("ParseWarning{line=1, text=text, comment=comment, parserContext=ctx}"));
  }

  @Test
  void testParseWarningsJavaSerialization() {
    ParseWarning pw = new ParseWarning(1, "t", "ctx", "com");
    assertThat(SerializationUtils.clone(pw), equalTo(pw));
  }

  @Test
  void testParseWarningsJsonSerialization() {
    ParseWarning pw = new ParseWarning(1, "t", "ctx", "com");
    assertThat(BatfishObjectMapper.clone(pw, ParseWarning.class), equalTo(pw));
  }
}
