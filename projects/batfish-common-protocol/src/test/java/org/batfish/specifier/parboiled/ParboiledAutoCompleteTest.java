package org.batfish.specifier.parboiled;

import static junit.framework.TestCase.assertTrue;
import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_PROTOCOL_NOT;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_SET_OP;
import static org.batfish.specifier.parboiled.Anchor.Type.OPERATOR_END;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.UNKNOWN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.batfish.specifier.parboiled.CommonParser.ShadowStack;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.DefaultValueStack;
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
      TestParser parser, String query, CompletionMetadata completionMetadata) {
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
      TestParser parser, String query, ReferenceLibrary referenceLibrary) {
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

  private static CompletionMetadata testCompletionMetadata =
      CompletionMetadata.builder()
          .setInterfaces(
              ImmutableSet.of(
                  new NodeInterfacePair("n1a", "eth11"),
                  new NodeInterfacePair("n1a", "eth12"),
                  new NodeInterfacePair("n2a", "eth21")))
          .build();

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
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1", "2.2.2.2")).build();

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
        CompletionMetadata.builder().setIps(ImmutableSet.of("1.1.1.1", "1.1.1.10")).build();

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

  /** Throw an exception if anchor is not present in the path */
  @Test
  public void testAutoCompleteInterfaceNameMissingAnchor() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.INTERFACE_NAME, null, 0, 0), "", ImmutableList.of());

    _thrown.expect(IllegalArgumentException.class);
    getTestPAC("@specifier(g1,", testCompletionMetadata).autoCompleteInterfaceName(pm);
  }

  /** Context-sensitive completion of interface name after node name */
  @Test
  public void testAutoCompleteInterfaceNameNodeName() {
    String query = "n1a[";

    // the expected stack for the query
    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new NameNodeAstNode("n1a"));
    ShadowStack ss = new ShadowStack(vs);

    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);

    ParboiledAutoComplete pac = getTestPAC(parser, query, testCompletionMetadata);

    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, query.length());
    PathElement parent = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(parent, anchor));

    assertThat(
        pac.autoCompleteInterfaceName(pm),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("eth11", query.length(), INTERFACE_NAME),
            new ParboiledAutoCompleteSuggestion("eth12", query.length(), INTERFACE_NAME)));
  }

  /** Context-sensitive completion of interface name after node name regex */
  @Test
  public void testAutoCompleteInterfaceNameNodeNameRegex() {
    String query = "/n1/[";

    // the expected stack for the query
    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new NameRegexNodeAstNode("n1"));
    ShadowStack ss = new ShadowStack(vs);

    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);

    ParboiledAutoComplete pac = getTestPAC(parser, query, testCompletionMetadata);

    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, query.length());
    PathElement parent = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(parent, anchor));

    assertThat(
        pac.autoCompleteInterfaceName(pm),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("eth11", query.length(), INTERFACE_NAME),
            new ParboiledAutoCompleteSuggestion("eth12", query.length(), INTERFACE_NAME)));
  }

  /** Should fall back to all interfaces for complex node expressions */
  @Test
  public void testAutoCompleteInterfaceNameNodeComplex() {
    String query = "@role(a, b)";

    // the expected stack for the query
    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new RoleNodeAstNode("a", "b"));
    ShadowStack ss = new ShadowStack(vs);

    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);

    ParboiledAutoComplete pac = getTestPAC(parser, query, testCompletionMetadata);

    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, query.length());
    PathElement parent = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(parent, anchor));

    assertThat(
        pac.autoCompleteInterfaceName(pm),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("eth11", query.length(), INTERFACE_NAME),
            new ParboiledAutoCompleteSuggestion("eth12", query.length(), INTERFACE_NAME),
            new ParboiledAutoCompleteSuggestion("eth21", query.length(), INTERFACE_NAME)));
  }

  /** Context-sensitive completion of interface name when interface name prefix is present */
  @Test
  public void testAutoCompleteInterfaceNameInterfaceNamePrefix() {
    String nodePart = "n1a";

    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new NameNodeAstNode(nodePart));
    // value on stack doesn't matter; presence means something about the interface was entered.
    // actual value is taken from the matchPrefix; we try two values below
    vs.push(new StringAstNode("dummy"));

    ShadowStack ss = new ShadowStack(vs);
    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);
    ParboiledAutoComplete pac = getTestPAC(parser, "dummy", testCompletionMetadata);

    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, 42);
    PathElement parent = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);

    assertThat(
        pac.autoCompleteInterfaceName(
            new PotentialMatch(anchor, "eth12", ImmutableList.of(parent, anchor))),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("eth12", 42, INTERFACE_NAME)));

    // now with quotes, which should be preserved
    assertThat(
        pac.autoCompleteInterfaceName(
            new PotentialMatch(anchor, "\"eth12", ImmutableList.of(parent, anchor))),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("\"eth12\"", 42, INTERFACE_NAME)));
  }

  /** Throw an exception if anchor is not present in the path */
  @Test
  public void testAutoCompleteReferenceBookEntityMissingAnchor() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.REFERENCE_BOOK_NAME, null, 0, 0), "", ImmutableList.of());

    _thrown.expect(IllegalArgumentException.class);
    getTestPAC("@specifier(b1a,", testLibrary).autoCompleteReferenceBookEntity(pm);
  }

  /** Context-sensitive completion for address groups based on reference book */
  @Test
  public void testAutoCompleteReferenceBookEntityAddressGroup() {
    String query = "@specifier(b1a,";

    // the expected stack for the query
    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new StringAstNode("b1a"));
    ShadowStack ss = new ShadowStack(vs);

    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);

    ParboiledAutoComplete pac = getTestPAC(parser, query, testLibrary);

    PathElement anchor = new PathElement(Type.ADDRESS_GROUP_NAME, null, 1, query.length());
    PathElement parent = new PathElement(REFERENCE_BOOK_AND_ADDRESS_GROUP, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(parent, anchor));

    assertThat(
        pac.autoCompleteReferenceBookEntity(pm),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", query.length(), ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", query.length(), ADDRESS_GROUP_NAME)));
  }

  /** Revert to non context-sensitive completion when the contextual anchor is absent */
  @Test
  public void testAutoCompleteReferenceBookEntityAddressGroupMissingContext() {
    String query = "@specifier(b1a,";
    ParboiledAutoComplete pac = getTestPAC(TestParser.instance(), query, testLibrary);

    PathElement anchor = new PathElement(Type.ADDRESS_GROUP_NAME, null, 0, query.length());
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(anchor));

    assertThat(
        pac.autoCompleteReferenceBookEntity(pm),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("g11", query.length(), ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g12", query.length(), ADDRESS_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("g21", query.length(), ADDRESS_GROUP_NAME)));
  }

  /** Context-sensitive completion for interface groups based on reference book */
  @Test
  public void testAutoCompleteReferenceBookEntityInterfaceGroup() {
    String query = "@specifier(b1a,";

    // the expected stack for the query
    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new StringAstNode("b1a"));
    ShadowStack ss = new ShadowStack(vs);

    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);

    ParboiledAutoComplete pac = getTestPAC(parser, query, testLibrary);

    PathElement anchor = new PathElement(Type.INTERFACE_GROUP_NAME, null, 1, query.length());
    PathElement parent = new PathElement(Type.REFERENCE_BOOK_AND_INTERFACE_GROUP, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(parent, anchor));

    assertThat(
        pac.autoCompleteReferenceBookEntity(pm),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("i11", query.length(), INTERFACE_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("i12", query.length(), INTERFACE_GROUP_NAME)));
  }

  /** Revert to non context-sensitive completion when the contextual anchor is absent */
  @Test
  public void testAutoCompleteReferenceBookEntityInterfaceGroupMissingContext() {
    String query = "@specifier(b1a,";
    ParboiledAutoComplete pac = getTestPAC(TestParser.instance(), query, testLibrary);

    PathElement anchor = new PathElement(Type.INTERFACE_GROUP_NAME, null, 0, query.length());
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(anchor));

    assertThat(
        pac.autoCompleteReferenceBookEntity(pm),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("i11", query.length(), INTERFACE_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("i12", query.length(), INTERFACE_GROUP_NAME),
            new ParboiledAutoCompleteSuggestion("i21", query.length(), INTERFACE_GROUP_NAME)));
  }

  /** Group name's prefix is considered in context-sensitive autocompletion */
  @Test
  public void testAutoCompleteReferenceBookEntityPrefix() {
    ReferenceLibrary testLibrary =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("b1a")
                    .setAddressGroups(
                        ImmutableList.of(
                            new AddressGroup(null, "g1a"), new AddressGroup(null, "g2a")))
                    .build()));

    String bookPart = "b1a";

    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new StringAstNode(bookPart));
    // value on stack doesn't matter; presence means something about the group was entered.
    // actual value is taken from the matchPrefix; we try two values below
    vs.push(new StringAstNode("dummy"));

    ShadowStack ss = new ShadowStack(vs);
    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);
    ParboiledAutoComplete pac = getTestPAC(parser, "dummy", testLibrary);

    PathElement anchor = new PathElement(Type.ADDRESS_GROUP_NAME, null, 1, 42);
    PathElement parent = new PathElement(REFERENCE_BOOK_AND_ADDRESS_GROUP, null, 0, 0);

    assertThat(
        pac.autoCompleteReferenceBookEntity(
            new PotentialMatch(anchor, "g1", ImmutableList.of(parent, anchor))),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("g1a", 42, ADDRESS_GROUP_NAME)));

    // now with quotes; preserve them in the answer
    assertThat(
        pac.autoCompleteReferenceBookEntity(
            new PotentialMatch(anchor, "\"g1", ImmutableList.of(parent, anchor))),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("\"g1a\"", 42, ADDRESS_GROUP_NAME)));
  }
}
