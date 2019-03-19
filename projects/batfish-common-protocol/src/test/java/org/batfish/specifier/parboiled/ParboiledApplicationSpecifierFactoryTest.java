package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ParboiledApplicationSpecifierFactory} */
public class ParboiledApplicationSpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildApplicationSpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledApplicationSpecifierFactory().buildApplicationSpecifier("@..");
  }

  @Test
  public void testBuildApplicationSpecifierGoodInput() {
    assertThat(
        new ParboiledApplicationSpecifierFactory().buildApplicationSpecifier("ssh"),
        equalTo(new ParboiledApplicationSpecifier(new NameApplicationAstNode("ssh"))));
  }

  @Test
  public void testBuildFilterSpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledApplicationSpecifierFactory().buildApplicationSpecifier(null);
  }
}
