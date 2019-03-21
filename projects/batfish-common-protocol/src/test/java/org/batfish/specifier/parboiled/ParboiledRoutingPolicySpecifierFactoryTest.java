package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledRoutingPolicySpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildRoutingPolicySpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledRoutingPolicySpecifierFactory().buildRoutingPolicySpecifier("@connected");
  }

  @Test
  public void testBuildRoutingPolicySpecifierGoodInput() {
    assertThat(
        new ParboiledRoutingPolicySpecifierFactory().buildRoutingPolicySpecifier("router0"),
        equalTo(new ParboiledRoutingPolicySpecifier(new NameRoutingPolicyAstNode("router0"))));
  }

  @Test
  public void testBuildRoutingPolicySpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledRoutingPolicySpecifierFactory().buildRoutingPolicySpecifier(null);
  }
}
