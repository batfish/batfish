package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParserUtils.STRING_LITERAL_LABEL;
import static org.batfish.specifier.parboiled.ParserUtils.getErrorString;
import static org.batfish.specifier.parboiled.ParserUtils.getPartialMatches;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class ParserUtilsTest {

  @org.junit.Rule public ExpectedException _thrown = ExpectedException.none();

  private static TestParser _parser = Parboiled.createParser(TestParser.class);

  private static AbstractParseRunner<?> getRunner() {
    return new ReportingParseRunner<>(_parser.input(_parser.TestExpression()));
  }

  @SuppressWarnings({
    "InfiniteRecursion",
    "checkstyle:methodname", // this class uses idiomatic names
    "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
  })
  public static class TestParser extends CommonParser {

    public Rule input(Rule expression) {
      return Sequence(WhiteSpace(), expression, WhiteSpace(), EOI);
    }

    /**
     * Test grammar
     *
     * <pre>
     * testExpr := testTerm [, testTerm]*
     *
     * testTerm := specifier(argument)
     *               | (testTerm)
     *               | ! testTerm
     *               | testBase
     * </pre>
     */

    /* An Test expression is a comma-separated list of TestTerms */
    public Rule TestExpression() {
      return Sequence(TestTerm(), WhiteSpace(), ZeroOrMore(", ", TestTerm(), WhiteSpace(), EOI));
    }

    /* An Test term is one of these things */
    public Rule TestTerm() {
      return FirstOf(TestParens(), TestSpecifier(), TestNotOp(), TestBase());
    }

    public Rule TestParens() {
      return Sequence("( ", TestTerm(), ") ");
    }

    public Rule TestSpecifier() {
      return Sequence("@specifier ", "( ", TestSpecifierInput(), ") ");
    }

    public Rule TestNotOp() {
      return Sequence("! ", TestNot("! "), TestTerm());
    }

    public Rule TestSpecifierInput() {
      return Sequence(ReferenceObjectNameLiteral(), WhiteSpace());
    }

    public Rule TestBase() {
      return ReferenceObjectNameLiteral();
    }
  }

  /** These represent all the ways valid input can start */
  private Set<PartialMatch> _validStarts =
      ImmutableSet.of(
          new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "", "@specifier"),
          new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "", "!"),
          new PartialMatch("TestBase", "", null),
          new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "", "("));

  @Test
  public void testGetErrorString() {
    assertThat(getErrorString(new PartialMatch(STRING_LITERAL_LABEL, "a", "b")), equalTo("'b'"));
    assertThat(getErrorString(new PartialMatch("TestBase", "", "b")), equalTo("TestBase"));
    assertThat(
        getErrorString(new PartialMatch("TestBase", "a", null)),
        equalTo("TestBase starting with 'a'"));
  }

  @Test
  public void testGetPartialMatchesEmpty() {
    ParsingResult<?> resultEmpty = getRunner().run("");
    assertThat(
        getPartialMatches((InvalidInputError) resultEmpty.parseErrors.get(0)),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesBadStart() {
    ParsingResult<?> resultEmpty = getRunner().run("[");
    assertThat(
        getPartialMatches((InvalidInputError) resultEmpty.parseErrors.get(0)),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesDanglingList() {
    ParsingResult<?> resultEmpty = getRunner().run("a,");
    assertThat(
        getPartialMatches((InvalidInputError) resultEmpty.parseErrors.get(0)),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesOpenParens() {
    ParsingResult<?> resultEmpty = getRunner().run("(");
    assertThat(
        getPartialMatches((InvalidInputError) resultEmpty.parseErrors.get(0)),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesOperator() {
    ParsingResult<?> resultEmpty = getRunner().run("!");
    assertThat(
        getPartialMatches((InvalidInputError) resultEmpty.parseErrors.get(0)),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesMissingCloseParens() {
    ParsingResult<?> result = getRunner().run("(a");
    assertThat(
        getPartialMatches((InvalidInputError) result.parseErrors.get(0)),
        equalTo(
            ImmutableSet.of(
                new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "", ")"),
                new PartialMatch("TestBase", "a", null))));
  }

  @Test
  public void testGetPartialMatchesSpecifierComplete() {
    ParsingResult<?> result = getRunner().run("@specifier");
    assertThat(
        getPartialMatches((InvalidInputError) result.parseErrors.get(0)),
        equalTo(ImmutableSet.of(new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "", "("))));
  }

  @Test
  public void testGetPartialMatchesSpecifierOpenParens() {
    ParsingResult<?> result = getRunner().run("@specifier(");
    assertThat(
        getPartialMatches((InvalidInputError) result.parseErrors.get(0)),
        equalTo(ImmutableSet.of(new PartialMatch("TestSpecifierInput", "", null))));
  }

  @Test
  public void testGetPartialMatchesSpecifierSubstring() {
    ParsingResult<?> result = getRunner().run("@specifi");
    assertThat(
        getPartialMatches((InvalidInputError) result.parseErrors.get(0)),
        equalTo(
            ImmutableSet.of(new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "@specifi", "er"))));
  }

  @Test
  public void testGetPartialMatchesSpecifierIncorrect() {
    ParsingResult<?> result = getRunner().run("@wrong");
    assertThat(
        getPartialMatches((InvalidInputError) result.parseErrors.get(0)),
        equalTo(
            ImmutableSet.of(new PartialMatch(ParserUtils.STRING_LITERAL_LABEL, "@", "specifier"))));
  }
}
