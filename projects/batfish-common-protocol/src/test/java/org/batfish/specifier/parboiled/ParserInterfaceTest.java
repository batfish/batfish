package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledAutoComplete.RANK_STRING_LITERAL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link InterfaceAstNode}. */
public class ParserInterfaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.INSTANCE.input(Parser.INSTANCE.InterfaceExpression()));
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
            .setInterfaces(ImmutableSet.of(new NodeInterfacePair("node1", "iface1")))
            .build();

    ParboiledAutoComplete pac =
        new ParboiledAutoComplete(
            Parser.INSTANCE,
            Parser.INSTANCE.input(Parser.INSTANCE.InterfaceExpression()),
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
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "iface1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
                new AutocompleteSuggestion("(", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("/", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@connectedTo", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "connectedTo", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@interfaceGroup", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "ref.interfaceGroup", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@link", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("type", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("@vrf", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("vrf", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "@zone", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(
                    "zone", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  @Test
  public void testCompletionPartialName() {
    String query = "iface1"; // this could be a complete term or a partial name

    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setInterfaces(
                ImmutableSet.of(
                    new NodeInterfacePair("node1", "iface1"),
                    new NodeInterfacePair("node1", "iface11")))
            .build();

    ParboiledAutoComplete pac =
        new ParboiledAutoComplete(
            Parser.INSTANCE,
            Parser.INSTANCE.input(Parser.INSTANCE.InterfaceExpression()),
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
            ImmutableSet.of(
                new AutocompleteSuggestion(
                    "", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
                new AutocompleteSuggestion(
                    "1", true, null, AutocompleteSuggestion.DEFAULT_RANK, query.length()),
                new AutocompleteSuggestion("\\", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion(",", true, null, RANK_STRING_LITERAL, query.length()),
                new AutocompleteSuggestion("&", true, null, RANK_STRING_LITERAL, query.length()))));
  }

  @Test
  public void testParseInterfaceConnectedTo() {
    ConnectedToInterfaceAstNode expectedAst =
        new ConnectedToInterfaceAstNode(new IpAstNode("1.1.1.1"));

    assertThat(ParserUtils.getAst(getRunner().run("@connectedTo(1.1.1.1)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @connectedTo ( 1.1.1.1 ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@COnnECTEDTO(1.1.1.1)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("connectedTo(1.1.1.1)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" connectedTo ( 1.1.1.1 ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceInterfaceGroup() {
    InterfaceGroupInterfaceAstNode expectedAst = new InterfaceGroupInterfaceAstNode("a", "b");

    assertThat(ParserUtils.getAst(getRunner().run("@interfacegroup(a, b)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @interfacegroup ( a , b ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@InterfaceGrouP(a , b)")), equalTo(expectedAst));

    // old style
    assertThat(
        ParserUtils.getAst(getRunner().run("ref.interfacegroup(a, b)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ref.interfacegroup (a, b ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceName() {
    String ifaceName = "ifa-ce0:1/0.0";
    NameInterfaceAstNode expectedAst = new NameInterfaceAstNode(ifaceName);

    assertThat(ParserUtils.getAst(getRunner().run(ifaceName)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + ifaceName + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceNameRegex() {
    String regex = "^iface 0-0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexInterfaceAstNode expectedAst = new NameRegexInterfaceAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regexWithSlashes)), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" " + regexWithSlashes + " ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceParens() {
    assertThat(
        ParserUtils.getAst(getRunner().run("(e1/0)")), equalTo(new NameInterfaceAstNode("e1/0")));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ( e1/0 ) ")),
        equalTo(new NameInterfaceAstNode("e1/0")));
    assertThat(
        ParserUtils.getAst(getRunner().run("(e1/0&e1/1)")),
        equalTo(
            new IntersectionInterfaceAstNode(
                new NameInterfaceAstNode("e1/0"), new NameInterfaceAstNode("e1/1"))));
  }

  @Test
  public void testParseInterfaceType() {
    TypeInterfaceAstNode expectedAst =
        new TypeInterfaceAstNode(new StringAstNode(InterfaceType.PHYSICAL.toString()));

    assertThat(ParserUtils.getAst(getRunner().run("@link(physical)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @link ( physical ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@LinK(PHYsical)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("type(physical)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" type ( physical ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceVrf() {
    VrfInterfaceAstNode expectedAst = new VrfInterfaceAstNode(new StringAstNode("vrf-name"));

    assertThat(ParserUtils.getAst(getRunner().run("@vrf(vrf-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @vrf ( vrf-name ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@VrF(vrf-name)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("vrf(vrf-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" vrf ( vrf-name ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceZone() {
    ZoneInterfaceAstNode expectedAst = new ZoneInterfaceAstNode(new StringAstNode("zone-name"));

    assertThat(ParserUtils.getAst(getRunner().run("@zone(zone-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @zone ( zone-name ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@ZoNe(zone-name)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("zone(zone-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" zone ( zone-name ) ")), equalTo(expectedAst));
  }

  @Test
  public void testParseInterfaceDifference() {
    DifferenceInterfaceAstNode expectedNode =
        new DifferenceInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0\\loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 \\ loopback0 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseInterfaceIntersection() {
    IntersectionInterfaceAstNode expectedNode =
        new IntersectionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0&loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 & loopback0 ")), equalTo(expectedNode));
  }

  @Test
  public void testParseInterfaceUnion() {
    UnionInterfaceAstNode expectedNode =
        new UnionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0,loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 , loopback0 ")), equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testParseInterfaceSetOpPrecedence() {
    assertThat(
        ParserUtils.getAst(getRunner().run("eth0\\loopback0&eth1")),
        equalTo(
            new DifferenceInterfaceAstNode(
                new NameInterfaceAstNode("eth0"),
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("loopback0"), new NameInterfaceAstNode("eth1")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("eth0&loopback0,eth1")),
        equalTo(
            new UnionInterfaceAstNode(
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0")),
                new NameInterfaceAstNode("eth1"))));
  }
}
