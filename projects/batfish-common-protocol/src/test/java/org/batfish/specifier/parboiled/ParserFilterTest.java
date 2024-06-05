package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_INTERFACE_IN;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_INTERFACE_OUT;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_SET_OP;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_FILTER_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_PARENS;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_AND_DIMENSION;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_TYPE;
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

/** Tests of {@link Parser} producing {@link FilterAstNode}. */
public class ParserFilterTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.instance().getInputRule(Grammar.FILTER_SPECIFIER));
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
            .setFilterNames(ImmutableSet.of("filter1"))
            .build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.FILTER_SPECIFIER,
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
            new ParboiledAutoCompleteSuggestion("filter1", query.length(), FILTER_NAME),
            new ParboiledAutoCompleteSuggestion("(", query.length(), FILTER_PARENS),
            new ParboiledAutoCompleteSuggestion("/", query.length(), FILTER_NAME_REGEX),
            new ParboiledAutoCompleteSuggestion("@in(", query.length(), FILTER_INTERFACE_IN),
            new ParboiledAutoCompleteSuggestion("@out(", query.length(), FILTER_INTERFACE_OUT),

            // node based completions
            new ParboiledAutoCompleteSuggestion("(", query.length(), NODE_PARENS),
            new ParboiledAutoCompleteSuggestion("/", query.length(), NODE_NAME_REGEX),
            new ParboiledAutoCompleteSuggestion("node1", query.length(), NODE_NAME),
            new ParboiledAutoCompleteSuggestion("@role(", query.length(), NODE_ROLE_AND_DIMENSION),
            new ParboiledAutoCompleteSuggestion("@deviceType(", query.length(), NODE_TYPE)));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "filter1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("node1"))
            .setFilterNames(ImmutableSet.of("filter1", "filter11"))
            .build();

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.FILTER_SPECIFIER,
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
            new ParboiledAutoCompleteSuggestion("filter1", 0, FILTER_NAME),
            new ParboiledAutoCompleteSuggestion("filter11", 0, FILTER_NAME),
            new ParboiledAutoCompleteSuggestion("\\", query.length(), FILTER_SET_OP),
            new ParboiledAutoCompleteSuggestion(",", query.length(), FILTER_SET_OP),
            new ParboiledAutoCompleteSuggestion("&", query.length(), FILTER_SET_OP),
            new ParboiledAutoCompleteSuggestion("[", query.length(), NODE_AND_FILTER_TAIL)));
  }

  @Test
  public void testParseFilterDirectionIn() {
    InFilterAstNode expectedAst = new InFilterAstNode(new NameInterfaceAstNode("eth0"));

    assertThat(ParserUtils.getAst(getRunner().run("@in(eth0)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @in ( eth0 ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@IN(eth0)")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterDirectionOut() {
    OutFilterAstNode expectedAst = new OutFilterAstNode(new NameInterfaceAstNode("eth0"));

    assertThat(ParserUtils.getAst(getRunner().run("@out(eth0)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @out ( eth0 ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@OUT(eth0)")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterName() {
    String filterName = "filt-er";
    NameFilterAstNode expectedAst = new NameFilterAstNode(filterName);

    assertThat(ParserUtils.getAst(getRunner().run(filterName)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + filterName + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterNameRegex() {
    String regex = "^filter 0-0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexFilterAstNode expectedAst = new NameRegexFilterAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regexWithSlashes)), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" " + regexWithSlashes + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterNameRegexDeprecated() {
    String regex = "filter.*";
    NameRegexFilterAstNode expectedAst = new NameRegexFilterAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regex)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + regex + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterParens() {
    assertThat(
        ParserUtils.getAst(getRunner().run("(filter)")), equalTo(new NameFilterAstNode("filter")));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ( filter ) ")),
        equalTo(new NameFilterAstNode("filter")));
    assertThat(
        ParserUtils.getAst(getRunner().run("(filter1&filter2)")),
        equalTo(
            new IntersectionFilterAstNode(
                new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"))));
  }

  @Test
  public void testParseFilterDifference() {
    DifferenceFilterAstNode expectedNode =
        new DifferenceFilterAstNode(
            new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"));

    assertThat(ParserUtils.getAst(getRunner().run("filter1\\filter2")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" filter1 \\ filter2 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseFilterIntersection() {
    IntersectionFilterAstNode expectedNode =
        new IntersectionFilterAstNode(
            new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"));

    assertThat(ParserUtils.getAst(getRunner().run("filter1&filter2")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" filter1 & filter2 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseFilterWithNodeSimple() {
    FilterAstNode expectedAst =
        new FilterWithNodeFilterAstNode(new NameNodeAstNode("n"), new NameFilterAstNode("e"));

    assertThat(ParserUtils.getAst(getRunner().run("n[e]")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" n [ e ] ")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterWithNodeComplexNodeTerm() {
    FilterAstNode expectedAst =
        new FilterWithNodeFilterAstNode(
            new UnionNodeAstNode(new NameNodeAstNode("n1"), new NameNodeAstNode("n2")),
            new UnionFilterAstNode(new NameFilterAstNode("e1"), new NameFilterAstNode("e2")));

    assertThat(ParserUtils.getAst(getRunner().run("(n1, n2)[e1, e2]")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("(n1, n2)[(e1, e2)]")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("(n1, (n2))[e1, (e2)]")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterUnion() {
    UnionFilterAstNode expectedNode =
        new UnionFilterAstNode(new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2"));

    assertThat(ParserUtils.getAst(getRunner().run("filter1,filter2")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" filter1 , filter2 ")), equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseFilterSetOpPrecedence() {
    assertThat(
        ParserUtils.getAst(getRunner().run("filter1\\filter2&filter3")),
        equalTo(
            new DifferenceFilterAstNode(
                new NameFilterAstNode("filter1"),
                new IntersectionFilterAstNode(
                    new NameFilterAstNode("filter2"), new NameFilterAstNode("filter3")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("filter1&filter2,filter3")),
        equalTo(
            new UnionFilterAstNode(
                new IntersectionFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2")),
                new NameFilterAstNode("filter3"))));
  }
}
