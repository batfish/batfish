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

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AstNode getOnlyStackItem(ParsingResult<?> result) {
    assertTrue(result.parseErrors.isEmpty());
    assertThat(result.valueStack.size(), equalTo(1));
    return (AstNode) result.valueStack.iterator().next();
  }

  @Test
  public void testIpSpaceIpAddress() {
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1");

    assertThat(getOnlyStackItem(result), equalTo(new LeafAstNode(Ip.parse("1.1.1.1"))));
  }

  @Test
  public void testIpSpaceIpAddressFail() {
    _thrown.expectMessage("Invalid ip");
    _thrown.expect(ParserRuntimeException.class);
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1111");
  }

  @Test
  public void testIpSpaceIpRange() {
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1-2.2.2.2");

    assertThat(
        getOnlyStackItem(result),
        equalTo(
            new IpSpaceAstNode(
                Type.DASH,
                new LeafAstNode(Ip.parse("1.1.1.1")),
                new LeafAstNode(Ip.parse("2.2.2.2")))));

    // with white space
    ParsingResult<?> result2 =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1 - 2.2.2.2");

    assertThat(
        getOnlyStackItem(result2),
        equalTo(
            new IpSpaceAstNode(
                Type.DASH,
                new LeafAstNode(Ip.parse("1.1.1.1")),
                new LeafAstNode(Ip.parse("2.2.2.2")))));
  }

  @Test
  public void testIpSpaceIpWildcard() {
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1:2.2.2.2");

    assertThat(
        getOnlyStackItem(result), equalTo(new LeafAstNode(new IpWildcard("1.1.1.1:2.2.2.2"))));
  }

  @Test
  public void testIpSpaceList2() {
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1,2.2.2.2");

    IpSpaceAstNode expectedNode =
        new IpSpaceAstNode(
            Type.COMMA, new LeafAstNode(Ip.parse("1.1.1.1")), new LeafAstNode(Ip.parse("2.2.2.2")));

    assertThat(getOnlyStackItem(result), equalTo(expectedNode));

    // again, with space
    ParsingResult<?> resultSpace =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1 , 2.2.2.2");

    assertThat(getOnlyStackItem(resultSpace), equalTo(expectedNode));
  }

  @Test
  public void testIpSpaceList3() {
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression()))
            .run("1.1.1.1,2.2.2.2,3.3.3.3");

    assertThat(
        getOnlyStackItem(result),
        equalTo(
            new IpSpaceAstNode(
                Type.COMMA,
                new IpSpaceAstNode(
                    Type.COMMA,
                    new LeafAstNode(Ip.parse("1.1.1.1")),
                    new LeafAstNode(Ip.parse("2.2.2.2"))),
                new LeafAstNode(Ip.parse("3.3.3.3")))));
  }

  @Test
  public void testIpSpaceNot() {
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("!1.1.1.1");

    assertThat(
        getOnlyStackItem(result),
        equalTo(new IpSpaceAstNode(Type.NOT, new LeafAstNode(Ip.parse("1.1.1.1")), null)));

    // with space
    ParsingResult<?> resultSpace =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("! 1.1.1.1");

    assertThat(
        getOnlyStackItem(resultSpace),
        equalTo(new IpSpaceAstNode(Type.NOT, new LeafAstNode(Ip.parse("1.1.1.1")), null)));
  }

  @Test
  public void testIpSpacePrefix() {
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1/1");

    assertThat(getOnlyStackItem(result), equalTo(new LeafAstNode(Prefix.parse("1.1.1.1/1"))));
  }

  @Test
  public void testIpSpacePrefixFail() {
    _thrown.expectMessage("Invalid prefix length");
    _thrown.expect(ParserRuntimeException.class);
    ParsingResult<?> result =
        new BasicParseRunner<>(_parser.input(_parser.IpSpaceExpression())).run("1.1.1.1/33");
  }
}
