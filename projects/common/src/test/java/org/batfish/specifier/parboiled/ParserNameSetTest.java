package org.batfish.specifier.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Test;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link NameSetAstNode}. */
public class ParserNameSetTest {

  private static CompletionMetadata mockCompletionMetadata =
      CompletionMetadata.builder().setMlagIds(ImmutableSet.of("mlag1", "mlag2", "other")).build();

  private static AbstractParseRunner<AstNode> getRunner() {
    return getRunner(Grammar.MLAG_ID_SPECIFIER);
  }

  private static AbstractParseRunner<AstNode> getRunner(Grammar grammar) {
    return new ReportingParseRunner<>(Parser.instance().getInputRule(grammar));
  }

  private static ParboiledAutoComplete getPAC(String query) {
    return new ParboiledAutoComplete(
        Grammar.MLAG_ID_SPECIFIER,
        Parser.instance().getInputRule(Grammar.MLAG_ID_SPECIFIER),
        Parser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        mockCompletionMetadata,
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
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
  public void testAutoCompleteEmpty() {
    assertThat(
        getPAC("").run(),
        equalTo(
            ImmutableSet.of(
                new ParboiledAutoCompleteSuggestion("mlag1", 0, Type.NAME_SET_NAME),
                new ParboiledAutoCompleteSuggestion("mlag2", 0, Type.NAME_SET_NAME),
                new ParboiledAutoCompleteSuggestion("other", 0, Type.NAME_SET_NAME),
                new ParboiledAutoCompleteSuggestion("/", 0, Type.NAME_SET_REGEX))));
  }

  @Test
  public void testAutocompletePartialName() {
    String query = "lag";

    assertThat(
        getPAC(query).run(),
        equalTo(
            ImmutableSet.of(
                new ParboiledAutoCompleteSuggestion("mlag1", 0, Type.NAME_SET_NAME),
                new ParboiledAutoCompleteSuggestion("mlag2", 0, Type.NAME_SET_NAME),
                new ParboiledAutoCompleteSuggestion(",", query.length(), Type.NAME_SET_SET_OP))));
  }

  @Test
  public void testParseName() {
    String query = "mla";
    NameSetAstNode expectedAst = new SingletonNameSetAstNode(query);

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNameRegex() {
    String query = "/IP/";
    RegexNameSetAstNode expectedAst = new RegexNameSetAstNode("IP");

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseNameRegexDeprecated() {
    String query = "ip.*";
    RegexNameSetAstNode expectedAst = new RegexNameSetAstNode("ip.*");

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseUnion() {
    String name = "n1";
    String regex = "re1";
    UnionNameSetAstNode expectedNode =
        new UnionNameSetAstNode(new SingletonNameSetAstNode(name), new RegexNameSetAstNode(regex));

    assertThat(
        ParserUtils.getAst(getRunner().run(String.format("%s,/%s/", name, regex))),
        equalTo(expectedNode));
    assertThat(
        ParserUtils.getAst(getRunner().run(String.format(" %s , /%s/ ", name, regex))),
        equalTo(expectedNode));
  }
}
