package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Protocol;
import org.junit.Test;

public class ParboiledApplicationSpecifierTest {

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
