package org.batfish.specifier.parboiled;

import static junit.framework.TestCase.assertTrue;
import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Test;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

public class ParboiledAutoCompleteTest {

  private static ParboiledAutoComplete getTestPAC(String query) {
    return new ParboiledAutoComplete(
        TestParser.INSTANCE,
        TestParser.INSTANCE.input(TestParser.INSTANCE.TestSpec()),
        TestParser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.builder().build(),
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  private static ParboiledAutoComplete getTestPAC(
      String query, CompletionMetadata completionMetadata) {
    return new ParboiledAutoComplete(
        TestParser.INSTANCE,
        TestParser.INSTANCE.input(TestParser.INSTANCE.TestSpec()),
        TestParser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        completionMetadata,
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  private static ParboiledAutoComplete getTestPAC(String query, ReferenceLibrary referenceLibrary) {
    return new ParboiledAutoComplete(
        TestParser.INSTANCE,
        TestParser.INSTANCE.input(TestParser.INSTANCE.TestSpec()),
        TestParser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        null,
        NodeRolesData.builder().build(),
        referenceLibrary);
  }

  /**
   * Test that we produce auto complete snapshot-based base (i.e., those that do not depend on other
   * values) dynamic values like IP addresses
   */
  @Test
  public void testRunDynamicValueBase() {
    String query = "1.1.1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1", "2.2.2.2")).build();

    // 1.1.1.1 matches, but 2.2.2.2 does not
    assertThat(
        ImmutableSet.copyOf(getTestPAC(query, completionMetadata).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "1.1.1.1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion(".", true, null, RANK_STRING_LITERAL, 5))));
  }

  /**
   * Test that we produce auto complete snapshot-based complex dynamic values (i.e., those that
   * depend on other dynamic values) like IP ranges
   */
  @Test
  public void testRunDynamicValueComplex() {
    String query = "1.1.1.1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1", "1.1.1.10")).build();

    // this should auto complete to 1.1.1.10, '-' (range), and ',' (list)
    assertThat(
        ImmutableSet.copyOf(getTestPAC(query, completionMetadata).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "1.1.1.1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion(
                    "1.1.1.10", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion("-", true, null, RANK_STRING_LITERAL, 7),
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, 7))));
  }

  /** Test that we produce auto complete snapshot-based names. */
  @Test
  public void testRunDynamicValueName() {
    String query = "node1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node10")).build();

    // this should auto complete to 1.1.1.10, '-' (range), and ',' (list)
    assertThat(
        ImmutableSet.copyOf(getTestPAC(query, completionMetadata).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "node1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion(
                    "node10", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  /** Test that we produce auto complete snapshot-based names even when we begin with a quote. */
  @Test
  public void testRunDynamicValueNameOpenQuote() {
    String query = "\"node1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node10")).build();

    // this should auto complete to 1.1.1.10, '-' (range), and ',' (list)
    assertThat(
        ImmutableSet.copyOf(getTestPAC(query, completionMetadata).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, 6),
                new AutocompleteSuggestion("\"node1\"", true, null, RANK_STRING_LITERAL, 0),
                new AutocompleteSuggestion(
                    "\"node10\"", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0))));
  }

  /** Test that we produce auto complete snapshot-based names even when we begin with a quote. */
  @Test
  public void testRunDynamicValueNameDoubleQuoted() {
    String query = "\"node1\"";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node10")).build();

    assertThat(
        ImmutableSet.copyOf(getTestPAC(query, completionMetadata).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  /** Test that we properly quote a name complex names when we offer them as suggestions. */
  @Test
  public void testRunDynamicValueNameEscaping() {
    String query = "node";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node 1", "node10")).build();

    // node10 should not be quoted and node 1 should be quoted
    assertThat(
        ImmutableSet.copyOf(getTestPAC(query, completionMetadata).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "node10", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion(
                    "\"node 1\"", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0))));
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
                    "g1,b1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 11))));
  }

  /** Test that String literals are inserted before dynamic values */
  @Test
  public void testRunStringLiteralsFirst() {
    String query = "";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1")).build();

    List<AutocompleteSuggestion> suggestions = getTestPAC(query, completionMetadata).run();

    /**
     * The first 5 elements should be string literals and the last one should be dynamic. We do a
     * 3-step dance because the ordering of string completions is non-deterministic.
     */
    assertThat(suggestions.size(), equalTo(6));
    assertThat(
        ImmutableSet.copyOf(suggestions.subList(0, 5)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("!", true, null, RANK_STRING_LITERAL, 0),
                new AutocompleteSuggestion("/", true, null, RANK_STRING_LITERAL, 0),
                new AutocompleteSuggestion("(", true, null, RANK_STRING_LITERAL, 0),
                new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, 0),
                new AutocompleteSuggestion("@specifier", true, null, RANK_STRING_LITERAL, 0))));
    assertThat(
        suggestions.get(5),
        equalTo(
            new AutocompleteSuggestion(
                "1.1.1.1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0)));
  }

  /** Test that we produce auto completion suggestions even for valid inputs */
  @Test
  public void testRunValidInput() {
    String query = "(1.1.1.1)"; //

    // first ensure that the query is valid input
    ParsingResult<?> result =
        new ReportingParseRunner<>(TestParser.INSTANCE.input(TestParser.INSTANCE.TestSpec()))
            .run(query);
    assertTrue(result.parseErrors.isEmpty());

    // commma is the only viable auto completion after a valid input
    assertThat(
        getTestPAC(query).run(),
        equalTo(
            ImmutableList.of(new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, 9))));
  }

  @Test
  public void testAutoCompletePotentialMatchStringLiteral() {
    PotentialMatch pm = new PotentialMatch(Type.STRING_LITERAL, "pfx", "pfxcomp", 0);
    assertThat(
        getTestPAC(null).autoCompletePotentialMatch(pm),
        equalTo(ImmutableList.of(new AutocompleteSuggestion("pfxcomp", true, null, -1, 0))));
  }

  /** The suggestion should have the case in the grammar token independent of user input */
  @Test
  public void testAutoCompletePotentialMatchStringLiteralCasePreserve() {
    PotentialMatch pm = new PotentialMatch(Type.STRING_LITERAL, "PfX", "pfxcomp", 0);
    assertThat(
        getTestPAC(null).autoCompletePotentialMatch(pm),
        equalTo(ImmutableList.of(new AutocompleteSuggestion("pfxcomp", true, null, -1, 0))));
  }

  @Test
  public void testAutoCompletePotentialMatchSkipLabel() {
    PotentialMatch pm = new PotentialMatch(Type.IP_ADDRESS_MASK, "pfx", "comp", 0);
    assertThat(getTestPAC(null).autoCompletePotentialMatch(pm), equalTo(ImmutableList.of()));
  }
}
