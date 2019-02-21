package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledFilterSpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildFilterSpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledFilterSpecifierFactory().buildFilterSpecifier("@connected");
  }

  @Test
  public void testBuildFilterSpecifierGoodInput() {
    assertThat(
        new ParboiledFilterSpecifierFactory().buildFilterSpecifier("filter0"),
        equalTo(new ParboiledFilterSpecifier(new NameFilterAstNode("filter0"))));
  }

  @Test
  public void testBuildFilterSpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledFilterSpecifierFactory().buildFilterSpecifier(null);
  }
}
