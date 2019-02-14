package org.batfish.specifier.parboiled;

import static junit.framework.TestCase.assertTrue;
import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
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
      return Sequence(
          ReferenceObjectNameLiteral(),
          WhiteSpace(),
          ", ",
          ReferenceObjectNameLiteral(),
          WhiteSpace());
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

  static ParboiledAutoComplete getTestPAC(String query, CompletionMetadata completionMetadata) {
    return new ParboiledAutoComplete(
        TestParser.INSTANCE,
        TestParser.INSTANCE.TestExpression(),
        TestParser.COMPLETION_TYPES,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        completionMetadata,
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  static ParboiledAutoComplete getTestPAC(String query, ReferenceLibrary referenceLibrary) {
    return new ParboiledAutoComplete(
        TestParser.INSTANCE,
        TestParser.INSTANCE.TestExpression(),
        TestParser.COMPLETION_TYPES,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        null,
        NodeRolesData.builder().build(),
        referenceLibrary);
  }

  /** Test that we produce auto complete snapshot-based dynamic values like IP addresses */
  @Test
  public void testRunDynamicValues() {
    String query = "1.1.1.";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1", "2.2.2.2")).build();

    // 1.1.1.1 matches, but 2.2.2.2 does not
    assertThat(
        getTestPAC(query, completionMetadata).run(),
        equalTo(
            ImmutableList.of(
                new AutocompleteSuggestion(
                    "1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 6))));
  }

  /** Test that we produce auto complete snapshot-based dynamic values like IP addresses */
  @Test
  public void testRunSpecifierInput() {
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("b1")
                    .setAddressGroups(
                        ImmutableList.of(
                            new AddressGroup(null, "g1"), new AddressGroup(null, "a1")))
                    .build(),
                ReferenceBook.builder("b2")
                    .setAddressGroups(ImmutableList.of(new AddressGroup(null, "g2")))
                    .build()));

    assertThat(
        ImmutableSet.copyOf(getTestPAC("@specifier(", library).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "g1,b1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 11),
                new AutocompleteSuggestion(
                    "a1,b1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 11),
                new AutocompleteSuggestion(
                    "g2,b2", true, null, AutocompleteSuggestion.DEFAULT_RANK, 11))));

    assertThat(
        ImmutableSet.copyOf(getTestPAC("@specifier(g1,", library).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "b1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 14))));
  }

  /** Test that String literals are inserted before dynamic values */
  @Test
  public void testRunStringLiteralsFirst() {
    String query = "";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1")).build();

    List<AutocompleteSuggestion> suggestions = getTestPAC(query, completionMetadata).run();

    /**
     * The first three elements should should string literals and the last one should be dynamic. We
     * do a 3-step dance to assert this because the ordering of first three completions is
     * non-deterministic.
     */
    assertThat(suggestions.size(), equalTo(4));
    assertThat(
        ImmutableSet.copyOf(suggestions.subList(0, 3)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("!", true, null, RANK_STRING_LITERAL, 0),
                new AutocompleteSuggestion("(", true, null, RANK_STRING_LITERAL, 0),
                new AutocompleteSuggestion("@specifier", true, null, RANK_STRING_LITERAL, 0))));
    assertThat(
        suggestions.get(3),
        equalTo(
            new AutocompleteSuggestion(
                "1.1.1.1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0)));
  }

  /** Test that we produce auto completion suggestions even for valid inputs */
  @Test
  public void testRunValidInput() {
    String query = "1.1.1.1 "; // space at the end means that ip adresses won't match

    // first ensure that the query is valid input
    ParsingResult<?> result =
        new ReportingParseRunner<>(TestParser.INSTANCE.input(TestParser.INSTANCE.TestExpression()))
            .run(query);
    assertTrue(result.parseErrors.isEmpty());

    // commma is the only viable auto completion after a valid input
    assertThat(
        getTestPAC(query).run(),
        equalTo(
            ImmutableList.of(new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, 8))));
  }

  @Test
  public void testAutoCompletePartialMatchIpRange() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1", "2.2.2.2")).build();

    assertThat(
        getTestPAC("1.1.1.", completionMetadata)
            .autoCompletePartialMatch(new PartialMatch(Type.IP_RANGE, "1.1.1.", null), 2),
        equalTo(ImmutableList.of()));

    assertThat(
        ImmutableSet.copyOf(
            getTestPAC("1.1.1.", completionMetadata)
                .autoCompletePartialMatch(new PartialMatch(Type.IP_RANGE, "1.1.1.1 - ", null), 2)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "1.1.1.1-1.1.1.1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 2),
                new AutocompleteSuggestion(
                    "1.1.1.1-2.2.2.2", true, null, AutocompleteSuggestion.DEFAULT_RANK, 2))));

    assertThat(
        ImmutableSet.copyOf(
            getTestPAC("1.1.1.", completionMetadata)
                .autoCompletePartialMatch(new PartialMatch(Type.IP_RANGE, "1.1.1.1 - 2", null), 2)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "1.1.1.1-2.2.2.2", true, null, AutocompleteSuggestion.DEFAULT_RANK, 2))));
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
    PartialMatch pm = new PartialMatch(Type.IP_WILDCARD, "pfx", "comp");
    assertThat(getTestPAC(null).autoCompletePartialMatch(pm, 2), equalTo(ImmutableList.of()));
  }
}
