package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ParboiledIpProtocolSpecifierFactory} */
public class ParboiledIpProtocolSpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildIpProtocolSpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledIpProtocolSpecifierFactory().buildIpProtocolSpecifier("@..");
  }

  @Test
  public void testBuildIpProtocolSpecifierGoodInput() {
    assertThat(
        new ParboiledIpProtocolSpecifierFactory().buildIpProtocolSpecifier("tcp"),
        equalTo(new ParboiledIpProtocolSpecifier(new IpProtocolIpProtocolAstNode("tcp"))));
  }

  @Test
  public void testBuildFilterSpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledIpProtocolSpecifierFactory().buildIpProtocolSpecifier(null);
  }
}
