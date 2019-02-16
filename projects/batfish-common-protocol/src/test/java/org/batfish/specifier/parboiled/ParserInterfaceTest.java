package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.InterfaceType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link IpSpaceAstNode}. */
public class ParserInterfaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.INSTANCE.input(Parser.INSTANCE.InterfaceExpression()));
  }

  /** This tests if we have proper completion annotations on the rules */
  @Test
  public void testAnchorAnnotations() {
    ParsingResult<?> result = getRunner().run("");

    // not barfing means all potential paths have completion annotation at least for empty input
    ParserUtils.getPotentialMatches(
        (InvalidInputError) result.parseErrors.get(0), Parser.ANCHORS, false);
  }

  // TODO: Write complex completion tests that exercises a bunch of the grammar

  @Test
  public void testInterfaceConnectedTo() {
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
  public void testInterfaceInterfaceGroup() {
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
  public void testInterfaceName() {
    String ifaceName = "iface0:1/0.0";
    NameInterfaceAstNode expectedAst = new NameInterfaceAstNode(ifaceName);

    assertThat(ParserUtils.getAst(getRunner().run(ifaceName)), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" " + ifaceName + " ")), equalTo(expectedAst));
  }

  @Test
  public void testInterfaceNameRegex() {
    String regex = "^iface 0:1\\/0.*.?$";
    String regexWithSlashes = "/" + regex + "/";
    NameRegexInterfaceAstNode expectedAst = new NameRegexInterfaceAstNode(regex);

    assertThat(ParserUtils.getAst(getRunner().run(regexWithSlashes)), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" " + regexWithSlashes + " ")), equalTo(expectedAst));
  }

  @Test
  public void testInterfaceParens() {
    String ifaceName = "Ethernet1/0";
    NameInterfaceAstNode expectedAst = new NameInterfaceAstNode(ifaceName);

    assertThat(ParserUtils.getAst(getRunner().run("(" + ifaceName + ")")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ( " + ifaceName + " ) ")), equalTo(expectedAst));
  }

  @Test
  public void testInterfaceType() {
    TypeInterfaceAstNode expectedAst =
        new TypeInterfaceAstNode(new StringAstNode(InterfaceType.PHYSICAL.toString()));

    assertThat(ParserUtils.getAst(getRunner().run("@type(physical)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @type ( physical ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@TypE(PHYsical)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("type(physical)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" type ( physical ) ")), equalTo(expectedAst));
  }

  @Test
  public void testInterfaceVrf() {
    VrfInterfaceAstNode expectedAst = new VrfInterfaceAstNode(new StringAstNode("vrf-name"));

    assertThat(ParserUtils.getAst(getRunner().run("@vrf(vrf-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @vrf ( vrf-name ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@VrF(vrf-name)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("vrf(vrf-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" vrf ( vrf-name ) ")), equalTo(expectedAst));
  }

  @Test
  public void testInterfaceZone() {
    ZoneInterfaceAstNode expectedAst = new ZoneInterfaceAstNode(new StringAstNode("zone-name"));

    assertThat(ParserUtils.getAst(getRunner().run("@zone(zone-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" @zone ( zone-name ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@ZoNe(zone-name)")), equalTo(expectedAst));

    // old style
    assertThat(ParserUtils.getAst(getRunner().run("zone(zone-name)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" zone ( zone-name ) ")), equalTo(expectedAst));
  }

  @Test
  public void testInterfaceDifference() {
    DifferenceInterfaceAstNode expectedNode =
        new DifferenceInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0-loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 - loopback0 ")), equalTo(expectedNode));
  }

  @Test
  public void testInterfaceIntersection() {
    IntersectionInterfaceAstNode expectedNode =
        new IntersectionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0&loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 & loopback0 ")), equalTo(expectedNode));
  }

  @Test
  public void testInterfaceUnion() {
    UnionInterfaceAstNode expectedNode =
        new UnionInterfaceAstNode(
            new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0"));

    assertThat(ParserUtils.getAst(getRunner().run("eth0+loopback0")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run(" eth0 + loopback0 ")), equalTo(expectedNode));
  }

  /** Test if we got the precedence of set operators right. Intersection is higher priority. */
  @Test
  public void testInterfaceSetOpPrecedence() {
    assertThat(
        ParserUtils.getAst(getRunner().run("eth0-loopback0&eth1")),
        equalTo(
            new DifferenceInterfaceAstNode(
                new NameInterfaceAstNode("eth0"),
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("loopback0"), new NameInterfaceAstNode("eth1")))));
    assertThat(
        ParserUtils.getAst(getRunner().run("eth0&loopback0+eth1")),
        equalTo(
            new UnionInterfaceAstNode(
                new IntersectionInterfaceAstNode(
                    new NameInterfaceAstNode("eth0"), new NameInterfaceAstNode("loopback0")),
                new NameInterfaceAstNode("eth1"))));
  }
}
