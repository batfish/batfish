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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.batfish.specifier.Grammar;
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
  public void testGetErrorStringHasUrls() {
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
    assertThat(errorString, containsString(Grammar.GENERAL_NOTE));
  }

  // The tests below for getPotentialMatches compare a simplified form of PotentialMatch from which
  // the path and the related level information has been removed. This is done for test simplicity,
  // as comparing full paths will become unwieldy. The simplification does not reduce coverage
  // meaningfully because the path information is a direct translation of parboiled path and that
  // translation is tested elsewhere.

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

  private static Set<PotentialMatch> getSimplePotentialMatches(String query) {
    ParsingResult<?> resultEmpty = getRunner().run(query);
    return simplifyPotentialMatches(
        getPotentialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.ANCHORS, false));
  }

  /** These represent all the ways valid input can start */
  private static Set<PotentialMatch> getValidStarts(int matchStartIndex) {
    return ImmutableSet.of(
        createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", matchStartIndex),
        createSimplePotentialMatch(CHAR_LITERAL, "'!'", matchStartIndex),
        createSimplePotentialMatch(IP_ADDRESS, "TestIpAddress", matchStartIndex),
        createSimplePotentialMatch(NODE_NAME, "TestName", matchStartIndex),
        // createSimplePotentialMatch(CHAR_LITERAL, "'\"'", matchStartIndex),
        createSimplePotentialMatch(CHAR_LITERAL, "'/'", matchStartIndex),
        createSimplePotentialMatch(CHAR_LITERAL, "'('", matchStartIndex));
  }

  @Test
  public void testGetPotentialMatchesEmpty() {
    assertThat(getSimplePotentialMatches(""), equalTo(getValidStarts(0)));
    assertThat(getSimplePotentialMatches(" "), equalTo(getValidStarts(1)));
  }

  @Test
  public void testGetPotentialMatchesDeprecated() {
    assertThat(
        getSimplePotentialMatches("(.*"),
        containsInAnyOrder(
            createSimplePotentialMatch(CHAR_LITERAL, "\',\'", 3),
            createSimplePotentialMatch(CHAR_LITERAL, "\')\'", 3)));
    assertThat(
        getSimplePotentialMatches("(.* "),
        containsInAnyOrder(
            createSimplePotentialMatch(CHAR_LITERAL, "\',\'", 4),
            createSimplePotentialMatch(CHAR_LITERAL, "\')\'", 4)));
  }

  @Test
  public void testGetPotentialMatchesBadStart() {
    assertThat(
        getSimplePotentialMatches(
            new String(Character.toChars(ParboiledAutoComplete.ILLEGAL_CHAR))),
        equalTo(getValidStarts(0)));
  }

  @Test
  public void testGetPotentialMatchesIncompleteBase() {
    assertThat(
        getSimplePotentialMatches("(1.1.1."),
        containsInAnyOrder(createSimplePotentialMatch(IP_ADDRESS, "TestIpAddress", 1, "1.1.1.")));
    // because we didn't finish parsing the IP, we try to match on its prefix
    assertThat(
        getSimplePotentialMatches("(1.1.1. "),
        containsInAnyOrder(createSimplePotentialMatch(IP_ADDRESS, "TestIpAddress", 1, "1.1.1.")));
  }

  @Test
  public void testGetPotentialMatchesIncompleteList() {
    assertThat(getSimplePotentialMatches("a,"), equalTo(getValidStarts(2)));
    assertThat(getSimplePotentialMatches("a, "), equalTo(getValidStarts(3)));
  }

  @Test
  public void testGetPotentialMatchesOpenParens() {
    assertThat(getSimplePotentialMatches("("), equalTo(getValidStarts(1)));
    assertThat(getSimplePotentialMatches("( "), equalTo(getValidStarts(2)));
  }

  @Test
  public void testGetPotentialMatchesOperator() {
    assertThat(getSimplePotentialMatches("!"), equalTo(getValidStarts(1)));
    assertThat(getSimplePotentialMatches("! "), equalTo(getValidStarts(2)));
  }

  @Test
  public void testGetPotentialMatchesMissingCloseParens() {
    assertThat(
        getSimplePotentialMatches("(1.1.1.1"),
        containsInAnyOrder(
            createSimplePotentialMatch(CHAR_LITERAL, "')'", 8),
            createSimplePotentialMatch(CHAR_LITERAL, "','", 8),
            createSimplePotentialMatch(IP_ADDRESS, "TestIpAddress", 1, "1.1.1.1"),
            createSimplePotentialMatch(CHAR_LITERAL, "'-'", 8)));
    // we finished parsing the IP address, so that token is considered done and is option
    assertThat(
        getSimplePotentialMatches("(1.1.1.1 "),
        containsInAnyOrder(
            createSimplePotentialMatch(CHAR_LITERAL, "')'", 9),
            createSimplePotentialMatch(CHAR_LITERAL, "','", 9),
            createSimplePotentialMatch(CHAR_LITERAL, "'-'", 9)));
  }

  @Test
  public void testGetPotentialMatchesQuoteOpenName() {
    assertThat(
        getSimplePotentialMatches("\"a"),
        containsInAnyOrder(createSimplePotentialMatch(NODE_NAME, "TestName", 0, "\"a")));
    // the trailing space is preserved in the match prefix
    assertThat(
        getSimplePotentialMatches("\"a "),
        containsInAnyOrder(createSimplePotentialMatch(NODE_NAME, "TestName", 0, "\"a ")));
  }

  @Test
  public void testGetPotentialMatchesSpecifierComplete() {
    assertThat(
        getSimplePotentialMatches("@specifier"),
        equalTo(ImmutableSet.of(createSimplePotentialMatch(CHAR_LITERAL, "'('", 10))));
    assertThat(
        getSimplePotentialMatches("@specifier "),
        equalTo(ImmutableSet.of(createSimplePotentialMatch(CHAR_LITERAL, "'('", 11))));
  }

  @Test
  public void testGetPotentialMatchesSpecifierOpenParens() {
    assertThat(
        getSimplePotentialMatches("@specifier("),
        containsInAnyOrder(
            createSimplePotentialMatch(REFERENCE_BOOK_NAME, "TestReferenceBookName", 11)));
    assertThat(
        getSimplePotentialMatches("@specifier( "),
        containsInAnyOrder(
            createSimplePotentialMatch(REFERENCE_BOOK_NAME, "TestReferenceBookName", 12)));
  }

  @Test
  public void testGetPotentialMatchesSpecifierOneOfPair() {
    assertThat(
        getSimplePotentialMatches("@specifier(a,"),
        containsInAnyOrder(
            createSimplePotentialMatch(ADDRESS_GROUP_NAME, "TestAddressGroupName", 13)));
    assertThat(
        getSimplePotentialMatches("@specifier(a, "),
        containsInAnyOrder(
            createSimplePotentialMatch(ADDRESS_GROUP_NAME, "TestAddressGroupName", 14)));
  }

  @Test
  public void testGetPotentialMatchesSpecifierSubstring() {
    assertThat(
        getSimplePotentialMatches("@specifi"),
        containsInAnyOrder(
            createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", 0, "@specifi")));
    // the trailing whitespace does not prevent us from completing the specifier
    assertThat(
        getSimplePotentialMatches("@specifi "),
        containsInAnyOrder(
            createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", 0, "@specifi")));
  }

  @Test
  public void testGetPotentialMatchesSpecifierIncorrect() {
    assertThat(
        getSimplePotentialMatches("@wrong"),
        containsInAnyOrder(createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", 0, "@")));
  }

  @Test
  public void testGetPotentialMatchesStringLiteralCasePreserve() {
    assertThat(
        getSimplePotentialMatches("@SPeciFi"),
        containsInAnyOrder(
            createSimplePotentialMatch(STRING_LITERAL, "\"@specifier\"", 0, "@SPeciFi")));
  }
}
