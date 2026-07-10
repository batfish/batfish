package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.Anchor.Type.APP_ICMP;
import static org.batfish.specifier.parse.Anchor.Type.APP_ICMP_TYPE;
import static org.batfish.specifier.parse.Anchor.Type.APP_ICMP_TYPE_CODE;
import static org.batfish.specifier.parse.Anchor.Type.APP_NAME;
import static org.batfish.specifier.parse.Anchor.Type.APP_PORTS;
import static org.batfish.specifier.parse.Anchor.Type.APP_PORT_RANGE;
import static org.batfish.specifier.parse.Anchor.Type.APP_SET_OP;
import static org.batfish.specifier.parse.Anchor.Type.APP_TCP;
import static org.batfish.specifier.parse.Anchor.Type.APP_UDP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Parser} producing {@link AppAstNode}. */
public class ParserAppTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static ParsedAutoComplete getPAC(String query) {
    return new ParsedAutoComplete(
        Grammar.APPLICATION_SPECIFIER,
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
        .add(new ParsedAutoCompleteSuggestion("icmp", insertionIndex, APP_ICMP))
        .add(new ParsedAutoCompleteSuggestion("tcp", insertionIndex, APP_TCP))
        .add(new ParsedAutoCompleteSuggestion("udp", insertionIndex, APP_UDP))
        .build();
  }

  @Test
  public void testCompletionEmpty() {
    String query = "";
    assertThat(getPAC(query).run(), equalTo(getAllStartingSuggestions(query.length())));
  }

  @Test
  public void testCompletionAppName_partialName() {
    assertThat(
        getPAC("ht").run(),
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, APP_NAME),
            new ParsedAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  public void testCompletionAppName_partialAndFullName() {
    assertThat(
        getPAC("http").run(), // http is both a partial name and a full name
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion(",", 4, APP_SET_OP),
            new ParsedAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, APP_NAME),
            new ParsedAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  public void testCompletionAppName_fullName() {
    assertThat(
        getPAC("https").run(),
        containsInAnyOrder(
            new ParsedAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME),
            new ParsedAutoCompleteSuggestion(",", 5, APP_SET_OP)));
  }

  @Test
  public void testCompletionPartialProtocolName() {
    assertThat(
        getPAC("ud").run(),
        containsInAnyOrder(new ParsedAutoCompleteSuggestion("udp", 0, APP_UDP)));
  }

  @Test
  public void testCompletionFullProtocolName() {
    assertThat(
        getPAC("udp").run(),
        equalTo(
            ImmutableSet.of(
                new ParsedAutoCompleteSuggestion("/", 3, APP_PORTS),
                new ParsedAutoCompleteSuggestion(",", 3, APP_SET_OP))));
  }

  @Test
  public void testCompletionPortProtocolName() {
    // nothing to autocomplete since we don't have useful suggestions for port numbers
    assertThat(getPAC("udp / ").run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCompletionProtocolPort() {
    String query = "udp / 2";
    assertThat(
        getPAC(query).run(),
        equalTo(
            ImmutableSet.of(
                new ParsedAutoCompleteSuggestion(",", query.length(), APP_SET_OP),
                new ParsedAutoCompleteSuggestion("-", query.length(), APP_PORT_RANGE),
                new ParsedAutoCompleteSuggestion(",", query.length(), APP_PORTS))));
  }

  @Test
  public void testCompletionPartialProtocolPortRange() {
    String query = "udp / 2 - ";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCompletionFullProtocolPortRange() {
    String query = "udp / 2 - 3";
    assertThat(
        getPAC(query).run(),
        equalTo(
            ImmutableSet.of(
                new ParsedAutoCompleteSuggestion(",", query.length(), APP_SET_OP),
                new ParsedAutoCompleteSuggestion(",", query.length(), APP_PORTS))));
  }

  @Test
  public void testCompletionProtocolPortComma() {
    String query = "udp / 2, ";
    assertThat(getPAC(query).run(), equalTo(getAllStartingSuggestions(query.length())));
  }

  @Test
  public void testCompletionIcmp() {
    String query = "icmp";
    assertThat(
        getPAC(query).run(),
        equalTo(
            ImmutableSet.of(
                new ParsedAutoCompleteSuggestion("/", 4, APP_ICMP_TYPE),
                new ParsedAutoCompleteSuggestion(",", 4, APP_SET_OP))));
  }

  @Test
  public void testCompletionIcmpSlash() {
    String query = "icmp/";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCompletionIcmpSlashType() {
    String query = "icmp/2";
    assertThat(
        getPAC(query).run(),
        equalTo(
            ImmutableSet.of(
                new ParsedAutoCompleteSuggestion("/", query.length(), APP_ICMP_TYPE_CODE),
                new ParsedAutoCompleteSuggestion(",", query.length(), APP_SET_OP))));
  }

  @Test
  public void testCompletionIcmpSlashTypeSlash() {
    String query = "icmp/2/";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCompletionIcmpSlashTypeCode() {
    String query = "icmp / 0 / 0 ";
    assertThat(
        getPAC(query).run(),
        equalTo(
            ImmutableSet.of(new ParsedAutoCompleteSuggestion(",", query.length(), APP_SET_OP))));
  }

  /** Test that we present all initial suggestions after all valid complete ICMP inputs */
  @Test
  public void testCompletionIcmpFullSpecificationComma() {
    String query1 = "icmp,";
    assertThat(getPAC(query1).run(), equalTo(getAllStartingSuggestions(query1.length())));

    String query2 = "icmp / 0, ";
    assertThat(getPAC(query2).run(), equalTo(getAllStartingSuggestions(query2.length())));

    String query3 = "icmp / 0 / 0, ";
    assertThat(getPAC(query3).run(), equalTo(getAllStartingSuggestions(query3.length())));
  }

  @Test
  public void testParseAppRegex() {
    String regex = "/ht.*/";
    RegexAppAstNode expectedAst = new RegexAppAstNode("ht.*");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, regex), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " " + regex + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseAppName() {
    String name = "http";
    NameAppAstNode expectedAst = new NameAppAstNode("http");

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, name), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " " + name + " "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseIcmp() {
    IcmpAllAppAstNode expectedAst = new IcmpAllAppAstNode();

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "icmp"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " icmp "), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " IcMp "), equalTo(expectedAst));
  }

  @Test
  public void testParseIcmpType() {
    IcmpTypeAppAstNode expectedAst = new IcmpTypeAppAstNode(8);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "icmp/8"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " icmp / 8 "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseIcmpType_invalidType() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid ICMP type");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "icmp/257");
  }

  @Test
  public void testParseIcmpTypeCode() {
    IcmpTypeCodeAppAstNode expectedAst = new IcmpTypeCodeAppAstNode(8, 0);

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "icmp/8/0"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " icmp / 8 / 0 "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseIcmpTypeCode_invalidCode() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid ICMP type/code");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "icmp/8/1");
  }

  @Test
  public void testParseTcp() {
    TcpAppAstNode expectedAst = new TcpAppAstNode();

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " tcp "), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "TCp"), equalTo(expectedAst));
  }

