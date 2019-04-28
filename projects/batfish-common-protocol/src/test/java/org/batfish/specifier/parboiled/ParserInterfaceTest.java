package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link InterfaceAstNode}. */
public class ParserInterfaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.instance().getInputRule(Grammar.INTERFACE_SPECIFIER));
  }

  private static List<AutocompleteSuggestion> autoCompleteHelper(
      String query, ReferenceLibrary referenceLibrary) {
    return ParboiledAutoComplete.autoComplete(
        Grammar.INTERFACE_SPECIFIER,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        null,
        NodeRolesData.builder().build(),
        referenceLibrary);
  }

  /** This testParses if we have proper completion annotations on the rules */
  @Test
  public void testAnchorAnnotations() {
    ParsingResult<?> result = getRunner().run("");

    // not barfing means all potential paths have completion annotation at least for empty input
    ParserUtils.getPotentialMatches(
        (InvalidInputError) result.parseErrors.get(0), Parser.ANCHORS, false);
  }

  @Test
  public void testCompletionEmpty() {
    String query = "";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("node1"))
            .setInterfaces(ImmutableSet.of(new NodeInterfacePair("node1", "iface1")))
            .build();

    List<AutocompleteSuggestion> suggestions =
        ParboiledAutoComplete.autoComplete(
            Grammar.INTERFACE_SPECIFIER,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            completionMetadata,
            null,
            null);

    assertThat(
        ImmutableSet.copyOf(suggestions),
        equalTo(
            ImmutableSet.of(
                // valid operators
                new AutocompleteSuggestion("(", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("/", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, query.length()),

                // node based completions
                new AutocompleteSuggestion(
                    "node1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
                new AutocompleteSuggestion(
                    "@role", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@deviceType", true, null, RANK_STRING_LITERAL, query.length()),

                // interface based completions
                new AutocompleteSuggestion(
                    "iface1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
                new AutocompleteSuggestion(
                    "@connectedTo", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@interfaceGroup", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@interfaceType", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("@vrf", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@zone", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "iface1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("node1"))
            .setInterfaces(
                ImmutableSet.of(
                    new NodeInterfacePair("node1", "iface1"),
                    new NodeInterfacePair("node1", "iface11")))
            .build();

    List<AutocompleteSuggestion> suggestions =
        ParboiledAutoComplete.autoComplete(
            Grammar.INTERFACE_SPECIFIER,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            completionMetadata,
            null,
            null);

    assertThat(
        ImmutableSet.copyOf(suggestions),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "iface1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion(
                    "iface11", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion("\\", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("&", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("[", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  @Test
  public void testParseInterfaceConnectedTo() {
    ConnectedToInterfaceAstNode expectedAst =
        new ConnectedToInterfaceAstNode(new IpAstNode("1.1.1.1"));

    assertThat(ParserUtils.getAst(getRunner().run("@connectedTo(1.1.1.1)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @connectedTo ( 1.1.1.1 ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@COnnECTEDTO(1.1.1.1)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("connectedTo(1.1.1.1)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" connectedTo ( 1.1.1.1 ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceInterfaceGroup() {
    InterfaceGroupInterfaceAstNode expectedAst = new InterfaceGroupInterfaceAstNode("a", "b");

    assertThat(ParserUtils.getAst(getRunner().run("@interfacegroup(a, b)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @interfacegroup ( a , b ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@InterfaceGrouP(a , b)")), equalTo(expectedAst));

    // old style
    assertThat(
        ParserUtils.getAst(getRunner().run("ref.interfacegroup(a, b)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ref.interfacegroup (a, b ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceName() {
    String ifaceName = "ifa-ce0:1/0.0";
    NameInterfaceAstNode expectedAst = new NameInterfaceAstNode(ifaceName);

    assertThat(ParserUtils.getAst(getRunner().run(ifaceName)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + ifaceName + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceNameRegex() {
    String regex = "^iface 0-0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexInterfaceAstNode expectedAst = new NameRegexInterfaceAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regexWithSlashes)), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" " + regexWithSlashes + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceNameRegexDeprecated() {
    String regex = "iface1/0.*";
    InterfaceAstNode expectedAst = new NameRegexInterfaceAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regex)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + regex + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceParens() {
    assertThat(
        ParserUtils.getAst(getRunner().run("(e1/0)")), equalTo(new NameInterfaceAstNode("e1/0")));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ( e1/0 ) ")),
        equalTo(new NameInterfaceAstNode("e1/0")));
    assertThat(
        ParserUtils.getAst(getRunner().run("(e1/0&e1/1)")),
        equalTo(
            new IntersectionInterfaceAstNode(
                new NameInterfaceAstNode("e1/0"), new NameInterfaceAstNode("e1/1"))));
  }

  @Test
  public void testParseInterfaceType() {
    TypeInterfaceAstNode expectedAst =
        new TypeInterfaceAstNode(new StringAstNode(InterfaceType.PHYSICAL.toString()));

    assertThat(
        ParserUtils.getAst(getRunner().run("@interfaceType(physical)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @interfaceType ( physical ) ")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run("@interFAcetype(PHYsical)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("type(physical)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" type ( physical ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceVrf() {
    VrfInterfaceAstNode expectedAst = new VrfInterfaceAstNode(new StringAstNode("vrf-name"));

    assertThat(ParserUtils.getAst(getRunner().run("@vrf(vrf-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @vrf ( vrf-name ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@VrF(vrf-name)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("vrf(vrf-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" vrf ( vrf-name ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceZone() {
    ZoneInterfaceAstNode expectedAst = new ZoneInterfaceAstNode(new StringAstNode("zone-name"));

    assertThat(ParserUtils.getAst(getRunner().run("@zone(zone-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @zone ( zone-name ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@ZoNe(zone-name)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("zone(zone-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" zone ( zone-name ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceWithNodeSimple() {
    InterfaceAstNode expectedAst =
        new InterfaceWithNodeInterfaceAstNode(
            new NameNodeAstNode("n"), new NameInterfaceAstNode("e"));

    assertThat(ParserUtils.getAst(getRunner().run("n[e]")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" n [ e ] ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceWithNodeComplexNodeTerm() {
    InterfaceAstNode expectedAst =
        new InterfaceWithNodeInterfaceAstNode(
            new UnionNodeAstNode(new NameNodeAstNode("n1"), new NameNodeAstNode("n2")),
            new UnionInterfaceAstNode(
                new NameInterfaceAstNode("e1"), new NameInterfaceAstNode("e2")));

    assertThat(ParserUtils.getAst(getRunner().run("(n1, n2)[e1, e2]")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("(n1, n2)[(e1, e2)]")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("(n1, (n2))[e1, (e2)]")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceDifference() {
    DifferenceInterfaceAstNode expectedNode =
        new DifferenceInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0\\loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 \\ loopback0 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseInterfaceIntersection() {
    IntersectionInterfaceAstNode expectedNode =
        new IntersectionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0&loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 & loopback0 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseInterfaceUnion() {
    UnionInterfaceAstNode expectedNode =
        new UnionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0,loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 , loopback0 ")), equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseInterfaceSetOpPrecedence() {
    assertThat(
        ParserUtils.getAst(getRunner().run("eth0\\loopback0&eth1")),
        equalTo(
            new DifferenceInterfaceAstNode(
                new NameInterfaceAstNode("eth0"),
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("loopback0"), new NameInterfaceAstNode("eth1")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("eth0&loopback0,eth1")),
        equalTo(
            new UnionInterfaceAstNode(
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0")),
                new NameInterfaceAstNode("eth1"))));
  }

  /**
   * Test that address group rule is written in a way that allows for context sensitive
   * autocompletion
   */
  @Test
  public void testContextSensitiveInterfaceGroup() {
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("b1")
                    .setInterfaceGroups(
                        ImmutableList.of(new InterfaceGroup(ImmutableSortedSet.of(), "g1")))
                    .build(),
                ReferenceBook.builder("b2")
                    .setInterfaceGroups(
                        ImmutableList.of(new InterfaceGroup(ImmutableSortedSet.of(), "g2")))
                    .build()));

    String query = "@interfaceGroup(g1,";

    // only b1 should be suggested
    assertThat(
        ImmutableSet.copyOf(autoCompleteHelper(query, library)),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "b1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()))));
  }
}
