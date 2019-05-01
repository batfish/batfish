package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_INTERFACE_IN;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_INTERFACE_OUT;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_PARENS;
import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
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
        CompletionMetadata.builder().setFilterNames(ImmutableSet.of("filter1")).build();

    List<AutocompleteSuggestion> suggestions =
        ParboiledAutoComplete.autoComplete(
            Grammar.FILTER_SPECIFIER,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            completionMetadata,
            null,
            null);

    assertThat(
        suggestions,
        containsInAnyOrder(
            new AutocompleteSuggestion(
                "filter1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
            new AutocompleteSuggestion(
                "(",
                true,
                FILTER_PARENS.getDescription(),
                RANK_STRING_LITERAL,
                query.length(),
                FILTER_PARENS.getHint()),
            new AutocompleteSuggestion(
                "/",
                true,
                FILTER_NAME_REGEX.getDescription(),
                RANK_STRING_LITERAL,
                query.length(),
                FILTER_NAME_REGEX.getHint()),
            new AutocompleteSuggestion("\"", true, null, RANK_STRING_LITERAL, query.length()),
            new AutocompleteSuggestion(
                "@in(",
                true,
                FILTER_INTERFACE_IN.getDescription(),
                RANK_STRING_LITERAL,
                query.length(),
                FILTER_INTERFACE_OUT.getHint()),
            new AutocompleteSuggestion(
                "@out(",
                true,
                FILTER_INTERFACE_OUT.getDescription(),
                RANK_STRING_LITERAL,
                query.length(),
                FILTER_INTERFACE_OUT.getHint())));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "filter1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setFilterNames(ImmutableSet.of("filter1", "filter11")).build();

    List<AutocompleteSuggestion> suggestions =
        ParboiledAutoComplete.autoComplete(
            Grammar.FILTER_SPECIFIER,
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
                    "filter1", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion(
                    "filter11", true, null, AutocompleteSuggestion.DEFAULT_RANK, 0),
                new AutocompleteSuggestion("\\", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("&", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  @Test
  public void testParseFilterDirectionIn() {
    InFilterAstNode expectedAst = new InFilterAstNode(new NameInterfaceAstNode("eth0"));

    assertThat(ParserUtils.getAst(getRunner().run("@in(eth0)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @in ( eth0 ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@IN(eth0)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("inFilterOf(eth0)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" InFilterOF ( eth0 ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterDirectionOut() {
    OutFilterAstNode expectedAst = new OutFilterAstNode(new NameInterfaceAstNode("eth0"));

    assertThat(ParserUtils.getAst(getRunner().run("@out(eth0)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @out ( eth0 ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@OUT(eth0)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("outFilterOf(eth0)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" OUTFilterOf ( eth0 ) ")), equalTo(expectedAst));
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
