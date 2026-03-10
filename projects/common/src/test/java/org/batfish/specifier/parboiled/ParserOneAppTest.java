package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.APP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.ONE_APP_ICMP;
import static org.batfish.specifier.parboiled.Anchor.Type.ONE_APP_ICMP_TYPE;
import static org.batfish.specifier.parboiled.Anchor.Type.ONE_APP_TCP;
import static org.batfish.specifier.parboiled.Anchor.Type.ONE_APP_UDP;
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
import org.parboiled.errors.InvalidInputError;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} for {@link Grammar#SINGLE_APPLICATION_SPECIFIER}. */
public class ParserOneAppTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(
        Parser.instance().getInputRule(Grammar.SINGLE_APPLICATION_SPECIFIER));
  }

  private static ParboiledAutoComplete getPAC(String query) {
    return new ParboiledAutoComplete(
        Parser.instance(),
        Grammar.SINGLE_APPLICATION_SPECIFIER,
        Parser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.EMPTY,
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  private static Set<ParboiledAutoCompleteSuggestion> getAllStartingSuggestions(
      int insertionIndex) {
    return ImmutableSet.<ParboiledAutoCompleteSuggestion>builder()
        .addAll(
            CommonParser.namedApplications.stream()
                .map(app -> new ParboiledAutoCompleteSuggestion(app, insertionIndex, APP_NAME))
                .collect(ImmutableSet.toImmutableSet()))
        .add(new ParboiledAutoCompleteSuggestion("icmp/", insertionIndex, ONE_APP_ICMP))
        .add(new ParboiledAutoCompleteSuggestion("tcp/", insertionIndex, ONE_APP_TCP))
        .add(new ParboiledAutoCompleteSuggestion("udp/", insertionIndex, ONE_APP_UDP))
        .build();
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
    assertThat(getPAC(query).run(), equalTo(getAllStartingSuggestions(query.length())));
  }

  @Test
  public void testCompletionAppName_partialName() {
    assertThat(
        getPAC("ht").run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, APP_NAME),
            new ParboiledAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  public void testCompletionAppName_partialAndFullName() {
    assertThat(
        getPAC("http").run(), // http is both a partial name and a full name
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(Protocol.HTTP.toString(), 0, APP_NAME),
            new ParboiledAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  public void testCompletionAppName_fullName() {
    assertThat(
        getPAC("https").run(),
        containsInAnyOrder(
            new ParboiledAutoCompleteSuggestion(Protocol.HTTPS.toString(), 0, APP_NAME)));
  }

  @Test
  public void testCompletionPartialProtocolName() {
    assertThat(
        getPAC("ud").run(),
        containsInAnyOrder(new ParboiledAutoCompleteSuggestion("udp/", 0, ONE_APP_UDP)));
  }

  @Test
  public void testCompletionFullProtocolName() {
    assertThat(
        getPAC("udp").run(),
        equalTo(ImmutableSet.of(new ParboiledAutoCompleteSuggestion("udp/", 0, ONE_APP_UDP))));
  }

  @Test
  public void testCompletionPortProtocolName() {
    // nothing to autocomplete since we don't have useful suggestions for port numbers
    assertThat(getPAC("udp/").run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCompletionProtocolPort() {
    String query = "udp/2";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCompletionIcmp() {
    String query = "icmp";
    assertThat(
        getPAC(query).run(),
        equalTo(ImmutableSet.of(new ParboiledAutoCompleteSuggestion("icmp/", 0, ONE_APP_ICMP))));
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
                new ParboiledAutoCompleteSuggestion("/", query.length(), ONE_APP_ICMP_TYPE))));
  }

  @Test
  public void testCompletionIcmpSlashTypeSlash() {
    String query = "icmp/2/";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testCompletionIcmpSlashTypeCode() {
    String query = "icmp/0/0";
    assertThat(getPAC(query).run(), equalTo(ImmutableSet.of()));
  }

  @Test
  public void testParseAppName() {
    String name = "http";
    NameAppAstNode expectedAst = new NameAppAstNode("http");

    assertThat(ParserUtils.getAst(getRunner().run(name)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + name + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseIcmpType_invalidType() {
    _thrown.expect(ParserRuntimeException.class);
    _thrown.expectMessage("Invalid ICMP type");
    ParserUtils.getAst(getRunner().run("icmp/257"));
  }

  @Test
  public void testParseIcmpTypeCode() {
    IcmpTypeCodeAppAstNode expectedAst = new IcmpTypeCodeAppAstNode(8, 0);
    assertThat(ParserUtils.getAst(getRunner().run("icmp/8/0")), equalTo(expectedAst));
  }

  @Test
  public void testParseIcmpTypeCode_invalidCode() {
    _thrown.expect(ParserRuntimeException.class);
    _thrown.expectMessage("Invalid ICMP type/code");
    ParserUtils.getAst(getRunner().run("icmp/8/1"));
  }

  @Test
  public void testParsePort() {
    TcpAppAstNode expectedAst = new TcpAppAstNode(ImmutableList.of(SubRange.singleton(80)));

    assertThat(ParserUtils.getAst(getRunner().run("tcp/80")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" tcp/80")), equalTo(expectedAst));
  }

  @Test
  public void testParsePort_invalid() {
    _thrown.expect(ParserRuntimeException.class);
    _thrown.expectMessage("Invalid port number");
    ParserUtils.getAst(getRunner().run("tcp/808080"));
  }
}
