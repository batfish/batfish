package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledIpSpaceSpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildIpSpaceSpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledIpSpaceSpecifierFactory().buildIpSpaceSpecifier("1.1.1");
  }

  @Test
  public void testBuildIpSpaceSpecifierGoodInput() {
    assertThat(
        new ParboiledIpSpaceSpecifierFactory().buildIpSpaceSpecifier("1.1.1.1"),
        equalTo(new ParboiledIpSpaceSpecifier(new IpAstNode("1.1.1.1"))));
  }

  @Test
  public void testBuildIpSpaceSpecifierParens() {
    assertThat(
        new ParboiledIpSpaceSpecifierFactory().buildIpSpaceSpecifier("(1.1.1.1,2.2.2.2)"),
        equalTo(
            new ParboiledIpSpaceSpecifier(
                new UnionIpSpaceAstNode(new IpAstNode("1.1.1.1"), new IpAstNode("2.2.2.2")))));
  }

  @Test
  public void testBuildIpSpaceSpecifierParensSpaces() {
    assertThat(
        new ParboiledIpSpaceSpecifierFactory()
            .buildIpSpaceSpecifier("   (  1.1.1.1   , 2.2.2.2   )  "),
        equalTo(
            new ParboiledIpSpaceSpecifier(
                new UnionIpSpaceAstNode(new IpAstNode("1.1.1.1"), new IpAstNode("2.2.2.2")))));
  }

  @Test
  public void testBuildIpSpaceSpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null);
  }
}
