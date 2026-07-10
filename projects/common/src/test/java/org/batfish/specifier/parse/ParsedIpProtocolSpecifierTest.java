package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.ParsedIpProtocolSpecifier.IP_PROTOCOLS_SET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.batfish.datamodel.IpProtocol;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ParsedIpProtocolSpecifier} */
public class ParsedIpProtocolSpecifierTest {

  @Test
  public void testResolveIpProtocol() {
    assertThat(
        new ParsedIpProtocolSpecifier(new IpProtocolIpProtocolAstNode("tcp")).resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP)));
  }

  @Test
  public void testResolveNotIpProtocol() {
    assertThat(
        new ParsedIpProtocolSpecifier(new NotIpProtocolAstNode("tcp")).resolve(),
        equalTo(Sets.difference(IP_PROTOCOLS_SET, ImmutableSet.of(IpProtocol.TCP))));
  }

  @Test
  public void testResolveUnionTwoExcludings() {
    assertThat(
        new ParsedIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new NotIpProtocolAstNode("tcp"), new NotIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(
            Sets.difference(IP_PROTOCOLS_SET, ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP))));
  }

  @Test
  public void testResolveUnionTwoIncludings() {
    assertThat(
        new ParsedIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new IpProtocolIpProtocolAstNode("tcp"), new IpProtocolIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP)));
  }

  @Test
  public void testResolveUnionIncludingExcluding() {
    assertThat(
        new ParsedIpProtocolSpecifier(
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
    ParsedIpProtocolSpecifier.parse("@..");
  }

  @Test
  public void testParseGoodInput() {
    assertThat(
        ParsedIpProtocolSpecifier.parse("tcp"),
        equalTo(new ParsedIpProtocolSpecifier(new IpProtocolIpProtocolAstNode("tcp"))));
  }
}
