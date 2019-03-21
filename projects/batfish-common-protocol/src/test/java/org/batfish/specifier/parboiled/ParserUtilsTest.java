package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_AND_BOOK;
import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.IGNORE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.batfish.specifier.parboiled.ParserUtils.findPathAnchorFromBottom;
import static org.batfish.specifier.parboiled.ParserUtils.getErrorString;
import static org.batfish.specifier.parboiled.ParserUtils.getPotentialMatches;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.batfish.specifier.parboiled.ParserUtils.PathElement;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class ParserUtilsTest {

  @org.junit.Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(TestParser.INSTANCE.input(TestParser.INSTANCE.TestSpec()));
  }

  /** These represent all the ways valid input can start */
  private static Set<PotentialMatch> getValidStarts(int matchStartIndex) {
    return ImmutableSet.of(
        new PotentialMatch(STRING_LITERAL, "", "@specifier", matchStartIndex),
        new PotentialMatch(CHAR_LITERAL, "", "!", matchStartIndex),
        new PotentialMatch(IP_ADDRESS, "", null, matchStartIndex),
        new PotentialMatch(NODE_NAME, "", null, matchStartIndex),
        new PotentialMatch(CHAR_LITERAL, "", "\"", matchStartIndex),
        new PotentialMatch(CHAR_LITERAL, "", "/", matchStartIndex),
        new PotentialMatch(CHAR_LITERAL, "", "(", matchStartIndex));
  }

  @Test
  public void testConstructorPathElement() {
    // double quoted strings map to STRING_LITERAL
    assertThat(new PathElement("\"@specifier\"", 0, null).getAnchorType(), equalTo(STRING_LITERAL));

    // unquoted strings do not
    assertThat(new PathElement("@specifier", 0, null).getAnchorType(), equalTo(null));

    // single-quoted characters map to CHAR_LITERAL
    assertThat(new PathElement("'a'", 0, null).getAnchorType(), equalTo(CHAR_LITERAL));

    // unquoted characters do not
    assertThat(new PathElement("a", 0, null).getAnchorType(), equalTo(null));

    // anchor type is preserved if provided
    assertThat(new PathElement("\"@specifier\"", 0, NODE_NAME).getAnchorType(), equalTo(NODE_NAME));
  }

  @Test
  public void testFindPathAnchorFromBottomNoAnchor() {
    assertFalse(findPathAnchorFromBottom(ImmutableList.of(), -1).isPresent());
    assertFalse(
        findPathAnchorFromBottom(ImmutableList.of(new PathElement("TestSomething", 0, null)), -1)
            .isPresent());
  }

  @Test
  public void testFindPathAnchorFromBottomCharAnchor() {
    PathElement stringElement = new PathElement("aloha", 0, STRING_LITERAL);
    PathElement charElement = new PathElement("a", 1, CHAR_LITERAL);
    PathElement otherElement = new PathElement("TestName", 0, NODE_NAME);

    // if parent is string, that should be returned
    assertThat(
        findPathAnchorFromBottom(ImmutableList.of(stringElement, charElement), 1),
        equalTo(Optional.of(stringElement)));

    // otherwise, the char literal itself should be returned
    assertThat(
        findPathAnchorFromBottom(ImmutableList.of(otherElement, charElement), 1),
        equalTo(Optional.of(charElement)));
  }

  @Test
  public void testFindPathAnchorFromBottomIgnoreIsIgnored() {
    PathElement ignoreElement = new PathElement("aloha", 0, IGNORE);

    assertThat(
        findPathAnchorFromBottom(ImmutableList.of(ignoreElement), 0), equalTo(Optional.empty()));
  }

  @Test
  public void testFindPathAnchorFromBottomIgnoreIsSkipped() {
    PathElement element0 = new PathElement("aloha", 0, NODE_NAME);
    PathElement element1 = new PathElement("aloha", 1, IGNORE);
    PathElement element2 = new PathElement("aloha", 2, null);
    PathElement element3 = new PathElement("aloha", 3, IGNORE);
    PathElement element4 = new PathElement("aloha", 4, null);

    assertThat(
        findPathAnchorFromBottom(
            ImmutableList.of(element0, element1, element2, element3, element4), 0),
        equalTo(Optional.of(element0)));
  }

  @Test
  public void testFindPathAnchorFromBottomStringAnchor() {
    PathElement element = new PathElement("\"@specifier\"", 0, null);
    assertThat(
        findPathAnchorFromBottom(ImmutableList.of(element), 0), equalTo(Optional.of(element)));
  }

  @Test
  public void testGetErrorString() {
    assertThat(getErrorString(new PotentialMatch(STRING_LITERAL, "a", "b", 0)), equalTo("'b'"));
    assertThat(
        getErrorString(new PotentialMatch(IP_ADDRESS, "", "b", 0)), equalTo(IP_ADDRESS.toString()));
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
        equalTo(getValidStarts(0)));
  }

  @Test
  public void testGetPartialMatchesBadStart() {
    ParsingResult<?> resultEmpty =
        getRunner().run(new String(Character.toChars(ParboiledAutoComplete.ILLEGAL_CHAR)));
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(getValidStarts(0)));
  }

  @Test
  public void testGetPartialMatchesIncompleteBase() {
    ParsingResult<?> result = getRunner().run("(1.1.1.");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(IP_ADDRESS, "1.1.1.", null, 1))));
  }

  @Test
  public void testGetPartialMatchesIncompleteList() {
    ParsingResult<?> resultEmpty = getRunner().run("a,");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(getValidStarts(2)));
  }

  @Test
  public void testGetPartialMatchesOpenParens() {
    ParsingResult<?> resultEmpty = getRunner().run("(");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(getValidStarts(1)));
  }

  @Test
  public void testGetPartialMatchesOperator() {
    ParsingResult<?> resultEmpty = getRunner().run("!");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(getValidStarts(1)));
  }

  @Test
  public void testGetPartialMatchesMissingCloseParens() {
    ParsingResult<?> result = getRunner().run("(1.1.1.1");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(
            ImmutableSet.of(
                new PotentialMatch(CHAR_LITERAL, "", ")", 8),
                new PotentialMatch(CHAR_LITERAL, "", ",", 8),
                new PotentialMatch(IP_ADDRESS, "1.1.1.1", null, 1),
                new PotentialMatch(CHAR_LITERAL, "", "-", 8))));
  }

  @Test
  public void testGetPartialMatchesQuoteOpenName() {
    ParsingResult<?> result = getRunner().run("\"a");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(
            ImmutableSet.of(
                new PotentialMatch(CHAR_LITERAL, "", "\"", 2),
                new PotentialMatch(NODE_NAME, "\"a", null, 0))));
  }

  @Test
  public void testGetPartialMatchesSpecifierComplete() {
    ParsingResult<?> result = getRunner().run("@specifier");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(CHAR_LITERAL, "", "(", 10))));
  }

  @Test
  public void testGetPartialMatchesSpecifierOpenParens() {
    ParsingResult<?> result = getRunner().run("@specifier(");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(ADDRESS_GROUP_AND_BOOK, "", null, 11))));
  }

  @Test
  public void testGetPartialMatchesSpecifierSubstring() {
    ParsingResult<?> result = getRunner().run("@specifi");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(STRING_LITERAL, "@specifi", "@specifier", 0))));
  }

  @Test
  public void testGetPartialMatchesSpecifierIncorrect() {
    ParsingResult<?> result = getRunner().run("@wrong");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(STRING_LITERAL, "@", "@specifier", 0))));
  }

  @Test
  public void testGetPartialMatchesStringLiteralCasePreserve() {
    ParsingResult<?> result = getRunner().run("@SPeciFi");
    assertThat(
        getPotentialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false),
        equalTo(ImmutableSet.of(new PotentialMatch(STRING_LITERAL, "@SPeciFi", "@specifier", 0))));
  }
}
