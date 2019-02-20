package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledInterfaceSpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildInterfaceSpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledInterfaceSpecifierFactory().buildInterfaceSpecifier("@connected");
  }

  @Test
  public void testBuildInterfaceSpecifierGoodInput() {
    assertThat(
        new ParboiledInterfaceSpecifierFactory().buildInterfaceSpecifier("eth0"),
        equalTo(new ParboiledInterfaceSpecifier(new NameInterfaceAstNode("eth0"))));
  }

  @Test
  public void testBuildInterfaceSpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledInterfaceSpecifierFactory().buildInterfaceSpecifier(null);
  }
}
