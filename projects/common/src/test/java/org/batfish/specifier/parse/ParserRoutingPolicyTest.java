package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.Anchor.Type.ROUTING_POLICY_NAME;
import static org.batfish.specifier.parse.Anchor.Type.ROUTING_POLICY_NAME_REGEX;
import static org.batfish.specifier.parse.Anchor.Type.ROUTING_POLICY_PARENS;
import static org.batfish.specifier.parse.Anchor.Type.ROUTING_POLICY_SET_OP;
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

/** Tests of {@link Parser} producing {@link RoutingPolicyAstNode}. */
public class ParserRoutingPolicyTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCompletionEmpty() {
    String query = "";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setRoutingPolicyNames(ImmutableSet.of("RoutingPolicy1"))
            .build();

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
                Grammar.ROUTING_POLICY_SPECIFIER,
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
            new ParsedAutoCompleteSuggestion("RoutingPolicy1", query.length(), ROUTING_POLICY_NAME),
            new ParsedAutoCompleteSuggestion("(", query.length(), ROUTING_POLICY_PARENS),
            new ParsedAutoCompleteSuggestion("/", query.length(), ROUTING_POLICY_NAME_REGEX)));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "RoutingPolicy1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setRoutingPolicyNames(ImmutableSet.of("RoutingPolicy1", "RoutingPolicy11"))
            .build();

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
                Grammar.ROUTING_POLICY_SPECIFIER,
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
            new ParsedAutoCompleteSuggestion("RoutingPolicy1", 0, ROUTING_POLICY_NAME),
            new ParsedAutoCompleteSuggestion("RoutingPolicy11", 0, ROUTING_POLICY_NAME),
            new ParsedAutoCompleteSuggestion("\\", query.length(), ROUTING_POLICY_SET_OP),
            new ParsedAutoCompleteSuggestion(",", query.length(), ROUTING_POLICY_SET_OP),
            new ParsedAutoCompleteSuggestion("&", query.length(), ROUTING_POLICY_SET_OP)));
  }

  @Test
  public void testParseRoutingPolicyName() {
    String routingPolicyName = "route-er";
    NameRoutingPolicyAstNode expectedAst = new NameRoutingPolicyAstNode(routingPolicyName);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, routingPolicyName),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, " " + routingPolicyName + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseRoutingPolicyNameRegex() {
    String regex = "^RoutingPolicy 0-0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexRoutingPolicyAstNode expectedAst = new NameRegexRoutingPolicyAstNode(regex);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, regexWithSlashes),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, " " + regexWithSlashes + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseRoutingPolicyNameRegexDeprecated() {
    String regex = "RoutingPolicy.*";
    NameRegexRoutingPolicyAstNode expectedAst = new NameRegexRoutingPolicyAstNode(regex);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, regex), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, " " + regex + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseRoutingPolicyParens() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, "(RoutingPolicy)"),
        equalTo(new NameRoutingPolicyAstNode("RoutingPolicy")));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.ROUTING_POLICY_SPECIFIER, " ( RoutingPolicy ) "),
        equalTo(new NameRoutingPolicyAstNode("RoutingPolicy")));
    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, "(RoutingPolicy1&RoutingPolicy2)"),
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
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, "RoutingPolicy1\\RoutingPolicy2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, " RoutingPolicy1 \\ RoutingPolicy2 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseRoutingPolicyIntersection() {
    IntersectionRoutingPolicyAstNode expectedNode =
        new IntersectionRoutingPolicyAstNode(
            new NameRoutingPolicyAstNode("RoutingPolicy1"),
            new NameRoutingPolicyAstNode("RoutingPolicy2"));

    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, "RoutingPolicy1&RoutingPolicy2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, " RoutingPolicy1 & RoutingPolicy2 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseRoutingPolicyUnion() {
    UnionRoutingPolicyAstNode expectedNode =
        new UnionRoutingPolicyAstNode(
            new NameRoutingPolicyAstNode("RoutingPolicy1"),
            new NameRoutingPolicyAstNode("RoutingPolicy2"));

    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, "RoutingPolicy1,RoutingPolicy2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, " RoutingPolicy1 , RoutingPolicy2 "),
        equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseRoutingPolicySetOpPrecedence() {
    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, "RoutingPolicy1\\RoutingPolicy2&RoutingPolicy3"),
        equalTo(
            new DifferenceRoutingPolicyAstNode(
                new NameRoutingPolicyAstNode("RoutingPolicy1"),
                new IntersectionRoutingPolicyAstNode(
                    new NameRoutingPolicyAstNode("RoutingPolicy2"),
                    new NameRoutingPolicyAstNode("RoutingPolicy3")))));
    assertThat(
        SpecifierAstBuilder.getAst(
            Grammar.ROUTING_POLICY_SPECIFIER, "RoutingPolicy1&RoutingPolicy2,RoutingPolicy3"),
        equalTo(
            new UnionRoutingPolicyAstNode(
                new IntersectionRoutingPolicyAstNode(
                    new NameRoutingPolicyAstNode("RoutingPolicy1"),
                    new NameRoutingPolicyAstNode("RoutingPolicy2")),
                new NameRoutingPolicyAstNode("RoutingPolicy3"))));
  }
}
