package org.batfish.specifier.parboiled;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.parboiled.Completion.Type;
import org.junit.Test;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class ParboiledAutoCompleteTest {

  @SuppressWarnings({
    "InfiniteRecursion",
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
     * testTerm := @specifier(specifierInput)
     *               | (testTerm)
     *               | ! testTerm
     *               | testBase
     *
     * specifierInput := REFERENCE_OBJECT_NAME_LITERAL
     *
     * testBase := IP_ADDRESS
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

    @Completion(Type.ADDRESS_GROUP_AND_BOOK)
    public Rule TestSpecifierInput() {
      return Sequence(ReferenceObjectNameLiteral(), WhiteSpace());
    }

    @Completion(Type.IP_ADDRESS)
    public Rule TestBase() {
      return IpAddressUnchecked();
    }
  }

  static ParboiledAutoComplete getTestPAC(String query) {
    return new ParboiledAutoComplete(
        TestParser.INSTANCE,
        TestParser.INSTANCE.TestExpression(),
        TestParser.COMPLETION_TYPES,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.builder().build(),
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  /** Test that we produce auto completion suggestions even for valid inputs */
  @Test
  public void testRunForceAutoComplete() {
    String query = "1.1.1.1";

    // first ensure that the query is valid input
    ParsingResult<?> result =
        new ReportingParseRunner<>(TestParser.INSTANCE.input(TestParser.INSTANCE.TestExpression()))
            .run(query);
    assertTrue(result.parseErrors.isEmpty());

    assertThat(
        getTestPAC(query).run(),
        equalTo(ImmutableList.of(new AutocompleteSuggestion(",", true, null, -1, 7))));
  }

  @Test
  public void testAutoCompletePartialMatchStringLiteral() {
    PartialMatch pm = new PartialMatch(Type.STRING_LITERAL, "pfx", "comp");
    assertThat(
        getTestPAC(null).autoCompletePartialMatch(pm, 2),
        equalTo(ImmutableList.of(new AutocompleteSuggestion("comp", true, null, -1, 2))));
  }

  @Test
  public void testAutoCompletePartialMatchSkipLabel() {
    PartialMatch pm = new PartialMatch(Type.IP_RANGE, "pfx", "comp");
    assertThat(getTestPAC(null).autoCompletePartialMatch(pm, 2), equalTo(ImmutableList.of()));
  }
}
