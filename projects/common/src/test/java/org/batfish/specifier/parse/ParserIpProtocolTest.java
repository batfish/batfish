package org.batfish.specifier.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.IpProtocol;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parse.Anchor.Type;
import org.junit.jupiter.api.Test;

/** Tests of {@link Parser} producing {@link IpProtocolAstNode}. */
class ParserIpProtocolTest {

  @Test
  void testCompletionEmpty() {
    String query = "";

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
                Grammar.IP_PROTOCOL_SPECIFIER,
                "network",
                "snapshot",
                query,
                Integer.MAX_VALUE,
                null,
                null,
                null)
            .run();

    Set<ParsedAutoCompleteSuggestion> expected =
        Stream.concat(
                Arrays.stream(IpProtocol.values())
                    .map(Object::toString)
                    .map(val ->
                        new ParsedAutoCompleteSuggestion(
                            val, query.length(), Type.IP_PROTOCOL_NAME)),
                ImmutableSet.of(
                    new ParsedAutoCompleteSuggestion("!", query.length(), Type.IP_PROTOCOL_NOT))
                    .stream())
            .collect(ImmutableSet.toImmutableSet());

    assertThat(suggestions, equalTo(expected));
  }

  @Test
  void testCompletionPartialName() {
    String query = "TC";

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
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
            new ParsedAutoCompleteSuggestion("TCF", 0, Type.IP_PROTOCOL_NAME),
            new ParsedAutoCompleteSuggestion("TCP", 0, Type.IP_PROTOCOL_NAME)));
  }

  @Test
  void testCompletionNumber() {
    String query = "25";

    Set<ParsedAutoCompleteSuggestion> suggestions =
        new ParsedAutoComplete(
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
            new ParsedAutoCompleteSuggestion(",", query.length(), Type.IP_PROTOCOL_SET_OP)));
  }

  @Test
  void testParseIpProtocol() {
    String query = "tcp";
    IpProtocolIpProtocolAstNode expectedAst = new IpProtocolIpProtocolAstNode(query);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, " " + query + " "),
        equalTo(expectedAst));
  }

  @Test
  void testParseIpProtocolMatchSuperStrings() {
    String query = IpProtocol.ANY_0_HOP_PROTOCOL.toString();
    IpProtocolIpProtocolAstNode expectedAst = new IpProtocolIpProtocolAstNode(query);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, " " + query + " "),
        equalTo(expectedAst));
  }

  @Test
  void testParseIpProtocolBadName() {
    String query = "faux";
    assertThrows(
        IllegalArgumentException.class,
        () -> SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query));
  }

  @Test
  void testParseIpProtocolBadNumber() {
    String query = "2555";
    assertThrows(
        IllegalArgumentException.class,
        () -> SpecifierAstBuilder.getAst(Grammar.IP_PROTOCOL_SPECIFIER, query));
  }

  @Test
  void testParseNotIpProtocol() {
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
  void testParseFilterUnion() {
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
  void testAutoCompleteAllSuperstrings() {
    String query = "is";
    ParsedAutoComplete pac =
        new ParsedAutoComplete(
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
                .map(p -> new ParsedAutoCompleteSuggestion(p.toString(), 0, Type.IP_PROTOCOL_NAME))
                .collect(ImmutableSet.toImmutableSet())));
  }
}