  @Test
  public void testParseUdp() {
    UdpAppAstNode expectedAst = new UdpAppAstNode();

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "udp"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " udp "), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "UdP"), equalTo(expectedAst));
  }

  @Test
  public void testParsePort() {
    TcpAppAstNode expectedAst = new TcpAppAstNode(ImmutableList.of(SubRange.singleton(80)));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/80"), equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " tcp / 80 "),
        equalTo(expectedAst));
  }

  @Test
  public void testParsePort_invalid_max() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid port number");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/808080");
  }

  @Test
  public void testParsePort_invalid_min() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid port number");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/0");
  }

  @Test
  public void testParsePortRange() {
    TcpAppAstNode expectedAst = new TcpAppAstNode(ImmutableList.of(new SubRange(80, 82)));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/80-82"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " tcp / 80 - 82"),
        equalTo(expectedAst));
  }

  @Test
  public void testParsePortRange_invalid1() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid port number");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/80 - 808080");
  }

  @Test
  public void testParsePortRange_invalid2() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid port number");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/808080 - 80");
  }

  @Test
  public void testParsePortRange_invalid3() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid port range");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/82 - 80");
  }

  @Test
  public void testParsePortRange_invalid4() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Invalid port number: 0");
    SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/0 - 80");
  }

  @Test
  public void testParsePortTerms() {
    TcpAppAstNode expectedAst =
        new TcpAppAstNode(ImmutableList.of(new SubRange(80, 82), SubRange.singleton(89)));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "tcp/80-82,89"),
        equalTo(expectedAst));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " tcp / 80 - 82 , 89 "),
        equalTo(expectedAst));
  }

  @Test
  public void testParseUnion() {
    UnionAppAstNode expectedNode =
        new UnionAppAstNode(new NameAppAstNode("http"), new NameAppAstNode("https"));

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "http,https"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " http , https "),
        equalTo(expectedNode));
  }

  @Test
  public void testParseUnion2() {
    UnionAppAstNode expectedNode =
        new UnionAppAstNode(new NameAppAstNode("http"), new TcpAppAstNode());

    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, "http,tcp"),
        equalTo(expectedNode));
    assertThat(
        SpecifierAstBuilder.getAst(Grammar.APPLICATION_SPECIFIER, " http , tcp "),
        equalTo(expectedNode));
  }
}
