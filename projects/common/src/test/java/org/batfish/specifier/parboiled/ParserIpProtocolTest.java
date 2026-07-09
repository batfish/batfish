package org.batfish.specifier.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.IpProtocol;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Parser} producing {@link IpProtocolAstNode}. */
public class ParserIpProtocolTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCompletionEmpty() {
    String query = "";

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Grammar.IP_PROTOCOL_SPECIFIER,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                null,
                null,
                null)
            .run();

    Set<ParboiledAutoCompleteSuggestion> expected =
        Stream.concat(
                Arrays.stream(IpProtocol.values())
                    .map(Object::toString)
                    .map(
                        val ->
                            new ParboiledAutoCompleteSuggestion(
                                val, query.length(), Type.IP_PROTOCOL_NAME)),
                ImmutableSet.of(
                    new ParboiledAutoCompleteSuggestion("!", query.length(), Type.IP_PROTOCOL_NOT))
                    .stream())
            .collect(ImmutableSet.toImmutableSet());

    assertThat(suggestions, equalTo(expected));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "TC";

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Grammar.IP_PROTOCOL_SPECIFIER,
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
            new ParboiledAutoCompleteSuggestion("TCF", 0, Type.IP_PROTOCOL_NAME),
            new ParboiledAutoCompleteSuggestion("TCP", 0, Type.IP_PROTOCOL_NAME)));
  }

  @Test
  public void testCompletionNumber() {
    String query = "25";

    Set<ParboiledAutoCompleteSuggestion> suggestions =
        new ParboiledAutoComplete(
                Grammar.IP_PROTOCOL_SPECIFIER,
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
            new ParboiledAutoCompleteSuggestion(",", query.length(), Type.IP_PROTOCOL_SET_OP)));
  }

  @Test
  public void testParseIpProtocol() {
    String query = "tcp";
    IpProtocolIpProtocolAstNode expectedAst = new IpProtocolIpProtocolAstNode(query);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, " " + query + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseIpProtocolMatchSuperStrings() {
    String query = IpProtocol.ANY_0_HOP_PROTOCOL.toString();
    IpProtocolIpProtocolAstNode expectedAst = new IpProtocolIpProtocolAstNode(query);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, " " + query + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseIpProtocolBadName() {
    String query = "faux";
    _thrown.expect(IllegalArgumentException.class);
    SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query);
  }

  @Test
  public void testParseIpProtocolBadNumber() {
    String query = "2555";
    _thrown.expect(IllegalArgumentException.class);
    SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query);
  }

  @Test
  public void testParseNotIpProtocol() {
    IpProtocolAstNode expectedAst = new NotIpProtocolAstNode("tcp");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, "!tcp"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, " ! tcp "), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, "!6"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, " ! 6 "), equalTo(expectedAst));
  }

  @Test
  public void testParseFilterUnion() {
    UnionIpProtocolAstNode expectedNode =
        new UnionIpProtocolAstNode(
            new IpProtocolIpProtocolAstNode("tcp"), new IpProtocolIpProtocolAstNode("23"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, "tcp,23"), equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, " tcp , 23 "),
        equalTo(expectedNode));
  }

  /** When the query is 'qq', we should match ALL superstrings, including xqq and qqx */
  @Test
  public void testAutoCompleteAllSuperstrings() {
    String query = "is";
    ParboiledAutoComplete pac =
        new ParboiledAutoComplete(
            Grammar.IP_PROTOCOL_SPECIFIER,
            "network",
            "snapshot",
            query,
            Integer.MAX_VALUE,
            CompletionMetadata.EMPTY,
            NodeRolesData.builder().build(),
            new ReferenceLibrary(null));
    assertThat(
        pac.run(),
        equalTo(
            Arrays.stream(IpProtocol.values())
                .filter(p -> p.toString().toLowerCase().contains(query))
                .map(
                    p ->
                        new ParboiledAutoCompleteSuggestion(p.toString(), 0, Type.IP_PROTOCOL_NAME))
                .collect(ImmutableSet.toImmutableSet())));
  }
}
