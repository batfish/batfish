package org.batfish.specifier.parboiled;

import static junit.framework.TestCase.assertTrue;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP;
import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.batfish.specifier.parboiled.CommonParser.ShadowStack;
import org.hamcrest.Matchers;
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
    TestParser parser = TestParser.instance();
    return new ParboiledAutoComplete(
        parser,
        parser.getInputRule(),
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
        parser.getInputRule(),
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
        parser.getInputRule(),
        TestParser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        null,
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
        ImmutableSet.copyOf(getTestPAC(query).run()),
        Matchers.equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("(", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("!", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "/",
                    true,
                    NODE_NAME_REGEX.getDescription(),
                    RANK_STRING_LITERAL,
                    query.length(),
                    NODE_NAME_REGEX.getHint()),
                new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@specifier(",
                    true,
                    REFERENCE_BOOK_AND_ADDRESS_GROUP.getDescription(),
                    RANK_STRING_LITERAL,
                    query.length(),
                    REFERENCE_BOOK_AND_ADDRESS_GROUP.getHint()))));
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
        getTestPAC(query, completionMetadata).run(),
        containsInAnyOrder(
            new AutocompleteSuggestion(
                "1.1.1.1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
            new AutocompleteSuggestion(
                "1.1.1.10", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
            new AutocompleteSuggestion(
                "-", true, IP_RANGE.getDescription(), RANK_STRING_LITERAL, 7, IP_RANGE.getHint()),
            new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, 7)));
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

  /** Test that we auto complete partial specifier names */
  @Test
  public void testRunSpecifierPartial() {
    assertThat(
        ImmutableSet.copyOf(getTestPAC("@specifie", testLibrary).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "@specifier(",
                    true,
                    REFERENCE_BOOK_AND_ADDRESS_GROUP.getDescription(),
                    RANK_STRING_LITERAL,
                    0,
                    REFERENCE_BOOK_AND_ADDRESS_GROUP.getHint()))));
  }

  /** Test that we auto complete specifier names */
  @Test
  public void testRunSpecifierFull() {
    assertThat(
        ImmutableSet.copyOf(getTestPAC("@specifier", testLibrary).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "(",
                    true,
                    REFERENCE_BOOK_AND_ADDRESS_GROUP.getDescription(),
                    RANK_STRING_LITERAL,
                    10,
                    REFERENCE_BOOK_AND_ADDRESS_GROUP.getHint()))));
  }

  /** Test that we produce auto complete snapshot-based dynamic values like address groups */
  @Test
  public void testRunSpecifierInput() {
    assertThat(
        ImmutableSet.copyOf(getTestPAC("@specifier(", testLibrary).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, 11),
                new AutocompleteSuggestion(
                    "b1a", true, null, AutocompleteSuggestion.DEFAULT_RANK, 11),
                new AutocompleteSuggestion(
                    "b2a", true, null, AutocompleteSuggestion.DEFAULT_RANK, 11))));

    // only g11 and g12 should be suggested
    assertThat(
        ImmutableSet.copyOf(getTestPAC("@specifier(b1a,", testLibrary).run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, 15),
                new AutocompleteSuggestion(
                    "g11", true, null, AutocompleteSuggestion.DEFAULT_RANK, 15),
                new AutocompleteSuggestion(
                    "g12", true, null, AutocompleteSuggestion.DEFAULT_RANK, 15))));
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
        suggestions.subList(0, 5),
        containsInAnyOrder(
            new AutocompleteSuggestion("!", true, null, RANK_STRING_LITERAL, 0),
            new AutocompleteSuggestion(
                "/",
                true,
                NODE_NAME_REGEX.getDescription(),
                RANK_STRING_LITERAL,
                0,
                NODE_NAME_REGEX.getHint()),
            new AutocompleteSuggestion("(", true, null, RANK_STRING_LITERAL, 0),
            new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, 0),
            new AutocompleteSuggestion(
                "@specifier(",
                true,
                REFERENCE_BOOK_AND_ADDRESS_GROUP.getDescription(),
                RANK_STRING_LITERAL,
                0,
                REFERENCE_BOOK_AND_ADDRESS_GROUP.getHint())));
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
        new ReportingParseRunner<>(TestParser.instance().input(TestParser.instance().TestSpec()))
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
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.STRING_LITERAL, "\"pfxcomp\"", 0, 0), "pfx", ImmutableList.of());
    assertThat(
        getTestPAC("pfx").autoCompletePotentialMatch(pm),
        equalTo(ImmutableList.of(new AutocompleteSuggestion("pfxcomp", true, null, -1, 0))));
  }

  /** The suggestion should have the case in the grammar token independent of user input */
  @Test
  public void testAutoCompletePotentialMatchStringLiteralCasePreserve() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.STRING_LITERAL, "\"pfxcomp\"", 0, 0), "PfX", ImmutableList.of());
    assertThat(
        getTestPAC("PfX").autoCompletePotentialMatch(pm),
        equalTo(ImmutableList.of(new AutocompleteSuggestion("pfxcomp", true, null, -1, 0))));
  }

  @Test
  public void testAutoCompletePotentialMatchSkipLabel() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.IP_ADDRESS_MASK, "label", 0, 0), "pfx", ImmutableList.of());
    assertThat(getTestPAC(null).autoCompletePotentialMatch(pm), equalTo(ImmutableList.of()));
  }

  /** Throw an exception if anchor is not present in the path */
  @Test
  public void testAutoCompleteInterfaceNameMissingAnchor() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.INTERFACE_NAME, null, 0, 0), "", ImmutableList.of());

    _thrown.expect(IllegalArgumentException.class);
    getTestPAC("@specifier(g1,", testCompletionMetadata).autoCompleteInterfaceName(pm, 0);
  }

  /** Context-sensitive completion of interface name after node name */
  @Test
  public void testAutoCompleteInterfaceNameNodeName() {
    String query = "n1a";

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
        ImmutableSet.copyOf(pac.autoCompleteInterfaceName(pm, 0)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("eth11", true, null, 0, query.length()),
                new AutocompleteSuggestion("eth12", true, null, 0, query.length()))));
  }

  /** Context-sensitive completion of interface name after node name regex */
  @Test
  public void testAutoCompleteInterfaceNameNodeNameRegex() {
    String query = "/n1/";

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
        ImmutableSet.copyOf(pac.autoCompleteInterfaceName(pm, 0)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("eth11", true, null, 0, query.length()),
                new AutocompleteSuggestion("eth12", true, null, 0, query.length()))));
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
        ImmutableSet.copyOf(pac.autoCompleteInterfaceName(pm, 0)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("eth11", true, null, 0, query.length()),
                new AutocompleteSuggestion("eth12", true, null, 0, query.length()),
                new AutocompleteSuggestion("eth21", true, null, 0, query.length()))));
  }

  /** Context-sensitive completion of interface name when interface name prefix is present */
  @Test
  public void testAutoCompleteInterfaceNameInterfaceNamePrefix() {
    String query = "n1a[eth12";

    // the expected stack for the query
    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new NameNodeAstNode("n1a"));
    vs.push(new StringAstNode("eth12"));
    ShadowStack ss = new ShadowStack(vs);

    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);

    ParboiledAutoComplete pac = getTestPAC(parser, query, testCompletionMetadata);

    PathElement anchor = new PathElement(Type.INTERFACE_NAME, null, 1, query.length());
    PathElement parent = new PathElement(Type.NODE_AND_INTERFACE, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "eth12", ImmutableList.of(parent, anchor));

    assertThat(
        ImmutableSet.copyOf(pac.autoCompleteInterfaceName(pm, 0)),
        equalTo(
            ImmutableSet.of(new AutocompleteSuggestion("eth12", true, null, 0, query.length()))));
  }

  /** Throw an exception if anchor is not present in the path */
  @Test
  public void testAutoCompleteReferenceBookEntityMissingAnchor() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(Type.REFERENCE_BOOK_NAME, null, 0, 0), "", ImmutableList.of());

    _thrown.expect(IllegalArgumentException.class);
    getTestPAC("@specifier(b1a,", testLibrary).autoCompleteReferenceBookEntity(pm, 0);
  }

  /** Revert to non context-sensitive completion when the parent anchor is not what we expect */
  @Test
  public void testAutoCompleteReferenceBookEntityNoContextAddressGroup() {
    String query = "@specifier(b1a,";
    ParboiledAutoComplete pac = getTestPAC(TestParser.instance(), query, testLibrary);

    PathElement anchor = new PathElement(Type.ADDRESS_GROUP_NAME, null, 0, query.length());
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(anchor));

    assertThat(
        ImmutableSet.copyOf(pac.autoCompleteReferenceBookEntity(pm, 0)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("g11", true, null, 0, query.length()),
                new AutocompleteSuggestion("g12", true, null, 0, query.length()),
                new AutocompleteSuggestion("g21", true, null, 0, query.length()))));
  }

  /** Revert to non context-sensitive completion when the parent anchor is not what we expect */
  @Test
  public void testAutoCompleteReferenceBookEntityNoContextInterfaceGroup() {
    String query = "@specifier(b1a,";
    ParboiledAutoComplete pac = getTestPAC(TestParser.instance(), query, testLibrary);

    PathElement anchor = new PathElement(Type.INTERFACE_GROUP_NAME, null, 0, query.length());
    PotentialMatch pm = new PotentialMatch(anchor, "", ImmutableList.of(anchor));

    assertThat(
        ImmutableSet.copyOf(pac.autoCompleteReferenceBookEntity(pm, 0)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("i11", true, null, 0, query.length()),
                new AutocompleteSuggestion("i12", true, null, 0, query.length()),
                new AutocompleteSuggestion("i21", true, null, 0, query.length()))));
  }

  /** Context-sensitive completion when address groups come before the reference book */
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
        ImmutableSet.copyOf(pac.autoCompleteReferenceBookEntity(pm, 0)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("g11", true, null, 0, query.length()),
                new AutocompleteSuggestion("g12", true, null, 0, query.length()))));
  }

  /** Context-sensitive completion when interface groups come before the reference book */
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
        ImmutableSet.copyOf(pac.autoCompleteReferenceBookEntity(pm, 0)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("i11", true, null, 0, query.length()),
                new AutocompleteSuggestion("i12", true, null, 0, query.length()))));
  }

  /** Group name's prefix is considered in context-sensitive autocompletion */
  @Test
  public void testAutoCompleteReferenceBookEntityPrefix() {
    // two books with the same group
    ReferenceLibrary testLibrary =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("b1a")
                    .setAddressGroups(
                        ImmutableList.of(
                            new AddressGroup(null, "g1a"), new AddressGroup(null, "g2a")))
                    .build()));

    String query = "@specifier(b1a, g1";

    // the expected stack for the query
    DefaultValueStack<AstNode> vs = new DefaultValueStack<>();
    vs.push(new StringAstNode("b1a"));
    vs.push(new StringAstNode("g1"));
    ShadowStack ss = new ShadowStack(vs);

    TestParser parser = TestParser.instance();
    parser.setShadowStack(ss);

    ParboiledAutoComplete pac = getTestPAC(parser, query, testLibrary);

    PathElement anchor = new PathElement(Type.ADDRESS_GROUP_NAME, null, 1, query.length());
    PathElement parent = new PathElement(REFERENCE_BOOK_AND_ADDRESS_GROUP, null, 0, 0);
    PotentialMatch pm = new PotentialMatch(anchor, "g1", ImmutableList.of(parent, anchor));

    assertThat(
        ImmutableSet.copyOf(pac.autoCompleteReferenceBookEntity(pm, 0)),
        equalTo(ImmutableSet.of(new AutocompleteSuggestion("g1a", true, null, 0, query.length()))));
  }
}
