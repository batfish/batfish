package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledIpProtocolSpecifier.VALID_IP_PROTOCOLS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

/** Tests for {@link ParboiledIpProtocolSpecifier} */
public class ParboiledIpProtocolSpecifierTest {

  @Test
  public void testResolveIpProtocol() {
    assertThat(
        new ParboiledIpProtocolSpecifier(new IpProtocolIpProtocolAstNode("tcp")).resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP)));
  }

  @Test
  public void testResolveNotIpProtocol() {
    assertThat(
        new ParboiledIpProtocolSpecifier(new NotIpProtocolAstNode("tcp")).resolve(),
        equalTo(Sets.difference(VALID_IP_PROTOCOLS, ImmutableSet.of(IpProtocol.TCP))));
  }

  @Test
  public void testResolveUnionTwoExcludings() {
    assertThat(
        new ParboiledIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new NotIpProtocolAstNode("tcp"), new NotIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(
            Sets.difference(VALID_IP_PROTOCOLS, ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP))));
  }

  @Test
  public void testResolveUnionTwoIncludings() {
    assertThat(
        new ParboiledIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new IpProtocolIpProtocolAstNode("tcp"), new IpProtocolIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP)));
  }

  @Test
  public void testResolveUnionIncludingExcluding() {
    assertThat(
        new ParboiledIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new IpProtocolIpProtocolAstNode("tcp"), new NotIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP)));
  }
}
