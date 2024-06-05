package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ROUTING_POLICY_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.ROUTING_POLICY_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.ROUTING_POLICY_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.ROUTING_POLICY_SET_OP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.specifier.Grammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link RoutingPolicyAstNode}. */
public class ParserRoutingPolicyTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(
        Parser.instance().getInputRule(Grammar.ROUTING_POLICY_SPECIFIER));
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
            .setRoutingPolicyNames(ImmutableSet.of("RoutingPolicy1"))
            .build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.ROUTING_POLICY_SPECIFIER,
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
            new ParboiledAutoCompleteSuggestion(
                "RoutingPolicy1", query.length(), ROUTING_POLICY_NAME),
            new ParboiledAutoCompleteSuggestion("(", query.length(), ROUTING_POLICY_PARENS),
            new ParboiledAutoCompleteSuggestion("/", query.length(), ROUTING_POLICY_NAME_REGEX)));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "RoutingPolicy1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setRoutingPolicyNames(ImmutableSet.of("RoutingPolicy1", "RoutingPolicy11"))
            .build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.ROUTING_POLICY_SPECIFIER,
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
            new ParboiledAutoCompleteSuggestion("RoutingPolicy1", 0, ROUTING_POLICY_NAME),
            new ParboiledAutoCompleteSuggestion("RoutingPolicy11", 0, ROUTING_POLICY_NAME),
            new ParboiledAutoCompleteSuggestion("\\", query.length(), ROUTING_POLICY_SET_OP),
            new ParboiledAutoCompleteSuggestion(",", query.length(), ROUTING_POLICY_SET_OP),
            new ParboiledAutoCompleteSuggestion("&", query.length(), ROUTING_POLICY_SET_OP)));
  }

  @Test
  public void testParseRoutingPolicyName() {
    String routingPolicyName = "route-er";
    NameRoutingPolicyAstNode expectedAst = new NameRoutingPolicyAstNode(routingPolicyName);

    assertThat(ParserUtils.getAst(getRunner().run(routingPolicyName)), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" " + routingPolicyName + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseRoutingPolicyNameRegex() {
    String regex = "^RoutingPolicy 0-0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexRoutingPolicyAstNode expectedAst = new NameRegexRoutingPolicyAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regexWithSlashes)), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" " + regexWithSlashes + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseRoutingPolicyNameRegexDeprecated() {
    String regex = "RoutingPolicy.*";
    NameRegexRoutingPolicyAstNode expectedAst = new NameRegexRoutingPolicyAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regex)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + regex + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseRoutingPolicyParens() {
    assertThat(
        ParserUtils.getAst(getRunner().run("(RoutingPolicy)")),
        equalTo(new NameRoutingPolicyAstNode("RoutingPolicy")));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ( RoutingPolicy ) ")),
        equalTo(new NameRoutingPolicyAstNode("RoutingPolicy")));
    assertThat(
        ParserUtils.getAst(getRunner().run("(RoutingPolicy1&RoutingPolicy2)")),
        equalTo(
            new IntersectionRoutingPolicyAstNode(
                new NameRoutingPolicyAstNode("RoutingPolicy1"),
                new NameRoutingPolicyAstNode("RoutingPolicy2"))));
  }

  @Test
  public void testParseRoutingPolicyDifference() {
    DifferenceRoutingPolicyAstNode expectedNode =
        new DifferenceRoutingPolicyAstNode(
            new NameRoutingPolicyAstNode("RoutingPolicy1"),
            new NameRoutingPolicyAstNode("RoutingPolicy2"));

    assertThat(
        ParserUtils.getAst(getRunner().run("RoutingPolicy1\\RoutingPolicy2")),
        equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(" RoutingPolicy1 \\ RoutingPolicy2 ")),
        equalTo(expectedNode));
  }

  @Test
  public void testParseRoutingPolicyIntersection() {
    IntersectionRoutingPolicyAstNode expectedNode =
        new IntersectionRoutingPolicyAstNode(
            new NameRoutingPolicyAstNode("RoutingPolicy1"),
            new NameRoutingPolicyAstNode("RoutingPolicy2"));

    assertThat(
        ParserUtils.getAst(getRunner().run("RoutingPolicy1&RoutingPolicy2")),
        equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(" RoutingPolicy1 & RoutingPolicy2 ")),
        equalTo(expectedNode));
  }

  @Test
  public void testParseRoutingPolicyUnion() {
    UnionRoutingPolicyAstNode expectedNode =
        new UnionRoutingPolicyAstNode(
            new NameRoutingPolicyAstNode("RoutingPolicy1"),
            new NameRoutingPolicyAstNode("RoutingPolicy2"));

    assertThat(
        ParserUtils.getAst(getRunner().run("RoutingPolicy1,RoutingPolicy2")),
        equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(" RoutingPolicy1 , RoutingPolicy2 ")),
        equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseRoutingPolicySetOpPrecedence() {
    assertThat(
        ParserUtils.getAst(getRunner().run("RoutingPolicy1\\RoutingPolicy2&RoutingPolicy3")),
        equalTo(
            new DifferenceRoutingPolicyAstNode(
                new NameRoutingPolicyAstNode("RoutingPolicy1"),
                new IntersectionRoutingPolicyAstNode(
                    new NameRoutingPolicyAstNode("RoutingPolicy2"),
                    new NameRoutingPolicyAstNode("RoutingPolicy3")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("RoutingPolicy1&RoutingPolicy2,RoutingPolicy3")),
        equalTo(
            new UnionRoutingPolicyAstNode(
                new IntersectionRoutingPolicyAstNode(
                    new NameRoutingPolicyAstNode("RoutingPolicy1"),
                    new NameRoutingPolicyAstNode("RoutingPolicy2")),
                new NameRoutingPolicyAstNode("RoutingPolicy3"))));
  }
}
