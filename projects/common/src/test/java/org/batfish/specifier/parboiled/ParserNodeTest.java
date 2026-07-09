package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_AND_DIMENSION;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_SET_OP;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.datamodel.DeviceType;
import org.batfish.specifier.Grammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Parser} producing {@link NodeAstNode}. */
public class ParserNodeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCompletionEmpty() {
    String query = "";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableMap.of("node1", new NodeCompletionMetadata(null)))
            .build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Grammar.NODE_SPECIFIER,
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
            new ParboiledAutoCompleteSuggestion("node1", query.length(), NODE_NAME),
            new ParboiledAutoCompleteSuggestion("(", query.length(), NODE_PARENS),
            new ParboiledAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParboiledAutoCompleteSuggestion("@role(", query.length(), NODE_ROLE_AND_DIMENSION),
            new ParboiledAutoCompleteSuggestion("@deviceType(", query.length(), NODE_TYPE)));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "node1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("node1", "node11")).build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Grammar.NODE_SPECIFIER,
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
        equalTo(
            ImmutableSet.of(
                new ParboiledAutoCompleteSuggestion("node1", 0, NODE_NAME),
                new ParboiledAutoCompleteSuggestion("node11", 0, NODE_NAME),
                new ParboiledAutoCompleteSuggestion("\\", query.length(), NODE_SET_OP),
                new ParboiledAutoCompleteSuggestion(",", query.length(), NODE_SET_OP),
                new ParboiledAutoCompleteSuggestion("&", query.length(), NODE_SET_OP))));
  }

  @Test
  public void testParseNodeRole() {
    RoleNodeAstNode expectedAst = new RoleNodeAstNode("a", "b");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "@role(a, b)"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " @role ( a , b ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "@RoLe(a , b)"), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeName() {
    String name = "node.com-011";
    NameNodeAstNode expectedAst = new NameNodeAstNode(name);

    assertThat(SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, name), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " " + name + " "), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeNameRegex() {
    String regex = "^node-0.1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexNodeAstNode expectedAst = new NameRegexNodeAstNode(regex);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, regexWithSlashes), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " " + regexWithSlashes + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseNodeNameRegexDeprecated() {
    String regex = "node.*";
    NameRegexNodeAstNode expectedAst = new NameRegexNodeAstNode(regex);

    assertThat(SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, regex), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " " + regex + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseNodeNameRegexDeprecated2() {
    String regex = ".*node.*";
    NameRegexNodeAstNode expectedAst = new NameRegexNodeAstNode(regex);

    assertThat(SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, regex), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " " + regex + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseNodeParens() {
    String ifaceName = "node-lhr";
    NameNodeAstNode expectedAst = new NameNodeAstNode(ifaceName);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "(" + ifaceName + ")"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " ( " + ifaceName + " ) "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseNodeType() {
    TypeNodeAstNode expectedAst =
        new TypeNodeAstNode(new StringAstNode(DeviceType.ROUTER.toString()));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "@deviceType(router)"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " @deviceType ( router ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "@DeviCEtype(RouTer)"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseNodeDifference() {
    DifferenceNodeAstNode expectedNode =
        new DifferenceNodeAstNode(new NameNodeAstNode("node0"), new NameNodeAstNode("node1"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "node0\\node1"), equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " node0 \\ node1 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseNodeIntersection() {
    IntersectionNodeAstNode expectedNode =
        new IntersectionNodeAstNode(new NameNodeAstNode("node0"), new NameNodeAstNode("node1"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "node0&node1"), equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " node0 & node1 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseNodeUnion() {
    UnionNodeAstNode expectedNode =
        new UnionNodeAstNode(new NameNodeAstNode("node0"), new NameNodeAstNode("node1"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "node0,node1"), equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, " node0 , node1 "),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "(node0 , node1)"),
        equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseNodeSetOpPrecedence() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "node0\\node1&eth1"),
        equalTo(
            new DifferenceNodeAstNode(
                new NameNodeAstNode("node0"),
                new IntersectionNodeAstNode(
                    new NameNodeAstNode("node1"), new NameNodeAstNode("eth1")))));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.NODE_SPECIFIER, "node0&node1,eth1"),
        equalTo(
            new UnionNodeAstNode(
                new IntersectionNodeAstNode(
                    new NameNodeAstNode("node0"), new NameNodeAstNode("node1")),
                new NameNodeAstNode("eth1"))));
  }
}
