package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.Anchor.Type.FILTER_INTERFACE_IN;
import static org.batfish.specifier.parse.Anchor.Type.FILTER_INTERFACE_OUT;
import static org.batfish.specifier.parse.Anchor.Type.FILTER_NAME;
import static org.batfish.specifier.parse.Anchor.Type.FILTER_NAME_REGEX;
import static org.batfish.specifier.parse.Anchor.Type.FILTER_PARENS;
import static org.batfish.specifier.parse.Anchor.Type.FILTER_SET_OP;
import static org.batfish.specifier.parse.Anchor.Type.NODE_AND_FILTER_TAIL;
import static org.batfish.specifier.parse.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parse.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parse.Anchor.Type.NODE_PARENS;
import static org.batfish.specifier.parse.Anchor.Type.NODE_ROLE_AND_DIMENSION;
import static org.batfish.specifier.parse.Anchor.Type.NODE_TYPE;
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

/** Tests of {@link Parser} producing {@link FilterAstNode}. */
public class ParserFilterTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCompletionEmpty() {
    String query = "";

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("node1"))
            .setFilterNames(ImmutableSet.of("filter1"))
            .build();

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
                Grammar.FILTER_SPECIFIER,
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
            new ParsedAutoCompleteSuggestion("filter1", query.length(), FILTER_NAME),
            new ParsedAutoCompleteSuggestion("(", query.length(), FILTER_PARENS),
            new ParsedAutoCompleteSuggestion("/", query.length(), FILTER_NAME_REGEX),
            new ParsedAutoCompleteSuggestion("@in(", query.length(), FILTER_INTERFACE_IN),
            new ParsedAutoCompleteSuggestion("@out(", query.length(), FILTER_INTERFACE_OUT),

            // node based completions
            new ParsedAutoCompleteSuggestion("(", query.length(), NODE_PARENS),
            new ParsedAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParsedAutoCompleteSuggestion("node1", query.length(), NODE_NAME),
            new ParsedAutoCompleteSuggestion("@role(", query.length(), NODE_ROLE_AND_DIMENSION),
            new ParsedAutoCompleteSuggestion("@deviceType(", query.length(), NODE_TYPE)));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "filter1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("node1"))
            .setFilterNames(ImmutableSet.of("filter1", "filter11"))
            .build();

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
                Grammar.FILTER_SPECIFIER,
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
            new ParsedAutoCompleteSuggestion("filter1", 0, FILTER_NAME),
            new ParsedAutoCompleteSuggestion("filter11", 0, FILTER_NAME),
            new ParsedAutoCompleteSuggestion("\\", query.length(), FILTER_SET_OP),
            new ParsedAutoCompleteSuggestion(",", query.length(), FILTER_SET_OP),
            new ParsedAutoCompleteSuggestion("&", query.length(), FILTER_SET_OP),
            new ParsedAutoCompleteSuggestion("[", query.length(), NODE_AND_FILTER_TAIL)));
  }

  @Test
  public void testParseFilterDirectionIn() {
    InFilterAstNode expectedAst = new InFilterAstNode(new NameInterfaceAstNode("eth0"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "@in(eth0)"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " @in ( eth0 ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "@IN(eth0)"), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterDirectionOut() {
    OutFilterAstNode expectedAst = new OutFilterAstNode(new NameInterfaceAstNode("eth0"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "@out(eth0)"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " @out ( eth0 ) "),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "@OUT(eth0)"), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterName() {
    String filterName = "filt-er";
    NameFilterAstNode expectedAst = new NameFilterAstNode(filterName);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, filterName), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " " + filterName + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseFilterNameRegex() {
    String regex = "^filter 0-0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexFilterAstNode expectedAst = new NameRegexFilterAstNode(regex);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, regexWithSlashes),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " " + regexWithSlashes + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseFilterNameRegexDeprecated() {
    String regex = "filter.*";
    NameRegexFilterAstNode expectedAst = new NameRegexFilterAstNode(regex);

    assertThat(SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, regex), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " " + regex + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseFilterParens() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "(filter)"),
        equalTo(new NameFilterAstNode("filter")));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " ( filter ) "),
        equalTo(new NameFilterAstNode("filter")));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "(filter1&filter2)"),
        equalTo(
            new IntersectionFilterAstNode(
                new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"))));
  }

  @Test
  public void testParseFilterDifference() {
    DifferenceFilterAstNode expectedNode =
        new DifferenceFilterAstNode(
            new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "filter1\\filter2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " filter1 \\ filter2 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseFilterIntersection() {
    IntersectionFilterAstNode expectedNode =
        new IntersectionFilterAstNode(
            new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "filter1&filter2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " filter1 & filter2 "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseFilterWithNodeSimple() {
    FilterAstNode expectedAst =
        new FilterWithNodeFilterAstNode(new NameNodeAstNode("n"), new NameFilterAstNode("e"));

    assertThat(SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "n[e]"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " n [ e ] "), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterWithNodeComplexNodeTerm() {
    FilterAstNode expectedAst =
        new FilterWithNodeFilterAstNode(
            new UnionNodeAstNode(new NameNodeAstNode("n1"), new NameNodeAstNode("n2")),
            new UnionFilterAstNode(new NameFilterAstNode("e1"), new NameFilterAstNode("e2")));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "(n1, n2)[e1, e2]"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "(n1, n2)[(e1, e2)]"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "(n1, (n2))[e1, (e2)]"),
        equalTo(expectedAst));
  }

  @Test
  public void testParseFilterUnion() {
    UnionFilterAstNode expectedNode =
        new UnionFilterAstNode(new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "filter1,filter2"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, " filter1 , filter2 "),
        equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseFilterSetOpPrecedence() {
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "filter1\\filter2&filter3"),
        equalTo(
            new DifferenceFilterAstNode(
                new NameFilterAstNode("filter1"),
                new IntersectionFilterAstNode(
                    new NameFilterAstNode("filter2"), new NameFilterAstNode("filter3")))));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.FILTER_SPECIFIER, "filter1&filter2,filter3"),
        equalTo(
            new UnionFilterAstNode(
                new IntersectionFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2")),
                new NameFilterAstNode("filter3"))));
  }
}
