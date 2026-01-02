package org.batfish.specifier.parboiled;

import static junit.framework.TestCase.assertTrue;
import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_PROTOCOL_NOT;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_INTERFACE;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_INTERFACE_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_SET_OP;
import static org.batfish.specifier.parboiled.Anchor.Type.OPERATOR_END;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.UNKNOWN;
import static org.batfish.specifier.parboiled.ParboiledAutoComplete.updateSuggestions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.AutocompleteSuggestion.SuggestionType;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests for {@link ParboiledAutoComplete} */
public class ParboiledAutoCompleteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static ParboiledAutoComplete getTestPAC(String query) {
    return new ParboiledAutoComplete(
        TestParser.instance(),
        Grammar.NODE_SPECIFIER,
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
    return getTestPAC(TestParser.instance(), query, completionMetadata);
  }

  private static ParboiledAutoComplete getTestPAC(
      CommonParser parser, String query, CompletionMetadata completionMetadata) {
    return new ParboiledAutoComplete(
        parser,
        Grammar.NODE_SPECIFIER,
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
    return getTestPAC(TestParser.instance(), query, referenceLibrary);
  }

  private static ParboiledAutoComplete getTestPAC(
      CommonParser parser, String query, ReferenceLibrary referenceLibrary) {
    return new ParboiledAutoComplete(
        parser,
        Grammar.NODE_SPECIFIER,
        TestParser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.builder().build(),
        NodeRolesData.builder().build(),
        referenceLibrary);
  }

  private static ReferenceLibrary testLibrary =
      new ReferenceLibrary(
          ImmutableList.of(
              ReferenceBook.builder("b1a")
                  .setAddressGroups(
                      ImmutableList.of(
                          new AddressGroup(null, "g11"), new AddressGroup(null, "g12")))
                  .setInterfaceGroups(
                      ImmutableList.of(
                          new InterfaceGroup(ImmutableSortedSet.of(), "i11"),
                          new InterfaceGroup(ImmutableSortedSet.of(), "i12")))
                  .build(),
              ReferenceBook.builder("b2a")
                  .setAddressGroups(ImmutableList.of(new AddressGroup(null, "g21")))
                  .setInterfaceGroups(
                      ImmutableList.of(new InterfaceGroup(ImmutableSortedSet.of(), "i21")))
                  .build()));

  @Test
  public void testCompletionEmpty() {
    String query = "";

    assertThat(
        getTestPAC(query).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("(", query.length(), NODE_PARENS),
            new ParboiledAutoCompleteSuggestion("!", query.length(), IP_PROTOCOL_NOT),
            new ParboiledAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParboiledAutoCompleteSuggestion(
                "@specifier(", query.length(), REFERENCE_BOOK_AND_ADDRESS_GROUP)));
  }

