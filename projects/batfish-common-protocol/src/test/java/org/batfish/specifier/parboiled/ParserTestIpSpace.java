package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.specifier.parboiled.IpSpaceAstNode.Type;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

public class ParserTestIpSpace {

  private static Parser _parser = Parboiled.createParser(Parser.class);

  private static BasicParseRunner<?> _runner =
      new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression()));

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AstNode getOnlyStackItem(ParsingResult<?> result) {
    assertTrue(result.parseErrors.isEmpty());
    assertThat(result.valueStack.size(), equalTo(1));
    return (AstNode) result.valueStack.iterator().next();
  }

  @Test
  public void testIpSpaceAddressGroup() {
    IpSpaceAstNode expectedAst =
        new IpSpaceAstNode(Type.ADDRESS_GROUP, new LeafAstNode("a"), new LeafAstNode("b"));

    assertThat(getOnlyStackItem(_runner.run("addressgroup(a, b)")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("addressgroup ( a , b ) ")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("ADDRESSGROUP(a , b)")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("addressGroup(a , b)")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceAddressGroupRef() {
    IpSpaceAstNode expectedAst =
        new IpSpaceAstNode(Type.ADDRESS_GROUP, new LeafAstNode("a"), new LeafAstNode("b"));

    assertThat(getOnlyStackItem(_runner.run("ref.addressgroup(a, b)")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("ref.addressgroup ( a , b ) ")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("REF.ADDRESSGROUP(a , b)")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("ref.addressGroup(a , b)")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceIpAddress() {
    assertThat(
        getOnlyStackItem(_runner.run("1.1.1.1")), equalTo(new LeafAstNode(Ip.parse("1.1.1.1"))));
  }

  @Test
  public void testIpSpaceIpAddressFail() {
    _thrown.expectMessage("Invalid ip");
    _thrown.expect(ParserRuntimeException.class);
    _runner.run("1.1.1.1111");
  }

  @Test
  public void testIpSpaceIpRange() {
    IpSpaceAstNode expectedAst =
        new IpSpaceAstNode(
            Type.DASH, new LeafAstNode(Ip.parse("1.1.1.1")), new LeafAstNode(Ip.parse("2.2.2.2")));

    assertThat(getOnlyStackItem(_runner.run("1.1.1.1-2.2.2.2")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("1.1.1.1 - 2.2.2.2 ")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceIpWildcard() {
    assertThat(
        getOnlyStackItem(_runner.run("1.1.1.1:2.2.2.2")),
        equalTo(new LeafAstNode(new IpWildcard("1.1.1.1:2.2.2.2"))));
  }

  @Test
  public void testIpSpaceList2() {
    IpSpaceAstNode expectedNode =
        new IpSpaceAstNode(
            Type.COMMA, new LeafAstNode(Ip.parse("1.1.1.1")), new LeafAstNode(Ip.parse("2.2.2.2")));

    assertThat(getOnlyStackItem(_runner.run("1.1.1.1,2.2.2.2")), equalTo(expectedNode));
    assertThat(getOnlyStackItem(_runner.run("1.1.1.1 , 2.2.2.2 ")), equalTo(expectedNode));
  }

  @Test
  public void testIpSpaceList3() {
    IpSpaceAstNode expectedNode =
        new IpSpaceAstNode(
            Type.COMMA,
            new IpSpaceAstNode(
                Type.COMMA,
                new LeafAstNode(Ip.parse("1.1.1.1")),
                new LeafAstNode(Ip.parse("2.2.2.2"))),
            new LeafAstNode(Ip.parse("3.3.3.3")));

    assertThat(getOnlyStackItem(_runner.run("1.1.1.1,2.2.2.2,3.3.3.3")), equalTo(expectedNode));

    // a more complex list
    IpSpaceAstNode expectedNode2 =
        new IpSpaceAstNode(
            Type.COMMA,
            new IpSpaceAstNode(
                Type.COMMA,
                new LeafAstNode(Ip.parse("1.1.1.1")),
                new IpSpaceAstNode(Type.NOT, new LeafAstNode(Ip.parse("2.2.2.2")), null)),
            new LeafAstNode(Ip.parse("3.3.3.3")));

    assertThat(getOnlyStackItem(_runner.run("1.1.1.1,!2.2.2.2,3.3.3.3")), equalTo(expectedNode2));
  }

  @Test
  public void testIpSpaceNot() {
    IpSpaceAstNode expectedAst =
        new IpSpaceAstNode(Type.NOT, new LeafAstNode(Ip.parse("1.1.1.1")), null);

    assertThat(getOnlyStackItem(_runner.run("!1.1.1.1")), equalTo(expectedAst));
    assertThat(getOnlyStackItem(_runner.run("! 1.1.1.1 ")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpacePrefix() {
    assertThat(
        getOnlyStackItem(_runner.run("1.1.1.1/1")),
        equalTo(new LeafAstNode(Prefix.parse("1.1.1.1/1"))));
  }

  @Test
  public void testIpSpacePrefixFail() {
    _thrown.expectMessage("Invalid prefix length");
    _thrown.expect(ParserRuntimeException.class);
    _runner.run("1.1.1.1/33");
  }
}
