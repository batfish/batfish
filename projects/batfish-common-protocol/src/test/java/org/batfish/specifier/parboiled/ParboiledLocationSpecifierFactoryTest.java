package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledLocationSpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildLocationSpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledLocationSpecifierFactory().buildLocationSpecifier("@connected");
  }

  @Test
  public void testBuildLocationSpecifierGoodInput() {
    assertThat(
        new ParboiledLocationSpecifierFactory().buildLocationSpecifier("node0"),
        equalTo(new ParboiledLocationSpecifier(InterfaceLocationAstNode.createFromNode("node0"))));
  }

  @Test
  public void testBuildLocationSpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledLocationSpecifierFactory().buildLocationSpecifier(null);
  }
}
