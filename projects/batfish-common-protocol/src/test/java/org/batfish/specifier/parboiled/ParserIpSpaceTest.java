package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;

/** Tests of {@link Parser} producing {@link IpSpaceAstNode}. */
public class ParserIpSpaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.INSTANCE.input(Parser.INSTANCE.IpSpaceExpression()));
  }

  @Test
  public void testIpSpaceAddressGroup() {
    IpSpaceAstNode expectedAst = new AddressGroupAstNode("a", "b");

    assertThat(ParserUtils.getAst(getRunner().run("@addressgroup(a, b)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" @addressgroup ( a , b ) ")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@ADDRESSGROUP(a , b)")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run("@addressGroup(a , b)")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceAddressGroupRef() {
    IpSpaceAstNode expectedAst = new AddressGroupAstNode("a", "b");

    assertThat(ParserUtils.getAst(getRunner().run("ref.addressgroup(a, b)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run(" ref.addressgroup ( a , b ) ")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run("REF.ADDRESSGROUP(a , b)")), equalTo(expectedAst));
    assertThat(
        ParserUtils.getAst(getRunner().run("ref.addressGroup(a , b)")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceIpAddress() {
    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1")),
        equalTo(new IpAstNode(Ip.parse("1.1.1.1"))));
  }

  @Test
  public void testIpSpaceIpAddressFail() {
    _thrown.expectMessage("1111 is an invalid octet");
    _thrown.expect(ParserRuntimeException.class);
    getRunner().run("1.1.1.1111");
  }

  @Test
  public void testIpSpaceIpRange() {
    IpSpaceAstNode expectedAst = new IpRangeAstNode(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"));

    assertThat(ParserUtils.getAst(getRunner().run("1.1.1.1-2.2.2.2")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" 1.1.1.1 - 2.2.2.2 ")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceIpWildcard() {
    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1:2.2.2.2")),
        equalTo(new IpWildcardAstNode(new IpWildcard("1.1.1.1:2.2.2.2"))));
  }

  @Test
  public void testIpSpaceList2() {
    IpSpaceAstNode expectedNode =
        new CommaIpSpaceAstNode(
            new IpAstNode(Ip.parse("1.1.1.1")), new IpAstNode(Ip.parse("2.2.2.2")));

    assertThat(ParserUtils.getAst(getRunner().run("1.1.1.1,2.2.2.2")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run("1.1.1.1 , 2.2.2.2 ")), equalTo(expectedNode));
  }

  @Test
  public void testIpSpaceList3() {
    IpSpaceAstNode expectedNode =
        new CommaIpSpaceAstNode(
            new CommaIpSpaceAstNode(
                new IpAstNode(Ip.parse("1.1.1.1")), new IpAstNode(Ip.parse("2.2.2.2"))),
            new IpAstNode(Ip.parse("3.3.3.3")));

    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1,2.2.2.2,3.3.3.3")), equalTo(expectedNode));

    // a more complex list
    IpSpaceAstNode expectedNode2 =
        new CommaIpSpaceAstNode(
            new CommaIpSpaceAstNode(
                new IpAstNode(Ip.parse("1.1.1.1")),
                new IpRangeAstNode(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3"))),
            new IpAstNode(Ip.parse("3.3.3.3")));

    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1,2.2.2.2-2.2.2.3,3.3.3.3")),
        equalTo(expectedNode2));
  }

  @Test
  public void testIpSpacePrefix() {
    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1/1")),
        equalTo(new PrefixAstNode(Prefix.parse("1.1.1.1/1"))));
  }

  @Test
  public void testIpSpacePrefixFail() {
    _thrown.expectMessage("Invalid prefix length");
    _thrown.expect(ParserRuntimeException.class);
    getRunner().run("1.1.1.1/33");
  }
}
