package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.GroupWildcard.toJavaRegex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/** Tests of {@link GroupWildcard} */
class GroupWildcardTest {

  @Test
  void testToJavaRegex() {
    assertThat(toJavaRegex("*"), equalTo(".*"));
    assertThat(toJavaRegex("*.*"), equalTo(".*\\..*"));
    assertThat(toJavaRegex("Foo*"), equalTo("Foo.*"));
    assertThat(toJavaRegex("Foo?"), equalTo("Foo\\w"));
    assertThat(toJavaRegex("DC?-suffix-*.*"), equalTo("DC\\w-suffix-.*\\..*"));
  }

  @Test
  void testToJavaRegexWithUnmatchedBrackets() {
    assertThat(toJavaRegex("["), equalTo("\\["));
    assertThat(toJavaRegex("Literal["), equalTo("Literal\\["));
    assertThat(toJavaRegex("Literal]"), equalTo("Literal\\]"));
  }

  @Test
  void testToJavaRegexWithCharacterClasses() {
    assertThat(toJavaRegex("[a-c]"), equalTo("[a-c]"));
    assertThat(toJavaRegex("Foo[bar]"), equalTo("Foo[bar]"));
    assertThat(toJavaRegex("[0-9]"), equalTo("[0-9]"));
    assertThat(toJavaRegex("[A-D]"), equalTo("[A-D]"));
    assertThat(toJavaRegex("border-[0-9]-DC?-*"), equalTo("border-[0-9]-DC\\w-.*"));
    assertThat(toJavaRegex("ge-1/2/[5-8]"), equalTo("ge-1/2/[5-8]"));
    assertThat(toJavaRegex("12.1[23]4.*.*/2[89]"), equalTo("12\\.1[23]4\\..*\\..*/2[89]"));
  }

  @Test
  void testToJavaRegexWithCharacterClassNegation() {
    assertThat(toJavaRegex("[!a-c]"), equalTo("[^a-c]"));
    assertThat(toJavaRegex("[!qwerty]"), equalTo("[^qwerty]"));
    assertThat(toJavaRegex("test[!4]"), equalTo("test[^4]"));
    assertThat(toJavaRegex("*[^/][^0]"), equalTo(".*[^/][^0]"));
  }
}
