package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.IGNORE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.batfish.specifier.parboiled.ParserUtils.findPathAnchorFromBottom;
import static org.batfish.specifier.parboiled.ParserUtils.getErrorString;
import static org.batfish.specifier.parboiled.ParserUtils.getPotentialMatches;
import static org.batfish.specifier.parboiled.ParserUtils.isCharLiteralLabel;
import static org.batfish.specifier.parboiled.ParserUtils.isStringLiteralLabel;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests for {@link ParserUtils} */
public class ParserUtilsTest {

  @org.junit.Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(TestParser.instance().getInputRule());
  }

  private static PotentialMatch createSimplePotentialMatch(
      Anchor.Type anchorType, String label, int startIndex) {
    return createSimplePotentialMatch(anchorType, label, startIndex, "");
  }

  private static PotentialMatch createSimplePotentialMatch(
      Anchor.Type anchorType, String label, int startIndex, String matchPrefix) {
    return new PotentialMatch(
        new PathElement(anchorType, label, 0, startIndex), matchPrefix, ImmutableList.of());
  }

  private static Set<PotentialMatch> simplifyPotentialMatches(
      Set<PotentialMatch> potentialMatches) {
    return potentialMatches.stream()
        .map(ParserUtilsTest::simplifyPotentialMatch)
        .collect(ImmutableSet.toImmutableSet());
  }

  private static PotentialMatch simplifyPotentialMatch(PotentialMatch pm) {
    return createSimplePotentialMatch(
        pm.getAnchorType(),
        pm.getAnchor().getLabel(),
        pm.getAnchor().getStartIndex(),
        pm.getMatchPrefix());
  }

  /** These represent all the ways valid input can start */
  private static Set<PotentialMatch> getValidStarts(int matchStartIndex) {
    return ImmutableSet.of(
        createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", matchStartIndex),
        createSimplePotentialMatch(CHAR_LITERAL, "'!'", matchStartIndex),
        createSimplePotentialMatch(IP_ADDRESS, "TestIpAddress", matchStartIndex),
        createSimplePotentialMatch(NODE_NAME, "TestName", matchStartIndex),
        createSimplePotentialMatch(CHAR_LITERAL, "'\"'", matchStartIndex),
        createSimplePotentialMatch(CHAR_LITERAL, "'/'", matchStartIndex),
        createSimplePotentialMatch(CHAR_LITERAL, "'('", matchStartIndex));
  }

  @Test
  public void testIsStringLiteralLabel() {
    // double quoted strings map to STRING_LITERAL
    assertTrue(isStringLiteralLabel("\"@specifier\""));

    // unquoted strings do not
    assertFalse(isStringLiteralLabel("@specifier"));
  }

  @Test
  public void testIsCharLiteralLabel() {
    // single-quoted characters map to CHAR_LITERAL
    assertTrue(isCharLiteralLabel("'a'"));

    // unquoted characters do not
    assertFalse(isCharLiteralLabel("a"));
  }

  @Test
  public void testFindPathAnchorFromBottomNoAnchor() {
    assertFalse(findPathAnchorFromBottom(ImmutableList.of(), -1).isPresent());
    assertFalse(
        findPathAnchorFromBottom(ImmutableList.of(new PathElement(null, "label", 0, 0)), -1)
            .isPresent());
  }

  @Test
  public void testFindPathAnchorFromBottomCharAnchor() {
    PathElement stringElement = new PathElement(STRING_LITERAL, "label", 0, 0);
    PathElement charElement = new PathElement(CHAR_LITERAL, "label", 1, 0);
    PathElement otherElement = new PathElement(NODE_NAME, "label", 0, 0);

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
    PathElement ignoreElement = new PathElement(IGNORE, "label", 0, 0);

    assertThat(
        findPathAnchorFromBottom(ImmutableList.of(ignoreElement), 0), equalTo(Optional.empty()));
  }

  @Test
  public void testFindPathAnchorFromBottomIgnoreIsSkipped() {
    PathElement element0 = new PathElement(NODE_NAME, "label", 0, 0);
    PathElement element1 = new PathElement(IGNORE, "label", 1, 0);
    PathElement element2 = new PathElement(null, "label", 2, 0);
    PathElement element3 = new PathElement(IGNORE, "label", 3, 0);
    PathElement element4 = new PathElement(IGNORE, "label", 4, 0);

    assertThat(
        findPathAnchorFromBottom(
            ImmutableList.of(element0, element1, element2, element3, element4), 0),
        equalTo(Optional.of(element0)));
  }

  @Test
  public void testFindPathAnchorFromBottomStringAnchor() {
    PathElement element = new PathElement(STRING_LITERAL, "label", 0, 0);
    assertThat(
        findPathAnchorFromBottom(ImmutableList.of(element), 0), equalTo(Optional.of(element)));
  }

  @Test
  public void testGetErrorString() {
    assertThat(
        getErrorString(
            new PotentialMatch(
                new PathElement(STRING_LITERAL, "\"b\"", 0, 0), "a", ImmutableList.of())),
        equalTo("'b'"));
    assertThat(
        getErrorString(
            new PotentialMatch(new PathElement(IP_ADDRESS, "label", 0, 0), "", ImmutableList.of())),
        equalTo(IP_ADDRESS.toString()));
  }

  @Test
  public void testGetErrorStringHasUrl() {
    String input = new String(Character.toChars(0x26bd)); // invalid input

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.IP_SPACE_SPECIFIER))
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
  public void testGetPotentialMatchesEmpty() {
    ParsingResult<?> resultEmpty = getRunner().run("");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(getValidStarts(0)));
  }

  @Test
  public void testGetPotentialMatchesBadStart() {
    ParsingResult<?> resultEmpty =
        getRunner().run(new String(Character.toChars(ParboiledAutoComplete.ILLEGAL_CHAR)));
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(getValidStarts(0)));
  }

  @Test
  public void testGetPotentialMatchesIncompleteBase() {
    ParsingResult<?> result = getRunner().run("(1.1.1.");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(
                new PotentialMatch(
                    new PathElement(IP_ADDRESS, "TestIpAddress", 0, 1),
                    "1.1.1.",
                    ImmutableList.of()))));
  }

  @Test
  public void testGetPotentialMatchesIncompleteList() {
    ParsingResult<?> resultEmpty = getRunner().run("a,");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(getValidStarts(2)));
  }

  @Test
  public void testGetPotentialMatchesOpenParens() {
    ParsingResult<?> resultEmpty = getRunner().run("(");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(getValidStarts(1)));
  }

  @Test
  public void testGetPotentialMatchesOperator() {
    ParsingResult<?> resultEmpty = getRunner().run("!");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(getValidStarts(1)));
  }

  @Test
  public void testGetPotentialMatchesMissingCloseParens() {
    ParsingResult<?> result = getRunner().run("(1.1.1.1");

    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(
                createSimplePotentialMatch(CHAR_LITERAL, "')'", 8),
                createSimplePotentialMatch(CHAR_LITERAL, "','", 8),
                createSimplePotentialMatch(IP_ADDRESS, "TestIpAddress", 1, "1.1.1.1"),
                createSimplePotentialMatch(CHAR_LITERAL, "'-'", 8))));
  }

  @Test
  public void testGetPotentialMatchesQuoteOpenName() {
    ParsingResult<?> result = getRunner().run("\"a");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(
                createSimplePotentialMatch(CHAR_LITERAL, "'\"'", 2),
                createSimplePotentialMatch(NODE_NAME, "TestName", 0, "\"a"))));
  }

  @Test
  public void testGetPotentialMatchesSpecifierComplete() {
    ParsingResult<?> result = getRunner().run("@specifier");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(ImmutableSet.of(createSimplePotentialMatch(CHAR_LITERAL, "'('", 10))));
  }

  @Test
  public void testGetPotentialMatchesSpecifierOpenParens() {
    ParsingResult<?> result = getRunner().run("@specifier(");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(
                createSimplePotentialMatch(CHAR_LITERAL, "'\"'", 11),
                createSimplePotentialMatch(ADDRESS_GROUP_NAME, "TestAddressGroupName", 11))));
  }

  @Test
  public void testGetPotentialMatchesSpecifierOneOfPair() {
    ParsingResult<?> result = getRunner().run("@specifier(a,");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(
                createSimplePotentialMatch(CHAR_LITERAL, "'\"'", 13),
                createSimplePotentialMatch(REFERENCE_BOOK_NAME, "TestReferenceBookName", 13))));
  }

  @Test
  public void testGetPotentialMatchesSpecifierSubstring() {
    ParsingResult<?> result = getRunner().run("@specifi");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(
                createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", 0, "@specifi"))));
  }

  @Test
  public void testGetPotentialMatchesSpecifierIncorrect() {
    ParsingResult<?> result = getRunner().run("@wrong");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", 0, "@"))));
  }

  @Test
  public void testGetPotentialMatchesStringLiteralCasePreserve() {
    ParsingResult<?> result = getRunner().run("@SPeciFi");
    assertThat(
        simplifyPotentialMatches(
            getPotentialMatches(
                (InvalidInputError) result.parseErrors.get(0), TestParser.ANCHORS, false)),
        equalTo(
            ImmutableSet.of(
                createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", 0, "@SPeciFi"))));
  }
}
