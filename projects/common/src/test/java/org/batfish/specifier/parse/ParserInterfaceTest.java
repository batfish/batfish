package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_CONNECTED_TO;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_GROUP_NAME;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_NAME;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_NAME_REGEX;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_PARENS;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_SET_OP;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_TYPE;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_VRF;
import static org.batfish.specifier.parse.Anchor.Type.INTERFACE_ZONE;
import static org.batfish.specifier.parse.Anchor.Type.NODE_AND_INTERFACE_TAIL;
import static org.batfish.specifier.parse.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parse.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parse.Anchor.Type.NODE_PARENS;
import static org.batfish.specifier.parse.Anchor.Type.NODE_ROLE_AND_DIMENSION;
import static org.batfish.specifier.parse.Anchor.Type.NODE_TYPE;
import static org.batfish.specifier.parse.Anchor.Type.OPERATOR_END;
import static org.batfish.specifier.parse.Anchor.Type.REFERENCE_BOOK_AND_INTERFACE_GROUP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Parser} producing {@link InterfaceAstNode}. */
public class ParserInterfaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static Set<ParsedAutoCompleteSuggestion> autoCompleteHelper(
      String query, CompletionMetadata completionMetadata) {
    return autoCompleteHelper(query, completionMetadata, new ReferenceLibrary(null));
  }

  private static Set<ParsedAutoCompleteSuggestion> autoCompleteHelper(
      String query, ReferenceLibrary referenceLibrary) {
    return autoCompleteHelper(query, null, referenceLibrary);
  }

  private static Set<ParsedAutoCompleteSuggestion> autoCompleteHelper(
      String query, CompletionMetadata completionMetadata, ReferenceLibrary referenceLibrary) {
    return new ParsedAutoComplete(
            Grammar.INTERFACE_SPECIFIER,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            completionMetadata,
            NodeRolesData.builder().build(),
            referenceLibrary)
        .run();
  }

  @Test
  public void testCompletionEmpty() {
    String query = "";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("node1"))
            .setInterfaces(ImmutableSet.of(NodeInterfacePair.of("node1", "iface1")))
            .build();

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
                Grammar.INTERFACE_SPECIFIER,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                completionMetadata,
                null,
                null)
            .run();

    assertThat(
        suggestions,
        containsInAnyOrder(
            // valid operators
            new ParsedAutoCompleteSuggestion("(", query.length(), INTERFACE_PARENS),
            new ParsedAutoCompleteSuggestion("/", query.length(), INTERFACE_NAME_REGEX),

            // node based completions
            new ParsedAutoCompleteSuggestion("(", query.length(), NODE_PARENS),
            new ParsedAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParsedAutoCompleteSuggestion("node1", query.length(), NODE_NAME),
            new ParsedAutoCompleteSuggestion("@role(", query.length(), NODE_ROLE_AND_DIMENSION),
            new ParsedAutoCompleteSuggestion("@deviceType(", query.length(), NODE_TYPE),
            // interface based completions
            new ParsedAutoCompleteSuggestion("iface1", query.length(), INTERFACE_NAME),
            new ParsedAutoCompleteSuggestion(
                "@connectedTo(", query.length(), INTERFACE_CONNECTED_TO),
            new ParsedAutoCompleteSuggestion(
                "@interfaceGroup(", query.length(), REFERENCE_BOOK_AND_INTERFACE_GROUP),
            new ParsedAutoCompleteSuggestion("@interfaceType(", query.length(), INTERFACE_TYPE),
            new ParsedAutoCompleteSuggestion("@vrf(", query.length(), INTERFACE_VRF),
            new ParsedAutoCompleteSuggestion("@zone(", query.length(), INTERFACE_ZONE)));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "iface1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("node1"))
            .setInterfaces(
                ImmutableSet.of(
                    NodeInterfacePair.of("node1", "iface1"),
                    NodeInterfacePair.of("node1", "iface11")))
            .build();

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
                Grammar.INTERFACE_SPECIFIER,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                completionMetadata,
                null,
                null)
            .run();

    assertThat(
        ImmutableSet.copyOf(suggestions),
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion("iface1", 0, INTERFACE_NAME),
            new ParsedAutoCompleteSuggestion("iface11", 0, INTERFACE_NAME),
            new ParsedAutoCompleteSuggestion("\\", query.length(), INTERFACE_SET_OP),
            new ParsedAutoCompleteSuggestion(",", query.length(), INTERFACE_SET_OP),
            new ParsedAutoCompleteSuggestion("&", query.length(), INTERFACE_SET_OP),
            new ParsedAutoCompleteSuggestion("[", query.length(), NODE_AND_INTERFACE_TAIL)));
  }

  @Test
  public void testParseInterfaceConnectedTo() {
    ConnectedToInterfaceAstNode expectedAst =
        new ConnectedToInterfaceAstNode(new IpAstNode("1.1.1.1"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@connectedTo(1.1.1.1)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " @connectedTo ( 1.1.1.1 ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@COnnECTEDTO(1.1.1.1)"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceInterfaceGroup() {
    InterfaceGroupInterfaceAstNode expectedAst = new InterfaceGroupInterfaceAstNode("a", "b");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@interfacegroup(a, b)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " @interfacegroup ( a , b ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@InterfaceGrouP(a , b)"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceName() {
    String ifaceName = "ifa-ce0:1/0.0";
    NameInterfaceAstNode expectedAst = new NameInterfaceAstNode(ifaceName);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, ifaceName), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " " + ifaceName + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceNameRegex() {
    String regex = "^iface 0-0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexInterfaceAstNode expectedAst = new NameRegexInterfaceAstNode(regex);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, regexWithSlashes),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " " + regexWithSlashes + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceNameRegexDeprecated() {
    String regex = "iface1/0.*";
    InterfaceAstNode expectedAst = new NameRegexInterfaceAstNode(regex);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, regex), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " " + regex + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceParens() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "(e1/0)"),
        equalTo(new NameInterfaceAstNode("e1/0")));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " ( e1/0 ) "),
        equalTo(new NameInterfaceAstNode("e1/0")));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "(e1/0&e1/1)"),
        equalTo(
            new IntersectionInterfaceAstNode(
                new NameInterfaceAstNode("e1/0"), new NameInterfaceAstNode("e1/1"))));
  }

  @Test
  public void testParseInterfaceType() {
    TypeInterfaceAstNode expectedAst =
        new TypeInterfaceAstNode(new StringAstNode(InterfaceType.PHYSICAL.toString()));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@interfaceType(physical)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " @interfaceType ( physical ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@interFAcetype(PHYsical)"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceVrf() {
    VrfInterfaceAstNode expectedAst = new VrfInterfaceAstNode(new StringAstNode("vrf-name"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@vrf(vrf-name)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " @vrf ( vrf-name ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@VrF(vrf-name)"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceZone() {
    ZoneInterfaceAstNode expectedAst = new ZoneInterfaceAstNode(new StringAstNode("zone-name"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@zone(zone-name)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " @zone ( zone-name ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "@ZoNe(zone-name)"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceWithNodeSimple() {
    InterfaceAstNode expectedAst =
        new InterfaceWithNodeInterfaceAstNode(
            new NameNodeAstNode("n"), new NameInterfaceAstNode("e"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "n[e]"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " n [ e ] "), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceWithNodeComplexNodeTerm() {
    InterfaceAstNode expectedAst =
        new InterfaceWithNodeInterfaceAstNode(
            new UnionNodeAstNode(new NameNodeAstNode("n1"), new NameNodeAstNode("n2")),
            new UnionInterfaceAstNode(
                new NameInterfaceAstNode("e1"), new NameInterfaceAstNode("e2")));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "(n1, n2)[e1, e2]"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "(n1, n2)[(e1, e2)]"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "(n1, (n2))[e1, (e2)]"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceDifference() {
    DifferenceInterfaceAstNode expectedNode =
        new DifferenceInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "eth0\\loopback0"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " eth0 \\ loopback0 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseInterfaceIntersection() {
    IntersectionInterfaceAstNode expectedNode =
        new IntersectionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "eth0&loopback0"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " eth0 & loopback0 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseInterfaceUnion() {
    UnionInterfaceAstNode expectedNode =
        new UnionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "eth0,loopback0"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, " eth0 , loopback0 "),
        equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseInterfaceSetOpPrecedence() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "eth0\\loopback0&eth1"),
        equalTo(
            new DifferenceInterfaceAstNode(
                new NameInterfaceAstNode("eth0"),
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("loopback0"), new NameInterfaceAstNode("eth1")))));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.INTERFACE_SPECIFIER, "eth0&loopback0,eth1"),
        equalTo(
            new UnionInterfaceAstNode(
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0")),
                new NameInterfaceAstNode("eth1"))));
  }

  /**
   * Test that interface group rule is written in a way that allows for context sensitive
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

    String query = "@interfaceGroup(b1,";

    // only b1 should be suggested
    assertThat(
        ImmutableSet.copyOf(autoCompleteHelper(query, library)),
        equalTo(
            ImmutableSet.of(
                new ParsedAutoCompleteSuggestion("g1", query.length(), INTERFACE_GROUP_NAME))));
  }

  /**
   * Test that interface rules are written in a way that allows for context sensitive autocompletion
   */
  @Test
  public void testContextSensitiveInterfaceName() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setInterfaces(
                ImmutableSet.of(
                    NodeInterfacePair.of("n1a", "eth11"),
                    NodeInterfacePair.of("n1a", "eth12"),
                    NodeInterfacePair.of("n2a", "eth21")))
            .build();

    String query = "n1a[eth";
    Set<ParsedAutoCompleteSuggestion> suggestions = autoCompleteHelper(query, completionMetadata);

    // only eth11 and eth12 should be suggested
    assertThat(
        suggestions,
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion(",", query.length(), INTERFACE_SET_OP),
            new ParsedAutoCompleteSuggestion("\\", query.length(), INTERFACE_SET_OP),
            new ParsedAutoCompleteSuggestion("&", query.length(), INTERFACE_SET_OP),
            new ParsedAutoCompleteSuggestion("]", query.length(), OPERATOR_END),
            new ParsedAutoCompleteSuggestion("eth11", 4, INTERFACE_NAME),
            new ParsedAutoCompleteSuggestion("eth12", 4, INTERFACE_NAME)));
  }
}
