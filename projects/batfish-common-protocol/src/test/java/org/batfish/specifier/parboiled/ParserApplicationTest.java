package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link ApplicationAstNode}. */
public class ParserApplicationTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(
        Parser.instance().getInputRule(Grammar.APPLICATION_SPECIFIER));
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

    ParboiledAutoComplete pac =
        new ParboiledAutoComplete(
            Parser.instance().getInputRule(Grammar.APPLICATION_SPECIFIER),
            Parser.ANCHORS,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            completionMetadata,
            null,
            null);

    assertThat(
        ImmutableSet.copyOf(pac.run()),
        equalTo(
            Arrays.stream(Protocol.values())
                .map(
                    val ->
                        new AutocompleteSuggestion(
                            val.toString(), true, null, RANK_STRING_LITERAL, query.length()))
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "SS";

    ParboiledAutoComplete pac =
        new ParboiledAutoComplete(
            Parser.instance().getInputRule(Grammar.APPLICATION_SPECIFIER),
            Parser.ANCHORS,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            null,
            null,
            null);

    assertThat(
        ImmutableSet.copyOf(pac.run()),
        equalTo(
            ImmutableSet.of(
                new AutocompleteSuggestion("SSH", true, null, RANK_STRING_LITERAL, 0))));
  }

  @Test
  public void testParseApplicationName() {
    String query = "ssh";
    NameApplicationAstNode expectedAst = new NameApplicationAstNode(query);

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseApplicationBad() {
    String query = "faux";
    _thrown.expect(IllegalArgumentException.class);
    ParserUtils.getAst(getRunner().run(query));
  }

  @Test
  public void testParseFilterUnion() {
    UnionApplicationAstNode expectedNode =
        new UnionApplicationAstNode(
            new NameApplicationAstNode("ssh"), new NameApplicationAstNode("telnet"));

    assertThat(ParserUtils.getAst(getRunner().run("ssh,telnet")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" ssh , telnet ")), equalTo(expectedNode));
  }
}
