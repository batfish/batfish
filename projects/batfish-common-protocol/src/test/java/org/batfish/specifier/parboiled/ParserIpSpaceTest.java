package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** Tests of {@link Parser} producing {@link IpSpaceAstNode}. */
public class ParserIpSpaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static AbstractParseRunner<AstNode> getRunner() {
    return new ReportingParseRunner<>(Parser.INSTANCE.input(Parser.INSTANCE.IpSpaceExpression()));
  }

  /** This tests if we have proper completion annotations on the rules */
  @Test
  public void testCompletionAnnotations() {
    ParsingResult<?> result = getRunner().run("");

    // not barfing = at least for empty input, all potential paths have completion annotation
    ParserUtils.getPotentialMatches(
        (InvalidInputError) result.parseErrors.get(0), Parser.COMPLETION_TYPES, false);
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
    assertThat(ParserUtils.getAst(getRunner().run("1.1.1.1")), equalTo(new IpAstNode("1.1.1.1")));
  }

  @Test
  public void testIpSpaceIpAddressFail() {
    _thrown.expectMessage("1111 is an invalid octet");
    _thrown.expect(ParserRuntimeException.class);
    getRunner().run("1.1.1.1111");
  }

  @Test
  public void testIpSpaceIpRange() {
    IpSpaceAstNode expectedAst = new IpRangeAstNode("1.1.1.1", "2.2.2.2");

    assertThat(ParserUtils.getAst(getRunner().run("1.1.1.1-2.2.2.2")), equalTo(expectedAst));
    assertThat(ParserUtils.getAst(getRunner().run(" 1.1.1.1 - 2.2.2.2 ")), equalTo(expectedAst));
  }

  @Test
  public void testIpSpaceIpWildcard() {
    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1:2.2.2.2")),
        equalTo(new IpWildcardAstNode("1.1.1.1:2.2.2.2")));
  }

  @Test
  public void testIpSpaceList2() {
    IpSpaceAstNode expectedNode =
        new CommaIpSpaceAstNode(new IpAstNode("1.1.1.1"), new IpAstNode("2.2.2.2"));

    assertThat(ParserUtils.getAst(getRunner().run("1.1.1.1,2.2.2.2")), equalTo(expectedNode));
    assertThat(ParserUtils.getAst(getRunner().run("1.1.1.1 , 2.2.2.2 ")), equalTo(expectedNode));
  }

  @Test
  public void testIpSpaceList3() {
    IpSpaceAstNode expectedNode =
        new CommaIpSpaceAstNode(
            new CommaIpSpaceAstNode(new IpAstNode("1.1.1.1"), new IpAstNode("2.2.2.2")),
            new IpAstNode("3.3.3.3"));

    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1,2.2.2.2,3.3.3.3")), equalTo(expectedNode));

    // a more complex list
    IpSpaceAstNode expectedNode2 =
        new CommaIpSpaceAstNode(
            new CommaIpSpaceAstNode(
                new IpAstNode("1.1.1.1"), new IpRangeAstNode("2.2.2.2", "2.2.2.3")),
            new IpAstNode("3.3.3.3"));

    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1,2.2.2.2-2.2.2.3,3.3.3.3")),
        equalTo(expectedNode2));
  }

  @Test
  public void testIpSpacePrefix() {
    assertThat(
        ParserUtils.getAst(getRunner().run("1.1.1.1/1")), equalTo(new PrefixAstNode("1.1.1.1/1")));
  }

  @Test
  public void testIpSpacePrefixFail() {
    _thrown.expectMessage("Invalid prefix length");
    _thrown.expect(ParserRuntimeException.class);
    getRunner().run("1.1.1.1/33");
  }
}