  /**
   * Test that we produce auto complete snapshot-based base (i.e., those that do not depend on other
   * values) dynamic values like IP addresses
   */
  @Test
  public void testRunDynamicValueBase() {
    String query = "1.1.1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setIps(ImmutableSet.of(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")))
            .build();

    // 1.1.1.1 matches, but 2.2.2.2 does not
    assertThat(
        ImmutableSet.copyOf(getTestPAC(query, completionMetadata).run()),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("1.1.1.1", 0, IP_ADDRESS)));
  }

  /**
   * Test that we produce auto complete snapshot-based complex dynamic values (i.e., those that
   * depend on other dynamic values) like IP ranges
   */
  @Test
  public void testRunDynamicValueComplex() {
    String query = "1.1.1.1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setIps(ImmutableSet.of(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.10")))
            .build();

    // this should auto complete to 1.1.1.10, '-' (range), and ',' (list)
    assertThat(
        getTestPAC(query, completionMetadata).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("1.1.1.1", 0, IP_ADDRESS),
            new ParboiledAutoCompleteSuggestion("1.1.1.10", 0, IP_ADDRESS),
            new ParboiledAutoCompleteSuggestion("-", 7, IP_RANGE),
            new ParboiledAutoCompleteSuggestion(",", 7, NODE_SET_OP)));
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
                new ParboiledAutoCompleteSuggestion("node1", 0, NODE_NAME),
                new ParboiledAutoCompleteSuggestion("node10", 0, NODE_NAME),
                new ParboiledAutoCompleteSuggestion(",", query.length(), NODE_SET_OP))));
  }

  /** Test that we produce auto complete snapshot-based names even when we begin with a quote. */
  @Test
  public void testRunDynamicValueNameOpenQuote() {
    String query = "\"node1";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node10")).build();

    // this should auto complete to 1.1.1.10, '-' (range), and ',' (list)
    assertThat(
        getTestPAC(query, completionMetadata).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("\"node1\"", 0, NODE_NAME),
            new ParboiledAutoCompleteSuggestion("\"node10\"", 0, NODE_NAME)));
  }

  /** Test that we produce auto complete snapshot-based names even when we begin with a quote. */
  @Test
  public void testRunDynamicValueNameDoubleQuoted() {
    String query = "\"node1\"";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node10")).build();

    assertThat(
        getTestPAC(query, completionMetadata).run(),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion(",", query.length(), NODE_SET_OP)));
  }

  /** Test that we properly quote a name complex names when we offer them as suggestions. */
  @Test
  public void testRunDynamicValueNameEscaping() {
    String query = "node";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node 1", "node10")).build();

    // node10 should not be quoted and node 1 should be quoted
    assertThat(
        getTestPAC(query, completionMetadata).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(",", query.length(), NODE_SET_OP),
            new ParboiledAutoCompleteSuggestion("node10", 0, NODE_NAME),
            new ParboiledAutoCompleteSuggestion("\"node 1\"", 0, NODE_NAME)));
  }

  /** Test that we produce an empty suggestion after a '/' that opens a regex */
  @Test
  public void testRunRegexEmpty() {
    String query = "/";

    assertThat(
        getTestPAC(query).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("", query.length(), NODE_NAME_REGEX)));
  }

  /** Test that we produce an empty suggestion after a '/aa' that denotes a partial regex */
  @Test
  public void testRunRegexPartial() {
    String query = "/aa";

    assertThat(
        getTestPAC(query).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParboiledAutoCompleteSuggestion("", query.length(), NODE_NAME_REGEX)));
  }

  /** Test that we auto complete partial specifier names */
  @Test
  public void testRunSpecifierPartial() {
    assertThat(
        getTestPAC("@specifie", testLibrary).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(
                "@specifier(", 0, REFERENCE_BOOK_AND_ADDRESS_GROUP)));
  }

  /** Test that we auto complete specifier names */
  @Test
  public void testRunSpecifierFullWithoutParens() {
    assertThat(
        getTestPAC("@specifier", testLibrary).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("(", 10, REFERENCE_BOOK_AND_ADDRESS_GROUP)));
  }

  /** Test that we auto complete snapshot-based dynamic values like reference books */
  @Test
  public void testRunSpecifierWithParensNoInput() {
    assertThat(
        getTestPAC("@specifier(", testLibrary).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("b1a", 11, REFERENCE_BOOK_NAME),
            new ParboiledAutoCompleteSuggestion("b2a", 11, REFERENCE_BOOK_NAME)));
  }

  @Test
  public void testRunSpecifierOneInputNoComma() {
    assertThat(
        getTestPAC("@specifier(b1", testLibrary).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("b1a", 11, REFERENCE_BOOK_NAME),
            new ParboiledAutoCompleteSuggestion(",", 13, REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL)));
  }

  @Test
  public void testRunSpecifierOneInputCommaNoRefbookMatch() {
    // nothing should match since b is not a reference book in the data
    assertThat(getTestPAC("@specifier(b,", testLibrary).run(), containsInAnyOrder());
  }

  /** Test that we auto complete prefixes of snapshot-based dynamic values like reference books */
  @Test
  public void testRunSpecifierFirstPartialInput() {
    assertThat(
        getTestPAC("@specifier(b1", testLibrary).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("b1a", 11, REFERENCE_BOOK_NAME),
            new ParboiledAutoCompleteSuggestion(",", 13, REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL)));
  }

  /** Test that we auto complete in a context-sensitive manner */
  @Test
  public void testRunSpecifierAfterFirstInput() {
    assertThat(
        getTestPAC("@specifier(b1a,", testLibrary).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", 15, ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", 15, ADDRESS_GROUP_NAME)));
  }

  /** Test that we auto complete in a context-sensitive manner while accounting for prefix */
  @Test
  public void testRunSpecifierPartialSecondInput() {
    String query = "@specifier(b1a, g";
    assertThat(
        getTestPAC(query, testLibrary).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", query.length() - 1, ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", query.length() - 1, ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END)));
  }

  /** Test that we produce auto completion suggestions even for valid inputs */
  @Test
  public void testRunValidInput() {
    String query = "(1.1.1.1)"; //

    // first ensure that the query is valid input
    ParsingResult<?> result =
        new ReportingParseRunner<>(TestParser.instance().input(TestParser.instance().TestSpec()))
            .run(query);
    assertTrue(result.parseErrors.isEmpty());

    // commma is the only viable auto completion after a valid input
    assertThat(
        getTestPAC(query).run(),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion(",", 9, NODE_SET_OP)));
  }

  @Test
  public void testRunOpenParen() {
    String query = "("; //

    // these should be the same as empty input ones
    assertThat(
        getTestPAC(query).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("(", query.length(), NODE_PARENS),
            new ParboiledAutoCompleteSuggestion("!", query.length(), IP_PROTOCOL_NOT),
            new ParboiledAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParboiledAutoCompleteSuggestion(
                "@specifier(", query.length(), REFERENCE_BOOK_AND_ADDRESS_GROUP)));
  }

  @Test
  public void testRunOpenParenWithValidInput() {
    String query = "(a"; //

    // these should be the same as empty input ones
    assertThat(
        getTestPAC(query).run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(",", query.length(), NODE_SET_OP),
            new ParboiledAutoCompleteSuggestion(")", query.length(), OPERATOR_END)));
  }

  @Test
  public void testAutoCompletePotentialMatchStringLiteral() {
    PathElement anchor = new PathElement(Type.STRING_LITERAL, "\"pfxcomp\"", 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "pfx", ImmutableList.of(anchor));
    assertThat(
        getTestPAC("pfx").autoCompletePotentialMatch(pm),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("pfxcomp", 0, UNKNOWN)));
  }

  /** The suggestion should have the case in the grammar token independent of user input */
  @Test
  public void testAutoCompletePotentialMatchStringLiteralCasePreserve() {
    PathElement anchor = new PathElement(Type.STRING_LITERAL, "\"pfxcomp\"", 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "PfX", ImmutableList.of(anchor));
    assertThat(
        getTestPAC("PfX").autoCompletePotentialMatch(pm),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("pfxcomp", 0, UNKNOWN)));
  }

  @Test
  public void testAutoCompletePotentialMatchSkipLabel() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.IP_ADDRESS_MASK, "label", 0, 0), "pfx", ImmutableList.of());
    assertThat(getTestPAC(null).autoCompletePotentialMatch(pm), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testFindFirstMatchingPathElement() {
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PathElement tailStart = new PathElement(Type.NODE_AND_INTERFACE_TAIL, null, 0, 42);
    PathElement nodeStart = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 42);
    PathElement iname2 = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PathElement tail2 = new PathElement(Type.NODE_AND_INTERFACE_TAIL, null, 0, 42);
    PotentialMatch pm =
        new PotentialMatch(
            anchor, "", ImmutableList.of(tail2, iname2, nodeStart, tailStart, anchor));

    // should return the tail close to the anchor
    assertThat(
        ParboiledAutoComplete.findFirstMatchingPathElement(pm, 2, NODE_AND_INTERFACE_TAIL),
        equalTo(Optional.of(tailStart)));

    // non-existent anchor type
    assertThat(
        ParboiledAutoComplete.findFirstMatchingPathElement(pm, 2, NODE_NAME),
        equalTo(Optional.empty()));

    // should return the other interface_name
    assertThat(
        ParboiledAutoComplete.findFirstMatchingPathElement(pm, 2, INTERFACE_NAME),
        equalTo(Optional.of(iname2)));
  }

  /** Throw an exception if anchor is not present in the path */
  @Test
  public void testFindPrecedingInputMissingAnchor() {
    _thrown.expect(IllegalArgumentException.class);
    ParboiledAutoComplete.findPrecedingInput(
        new PotentialMatch(
            new PathElement(Type.INTERFACE_NAME, null, 0, 0), "", ImmutableList.of()),
        "@specifier(g1,",
        NODE_AND_INTERFACE,
        NODE_AND_INTERFACE_TAIL);
  }

  /** Return empty optional if head anchor type is not present */
  @Test
  public void testFindPrecedingInpuMissingHead() {
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(anchor));
    assertFalse(
        ParboiledAutoComplete.findPrecedingInput(
                pm, "dummy", NODE_AND_INTERFACE, NODE_AND_INTERFACE_TAIL)
            .isPresent());
  }

  /** Throw an exception if tail anchor type is missing */
  @Test
  public void testFindPrecedingInputMissingTail() {
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PathElement nodeStart = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(nodeStart, anchor));

    _thrown.expect(IllegalArgumentException.class);
    ParboiledAutoComplete.findPrecedingInput(
        pm, "dummy", NODE_AND_INTERFACE, NODE_AND_INTERFACE_TAIL);
  }

  /** Get the proper input if both head and tail are present */
  @Test
  public void testFindPrecedingInput() {
    String query = "n1a[";
    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, query.length());
    PathElement tailStart = new PathElement(Type.NODE_AND_INTERFACE_TAIL, null, 0, 3);
    PathElement nodeStart = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm =
        new PotentialMatch(anchor, "", ImmutableList.of(nodeStart, tailStart, anchor));

    assertThat(
        ParboiledAutoComplete.findPrecedingInput(
            pm, query, NODE_AND_INTERFACE, NODE_AND_INTERFACE_TAIL),
        Matchers.equalTo(Optional.of("n1a")));
  }

  @Test
  public void testCompleteNodeNameWithHumanNameHint() {
    String query = "node";
    // these should be the same as empty input ones
    String node1 = "node1";
    String node1HumanName = "humanName";
    String node2 = "node2";
    CompletionMetadata metadata =
        CompletionMetadata.builder()
            .setNodes(
                ImmutableMap.of(
                    node1,
                    new NodeCompletionMetadata(node1HumanName),
                    node2,
                    new NodeCompletionMetadata(null)))
            .build();
    assertThat(
        getTestPAC(query, metadata).run(),
        allOf(
            hasItem(
                new ParboiledAutoCompleteSuggestion(
                    node1, NODE_NAME.getHint(), 0, NODE_NAME, node1HumanName)),
            hasItem(
                new ParboiledAutoCompleteSuggestion(node2, NODE_NAME.getHint(), 0, NODE_NAME))));
  }

  @Test
  public void testCompleteHumanName() {
    String query = "human";
    // these should be the same as empty input ones
    String node1 = "node1";
    String node1HumanName = "humanName1";
    String node2 = "node2";
    CompletionMetadata metadata =
        CompletionMetadata.builder()
            .setNodes(
                ImmutableMap.of(
                    node1,
                    new NodeCompletionMetadata(node1HumanName),
                    node2,
                    new NodeCompletionMetadata(null)))
            .build();
    assertThat(
        getTestPAC(query, metadata).run(),
        allOf(
            hasItem(
                new ParboiledAutoCompleteSuggestion(node1, null, 0, NODE_NAME, node1HumanName))));
  }

  /**
   * Tests that relevants fields are preserved when we covert {@link AutocompleteSuggestion} to
   * {@link ParboiledAutoCompleteSuggestion}.
   */
  @Test
  public void testUpdateSuggestions() {
    AutocompleteSuggestion autocompleteSuggestion =
        new AutocompleteSuggestion("text", SuggestionType.UNKNOWN, true, "desc", 42, 3, "hint");
    assertThat(
        updateSuggestions(ImmutableList.of(autocompleteSuggestion), false, NODE_NAME, 0),
        equalTo(
            ImmutableSet.of(
                new ParboiledAutoCompleteSuggestion("text", "hint", 0, NODE_NAME, "desc"))));
  }
}
