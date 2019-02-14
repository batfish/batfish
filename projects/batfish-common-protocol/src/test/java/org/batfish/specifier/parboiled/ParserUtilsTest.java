package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParserUtils.getErrorString;
import static org.batfish.specifier.parboiled.ParserUtils.getPartialMatches;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.batfish.specifier.parboiled.Completion.Type;
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

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(
        TestParser.INSTANCE.input(TestParser.INSTANCE.TestExpression()));
  }

  @SuppressWarnings({
    "checkstyle:methodname", // this class uses idiomatic names
    "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
  })
  static class TestParser extends CommonParser {

    public static final TestParser INSTANCE = Parboiled.createParser(TestParser.class);

    public static final Map<String, Type> COMPLETION_TYPES = initCompletionTypes(TestParser.class);

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
      return Sequence(TestTerm(), WhiteSpace(), ZeroOrMore(", ", TestTerm(), WhiteSpace()));
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

    @Completion(Type.ADDRESS_GROUP_AND_BOOK)
    public Rule TestSpecifierInput() {
      return Sequence(ReferenceObjectNameLiteral(), WhiteSpace());
    }

    @Completion(Type.IP_ADDRESS)
    public Rule TestBase() {
      return IpAddressUnchecked();
    }
  }

  /** These represent all the ways valid input can start */
  private Set<PartialMatch> _validStarts =
      ImmutableSet.of(
          new PartialMatch(Completion.Type.STRING_LITERAL, "", "@specifier"),
          new PartialMatch(Completion.Type.STRING_LITERAL, "", "!"),
          new PartialMatch(Type.IP_ADDRESS, "", null),
          new PartialMatch(Completion.Type.STRING_LITERAL, "", "("));

  @Test
  public void testGetErrorString() {
    assertThat(getErrorString(new PartialMatch(Type.STRING_LITERAL, "a", "b")), equalTo("'b'"));
    assertThat(
        getErrorString(new PartialMatch(Type.IP_ADDRESS, "", "b")),
        equalTo(Type.IP_ADDRESS.toString()));
  }

  @Test
  public void testGetPartialMatchesEmpty() {
    ParsingResult<?> resultEmpty = getRunner().run("");
    assertThat(
        getPartialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesBadStart() {
    ParsingResult<?> resultEmpty = getRunner().run("[");
    assertThat(
        getPartialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesIncompleteBase() {
    ParsingResult<?> result = getRunner().run("(1.1.1");
    assertThat(
        getPartialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(ImmutableSet.of(new PartialMatch(Type.IP_ADDRESS, "1.1.1", null))));
  }

  @Test
  public void testGetPartialMatchesIncompleteList() {
    ParsingResult<?> resultEmpty = getRunner().run("a,");
    assertThat(
        getPartialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesOpenParens() {
    ParsingResult<?> resultEmpty = getRunner().run("(");
    assertThat(
        getPartialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesOperator() {
    ParsingResult<?> resultEmpty = getRunner().run("!");
    assertThat(
        getPartialMatches(
            (InvalidInputError) resultEmpty.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(_validStarts));
  }

  @Test
  public void testGetPartialMatchesMissingCloseParens() {
    ParsingResult<?> result = getRunner().run("(1.1.1.1");
    assertThat(
        getPartialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(
            ImmutableSet.of(
                new PartialMatch(Type.STRING_LITERAL, "", ")"),
                new PartialMatch(Type.IP_ADDRESS, "1.1.1.1", null))));
  }

  @Test
  public void testGetPartialMatchesSpecifierComplete() {
    ParsingResult<?> result = getRunner().run("@specifier");
    assertThat(
        getPartialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(ImmutableSet.of(new PartialMatch(Completion.Type.STRING_LITERAL, "", "("))));
  }

  @Test
  public void testGetPartialMatchesSpecifierOpenParens() {
    ParsingResult<?> result = getRunner().run("@specifier(");
    assertThat(
        getPartialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(ImmutableSet.of(new PartialMatch(Type.ADDRESS_GROUP_AND_BOOK, "", null))));
  }

  @Test
  public void testGetPartialMatchesSpecifierSubstring() {
    ParsingResult<?> result = getRunner().run("@specifi");
    assertThat(
        getPartialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(
            ImmutableSet.of(new PartialMatch(Completion.Type.STRING_LITERAL, "@specifi", "er"))));
  }

  @Test
  public void testGetPartialMatchesSpecifierIncorrect() {
    ParsingResult<?> result = getRunner().run("@wrong");
    assertThat(
        getPartialMatches(
            (InvalidInputError) result.parseErrors.get(0), TestParser.COMPLETION_TYPES),
        equalTo(
            ImmutableSet.of(new PartialMatch(Completion.Type.STRING_LITERAL, "@", "specifier"))));
  }
}
