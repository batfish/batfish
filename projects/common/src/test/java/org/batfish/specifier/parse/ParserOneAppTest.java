package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.Anchor.Type.APP_NAME;
import static org.batfish.specifier.parse.Anchor.Type.ONE_APP_ICMP;
import static org.batfish.specifier.parse.Anchor.Type.ONE_APP_ICMP_TYPE;
import static org.batfish.specifier.parse.Anchor.Type.ONE_APP_TCP;
import static org.batfish.specifier.parse.Anchor.Type.ONE_APP_UDP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.junit.jupiter.api.Test;

/** Tests of {@link Parser} for {@link Grammar#SINGLE_APPLICATION_SPECIFIER}. */
class ParserOneAppTest {

  private static ParsedAutoComplete getPAC(String query) {
    return new ParsedAutoComplete(
        Grammar.SINGLE_APPLICATION_SPECIFIER,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.EMPTY,
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  private static Set<ParsedAutoCompleteSuggestion> getAllStartingSuggestions(int insertionIndex) {
    return ImmutableSet.<ParsedAutoCompleteSuggestion>builder()
        .addAll(
            CommonParser.namedApplications.stream()
                .map(app -> new ParsedAutoCompleteSuggestion(app, insertionIndex, APP_NAME))
                .collect(ImmutableSet.toImmutableSet()))
        .add(new ParsedAutoCompleteSuggestion("icmp/", insertionIndex, ONE_APP_ICMP))
        .add(new ParsedAutoCompleteSuggestion("tcp/", insertionIndex, ONE_APP_TCP))
        .add(new ParsedAutoCompleteSuggestion("udp/", insertionIndex, ONE_APP_UDP))
        .build();
  }

  @Test
  void testCompletionEmpty() {
    String query = "";
    assertThat(getPAC(query).run(), equalTo(getAllStartingSuggestions(query.length())));
  }

  @Test
  void testCompletionAppName_partialName() {
    assertThat(
        getPAC("ht").run(),
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, APP_NAME),
            new ParsedAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  void testCompletionAppName_partialAndFullName() {
    assertThat(
        getPAC("http").run(), // http is both a partial name and a full name
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, APP_NAME),
            new ParsedAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  void testCompletionAppName_fullName() {
    assertThat(
        getPAC("https").run(),
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  void testCompletionPartialProtocolName() {
    assertThat(
        getPAC("ud").run(),
        containsInAnyOrder(new ParsedAutoCompleteSuggestion("udp/", 0, ONE_APP_UDP)));
  }

  @Test
  void testCompletionFullProtocolName() {
    assertThat(
        getPAC("udp").run(),
        equalTo(ImmutableSet.of(new ParsedAutoCompleteSuggestion("udp/", 0, ONE_APP_UDP))));
  }

  @Test
  void testCompletionPortProtocolName() {
    // nothing to autocomplete since we don't have useful suggestions for port numbers
    assertThat(getPAC("udp/").run(), equalTo(ImmutableSet.of()));
  }

  @Test
  void testCompletionProtocolPort() {
    String query = "udp/2";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  void testCompletionIcmp() {
    String query = "icmp";
    assertThat(
        getPAC(query).run(),
        equalTo(ImmutableSet.of(new ParsedAutoCompleteSuggestion("icmp/", 0, ONE_APP_ICMP))));
  }

  @Test
  void testCompletionIcmpSlash() {
    String query = "icmp/";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  void testCompletionIcmpSlashType() {
    String query = "icmp/2";
    assertThat(
        getPAC(query).run(),
        equalTo(
            ImmutableSet.of(
                new ParsedAutoCompleteSuggestion("/", query.length(), ONE_APP_ICMP_TYPE))));
  }

  @Test
  void testCompletionIcmpSlashTypeSlash() {
    String query = "icmp/2/";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  void testCompletionIcmpSlashTypeCode() {
    String query = "icmp/0/0";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  void testParseAppName() {
    String name = "http";
    NameAppAstNode expectedAst = new NameAppAstNode("http");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, name),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, " " + name + " "),
        equalTo(expectedAst));
  }

  @Test
  void testParseIcmpType_invalidType() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, "icmp/257"));
    assertThat(exception.getMessage(), containsString("Invalid ICMP type"));
  }

  @Test
  void testParseIcmpTypeCode() {
    IcmpTypeCodeAppAstNode expectedAst = new IcmpTypeCodeAppAstNode(8, 0);
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, "icmp/8/0"),
        equalTo(expectedAst));
  }

  @Test
  void testParseIcmpTypeCode_invalidCode() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, "icmp/8/1"));
    assertThat(exception.getMessage(), containsString("Invalid ICMP type/code"));
  }

  @Test
  void testParsePort() {
    TcpAppAstNode expectedAst = new TcpAppAstNode(ImmutableList.of(SubRange.singleton(80)));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, "tcp/80"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, " tcp/80"),
        equalTo(expectedAst));
  }

  @Test
  void testParsePort_invalid() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> SpecifierAstBuilder.getAst(Grammar.SINGLE_APPLICATION_SPECIFIER, "tcp/808080"));
    assertThat(exception.getMessage(), containsString("Invalid port number"));
  }
}
