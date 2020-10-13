package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledIpProtocolSpecifier.IP_PROTOCOLS_SET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.batfish.datamodel.IpProtocol;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        equalTo(Sets.difference(IP_PROTOCOLS_SET, ImmutableSet.of(IpProtocol.TCP))));
  }

  @Test
  public void testResolveUnionTwoExcludings() {
    assertThat(
        new ParboiledIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new NotIpProtocolAstNode("tcp"), new NotIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(
            Sets.difference(IP_PROTOCOLS_SET, ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP))));
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

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testParseBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    ParboiledIpProtocolSpecifier.parse("@..");
  }

  @Test
  public void testParseGoodInput() {
    assertThat(
        ParboiledIpProtocolSpecifier.parse("tcp"),
        equalTo(new ParboiledIpProtocolSpecifier(new IpProtocolIpProtocolAstNode("tcp"))));
  }
}
