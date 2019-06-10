package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Protocol;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledApplicationSpecifierTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testParseBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    ParboiledApplicationSpecifier.parse("@..");
  }

  @Test
  public void testParseGoodInput() {
    assertThat(
        ParboiledApplicationSpecifier.parse("ssh"),
        equalTo(new ParboiledApplicationSpecifier(new NameApplicationAstNode("ssh"))));
  }

  @Test
  public void testResolveName() {
    assertThat(
        new ParboiledApplicationSpecifier(new NameApplicationAstNode("ssh")).resolve(),
        equalTo(ImmutableSet.of(Protocol.SSH)));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledApplicationSpecifier(
                new UnionApplicationAstNode(
                    new NameApplicationAstNode("ssh"), new NameApplicationAstNode("telnet")))
            .resolve(),
        equalTo(ImmutableSet.of(Protocol.SSH, Protocol.TELNET)));
  }
}
