package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.GroupWildcard.debugToJavaRegex;
import static org.batfish.representation.juniper.GroupWildcard.toJavaRegex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Ignore;
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

  @Ignore("Character classes not fleshed out yet")
  @Test
  public void testToJavaRegexWithUnmatchedBrackets() {
    assertThat(toJavaRegex("["), equalTo("\\["));
    assertThat(toJavaRegex("Literal["), equalTo("Literal\\["));
    assertThat(toJavaRegex("Literal]"), equalTo("Literal\\]"));
  }

  @Test
  public void testToJavaRegexWithCharacterClasses() {
    assertThat(debugToJavaRegex("[a-c]"), equalTo("[a-c]"));
    assertThat(debugToJavaRegex("Foo[bar]"), equalTo("Foo[bar]"));
    assertThat(toJavaRegex("[0-9]"), equalTo("[0-9]"));
    assertThat(toJavaRegex("[A-D]"), equalTo("[A-D]"));
    assertThat(toJavaRegex("border-[0-9]-DC?-*"), equalTo("border-[0-9]-DC\\w-.*"));
  }

  @Ignore("Character classes not fleshed out yet")
  @Test
  public void testToJavaRegexWithCharacterClassNegation() {
    assertThat(debugToJavaRegex("[!a-c]"), equalTo("[^a-c]"));
    assertThat(toJavaRegex("[!qwerty]"), equalTo("[^qwerty]"));
  }
}
