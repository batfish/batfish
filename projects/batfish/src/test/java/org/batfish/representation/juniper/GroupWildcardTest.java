package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.GroupWildcard.toJavaRegex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests of {@link GroupWildcard} */
public class GroupWildcardTest {

  @Test
  public void testToJavaRegex() {
    assertThat(toJavaRegex("*"), equalTo(".*"));
    assertThat(toJavaRegex("*.*"), equalTo(".*\\..*"));
    assertThat(toJavaRegex("Foo*"), equalTo("Foo.*"));
    assertThat(toJavaRegex("Foo?"), equalTo("Foo\\w"));
    assertThat(toJavaRegex("DC?-suffix-*.*"), equalTo("DC\\w-suffix-.*\\..*"));
  }

  @Test
  public void testToJavaRegexWithUnmatchedBrackets() {
    assertThat(toJavaRegex("["), equalTo("\\["));
    assertThat(toJavaRegex("Literal["), equalTo("Literal\\["));
    assertThat(toJavaRegex("Literal]"), equalTo("Literal\\]"));
  }

  @Test
  public void testToJavaRegexWithCharacterClasses() {
    assertThat(toJavaRegex("[a-c]"), equalTo("[a-c]"));
    assertThat(toJavaRegex("Foo[bar]"), equalTo("Foo[bar]"));
    assertThat(toJavaRegex("[0-9]"), equalTo("[0-9]"));
    assertThat(toJavaRegex("[A-D]"), equalTo("[A-D]"));
    assertThat(toJavaRegex("border-[0-9]-DC?-*"), equalTo("border-[0-9]-DC\\w-.*"));
  }

  @Test
  public void testToJavaRegexWithCharacterClassNegation() {
    assertThat(toJavaRegex("[!a-c]"), equalTo("[^a-c]"));
    assertThat(toJavaRegex("[!qwerty]"), equalTo("[^qwerty]"));
    assertThat(toJavaRegex("test[!4]"), equalTo("test[^4]"));
  }
}
