package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.datamodel.IpProtocol;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link IpProtocolAstNode}. */
public class ParserIpProtocolTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(
        Parser.instance().getInputRule(Grammar.IP_PROTOCOL_SPECIFIER));
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

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.IP_PROTOCOL_SPECIFIER,
                Parser.ANCHORS,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                null,
                null,
                null)
            .run();

    assertThat(
        suggestions,
        equalTo(
            // '!' and all named values
            Stream.concat(
                    Arrays.stream(IpProtocol.values())
                        .map(Object::toString)
                        .filter(p -> !p.startsWith("UNNAMED")),
                    ImmutableSet.of("!").stream())
                .map(val -> new ParboiledAutoCompleteSuggestion(val, query.length(), Type.UNKNOWN))
                .collect(ImmutableSet.toImmutableSet())));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "TC";

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.IP_PROTOCOL_SPECIFIER,
                Parser.ANCHORS,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                null,
                null,
                null)
            .run();

    assertThat(
        suggestions,
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion("TCF", 0, Type.UNKNOWN),
            new ParboiledAutoCompleteSuggestion("TCP", 0, Type.UNKNOWN)));
  }

  @Test
  public void testCompletionNumber() {
    String query = "25";

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Parser.instance(),
                Grammar.IP_PROTOCOL_SPECIFIER,
                Parser.ANCHORS,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                null,
                null,
                null)
            .run();

    assertThat(
        suggestions,
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion(",", query.length(), Type.UNKNOWN)));
  }

  @Test
  public void testParseIpProtocol() {
    String query = "tcp";
    IpProtocolIpProtocolAstNode expectedAst = new IpProtocolIpProtocolAstNode(query);

    assertThat(ParserUtils.getAst(getRunner().run(query)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + query + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseIpProtocolBadName() {
    String query = "faux";
    _thrown.expect(IllegalArgumentException.class);
    ParserUtils.getAst(getRunner().run(query));
  }

  @Test
  public void testParseIpProtocolBadNumber() {
    String query = "2555";
    _thrown.expect(ParserRuntimeException.class);
    ParserUtils.getAst(getRunner().run(query));
  }

  @Test
  public void testParseNotIpProtocol() {
    IpProtocolAstNode expectedAst = new NotIpProtocolAstNode("tcp");

    assertThat(ParserUtils.getAst(getRunner().run("!tcp")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" ! tcp ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("!6")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" ! 6 ")), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterUnion() {
    UnionIpProtocolAstNode expectedNode =
        new UnionIpProtocolAstNode(
            new IpProtocolIpProtocolAstNode("tcp"), new IpProtocolIpProtocolAstNode("23"));

    assertThat(ParserUtils.getAst(getRunner().run("tcp,23")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" tcp , 23 ")), equalTo(expectedNode));
  }
}
