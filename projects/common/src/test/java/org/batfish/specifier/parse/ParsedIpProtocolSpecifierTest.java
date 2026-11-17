package org.batfish.specifier.parse;

import static org.batfish.specifier.parse.ParsedIpProtocolSpecifier.IP_PROTOCOLS_SET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.batfish.datamodel.IpProtocol;
import org.junit.jupiter.api.Test;

/** Tests for {@link ParsedIpProtocolSpecifier} */
class ParsedIpProtocolSpecifierTest {

  @Test
  void testResolveIpProtocol() {
    assertThat(
        new ParsedIpProtocolSpecifier(new IpProtocolIpProtocolAstNode("tcp")).resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP)));
  }

  @Test
  void testResolveNotIpProtocol() {
    assertThat(
        new ParsedIpProtocolSpecifier(new NotIpProtocolAstNode("tcp")).resolve(),
        equalTo(Sets.difference(IP_PROTOCOLS_SET, ImmutableSet.of(IpProtocol.TCP))));
  }

  @Test
  void testResolveUnionTwoExcludings() {
    assertThat(
        new ParsedIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new NotIpProtocolAstNode("tcp"), new NotIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(
            Sets.difference(IP_PROTOCOLS_SET, ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP))));
  }

  @Test
  void testResolveUnionTwoIncludings() {
    assertThat(
        new ParsedIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new IpProtocolIpProtocolAstNode("tcp"), new IpProtocolIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP)));
  }

  @Test
  void testResolveUnionIncludingExcluding() {
    assertThat(
        new ParsedIpProtocolSpecifier(
                new UnionIpProtocolAstNode(
                    new IpProtocolIpProtocolAstNode("tcp"), new NotIpProtocolAstNode("udp")))
            .resolve(),
        equalTo(ImmutableSet.of(IpProtocol.TCP)));
  }

  @Test
  void testParseBadInput() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> ParsedIpProtocolSpecifier.parse("@.."));
    assertThat(exception.getMessage(), containsString("Error parsing"));
  }

  @Test
  void testParseGoodInput() {
    assertThat(
        ParsedIpProtocolSpecifier.parse("tcp"),
        equalTo(new ParsedIpProtocolSpecifier(new IpProtocolIpProtocolAstNode("tcp"))));
  }
}
