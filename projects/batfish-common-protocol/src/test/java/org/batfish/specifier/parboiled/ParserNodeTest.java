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
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link NodeAstNode}. */
public class ParserNodeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.instance().getInputRule(Grammar.NODE_SPECIFIER));
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
            .setNodes(ImmutableMap.of("node1", new NodeCompletionMetadata(null)))
            .build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.NODE_SPECIFIER,
                Parser.ANCHORS,
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
                Parser.instance(),
                Grammar.NODE_SPECIFIER,
                Parser.ANCHORS,
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

    assertThat(ParserUtils.getAst(getRunner().run("@role(a, b)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @role ( a , b ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@RoLe(a , b)")), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeName() {
    String name = "node.com-011";
    NameNodeAstNode expectedAst = new NameNodeAstNode(name);

    assertThat(ParserUtils.getAst(getRunner().run(name)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + name + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeNameRegex() {
    String regex = "^node-0.1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexNodeAstNode expectedAst = new NameRegexNodeAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regexWithSlashes)), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" " + regexWithSlashes + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeNameRegexDeprecated() {
    String regex = "node.*";
    NameRegexNodeAstNode expectedAst = new NameRegexNodeAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regex)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + regex + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeNameRegexDeprecated2() {
    String regex = ".*node.*";
    NameRegexNodeAstNode expectedAst = new NameRegexNodeAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regex)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + regex + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeParens() {
    String ifaceName = "node-lhr";
    NameNodeAstNode expectedAst = new NameNodeAstNode(ifaceName);

    assertThat(ParserUtils.getAst(getRunner().run("(" + ifaceName + ")")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ( " + ifaceName + " ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeType() {
    TypeNodeAstNode expectedAst =
        new TypeNodeAstNode(new StringAstNode(DeviceType.ROUTER.toString()));

    assertThat(ParserUtils.getAst(getRunner().run("@deviceType(router)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @deviceType ( router ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@DeviCEtype(RouTer)")), equalTo(expectedAst));
  }

  @Test
  public void testParseNodeDifference() {
    DifferenceNodeAstNode expectedNode =
        new DifferenceNodeAstNode(new NameNodeAstNode("node0"), new NameNodeAstNode("node1"));

    assertThat(ParserUtils.getAst(getRunner().run("node0\\node1")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" node0 \\ node1 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseNodeIntersection() {
    IntersectionNodeAstNode expectedNode =
        new IntersectionNodeAstNode(new NameNodeAstNode("node0"), new NameNodeAstNode("node1"));

    assertThat(ParserUtils.getAst(getRunner().run("node0&node1")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" node0 & node1 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseNodeUnion() {
    UnionNodeAstNode expectedNode =
        new UnionNodeAstNode(new NameNodeAstNode("node0"), new NameNodeAstNode("node1"));

    assertThat(ParserUtils.getAst(getRunner().run("node0,node1")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" node0 , node1 ")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run("(node0 , node1)")), equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseNodeSetOpPrecedence() {
    assertThat(
        ParserUtils.getAst(getRunner().run("node0\\node1&eth1")),
        equalTo(
            new DifferenceNodeAstNode(
                new NameNodeAstNode("node0"),
                new IntersectionNodeAstNode(
                    new NameNodeAstNode("node1"), new NameNodeAstNode("eth1")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("node0&node1,eth1")),
        equalTo(
            new UnionNodeAstNode(
                new IntersectionNodeAstNode(
                    new NameNodeAstNode("node0"), new NameNodeAstNode("node1")),
                new NameNodeAstNode("eth1"))));
  }
}
