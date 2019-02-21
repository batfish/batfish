package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParserUtils.getErrorString;
import static org.batfish.specifier.parboiled.ParserUtils.getPotentialMatches;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class ParserUtilsTest {

  @org.junit.Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(
        TestParser.INSTANCE.input(TestParser.INSTANCE.TestExpression()));
  }

  /** These represent all the ways valid input can start */
  private Set<PotentialMatch> _validStarts =
      ImmutableSet.of(
          new PotentialMatch(Anchor.Type.STRING_LITERAL, "", "@specifier"),
          new PotentialMatch(Anchor.Type.STRING_LITERAL, "", "!"),
          new PotentialMatch(Type.IP_ADDRESS, "", null),
          new PotentialMatch(Anchor.Type.STRING_LITERAL, "", "("));

  @Test
  public void testGetErrorString() {
    assertThat(getErrorString(new PotentialMatch(Type.STRING_LITERAL, "a", "b")), equalTo("'b'"));
    assertThat(
        getErrorString(new PotentialMatch(Type.IP_ADDRESS, "", "b")),
        equalTo(Type.IP_ADDRESS.toString()));
  }

  @Test
  public void testGetErrorStringHasUrl() {
    String input = new String(Character.toChars(0x26bd)); // invalid input

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.INSTANCE.input(Grammar.IP_SPACE_SPECIFIER.getExpression()))
            .run(input);

    String errorString =
        ParserUtils.getErrorString(
            input,
            Grammar.IP_SPACE_SPECIFIER,
            (InvalidInputError) result.parseErrors.get(0),
            Parser.ANCHORS);

    assertThat(errorString, containsString(Grammar.IP_SPACE_SPECIFIER.getFullUrl()));
  }

  @Test
  public void testGetPartialMatchesEmpty() {
    ParsingResult<?> resultEmpty = getRunner().run("");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesBadStart() {
    ParsingResult<?> resultEmpty = getRunner().run("[");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesIncompleteBase() {
    ParsingResult<?> result = getRunner().run("(1.1.1.");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(Type.IP_ADDRESS, "1.1.1.", null))));
  }

  @Test
  public void testGetPartialMatchesIncompleteList() {
    ParsingResult<?> resultEmpty = getRunner().run("a,");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesOpenParens() {
    ParsingResult<?> resultEmpty = getRunner().run("(");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesOperator() {
    ParsingResult<?> resultEmpty = getRunner().run("!");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesMissingCloseParens() {
    ParsingResult<?> result = getRunner().run("(1.1.1.1");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(
            ImmutableSet.of(
                new PotentialMatch(Type.STRING_LITERAL, "", ")"),
                new PotentialMatch(Type.IP_ADDRESS, "1.1.1.1", null),
                new PotentialMatch(Type.STRING_LITERAL, "", "-"))));
  }

  @Test
  public void testGetPartialMatchesSpecifierComplete() {
    ParsingResult<?> result = getRunner().run("@specifier");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(Anchor.Type.STRING_LITERAL, "", "("))));
  }

  @Test
  public void testGetPartialMatchesSpecifierOpenParens() {
    ParsingResult<?> result = getRunner().run("@specifier(");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(Type.ADDRESS_GROUP_AND_BOOK, "", null))));
  }

  @Test
  public void testGetPartialMatchesSpecifierSubstring() {
    ParsingResult<?> result = getRunner().run("@specifi");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(Anchor.Type.STRING_LITERAL, "@specifi", "er"))));
  }

  @Test
  public void testGetPartialMatchesSpecifierIncorrect() {
    ParsingResult<?> result = getRunner().run("@wrong");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(Anchor.Type.STRING_LITERAL, "@", "specifier"))));
  }
}
