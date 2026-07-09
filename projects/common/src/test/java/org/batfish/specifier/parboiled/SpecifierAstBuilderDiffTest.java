package org.batfish.specifier.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.specifier.Grammar;
import org.junit.Test;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * Differential test: for a range of inputs across all specifier grammars, asserts that the ANTLR
 * {@link SpecifierAstBuilder} produces the same {@link AstNode} as the parboiled {@link Parser}.
 * This is the oracle for the grammar migration: as long as the two agree, the ANTLR parser is a
 * faithful replacement.
 */
public class SpecifierAstBuilderDiffTest {

  private static AstNode parboiledAst(Grammar grammar, String input) {
    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(Parser.instance().getInputRule(grammar)).run(input);
    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              input, grammar, (InvalidInputError) result.parseErrors.get(0), Parser.ANCHORS));
    }
    return ParserUtils.getAst(result);
  }

  private static void assertSame(Grammar grammar, String input) {
    AstNode expected = parboiledAst(grammar, input);
    AstNode actual = SpecifierAstBuilder.getAst(grammar, input);
    assertThat("AST for [" + input + "]", actual, equalTo(expected));
  }

  private static void assertAllSame(Grammar grammar, String... inputs) {
    for (String input : inputs) {
      assertSame(grammar, input);
    }
  }

  @Test
  public void testNode() {
    assertAllSame(
        Grammar.NODE_SPECIFIER,
        "node.com-011",
        "@role(a, b)",
        " @role ( a , b ) ",
        "@deviceType(router)",
        "/^node-0.1\\/0.*.?$/",
        "node.*",
        ".*node.*",
        "(node-lhr)",
        "node0\\node1",
        "node0&node1",
        "node0,node1",
        "node0\\node1&eth1",
        "node0&node1,eth1",
        "\"quoted node\"",
        "\"node,with,commas\"");
  }

  @Test
  public void testInterface() {
    assertAllSame(
        Grammar.INTERFACE_SPECIFIER,
        "Ethernet0",
        "ifa-ce0:1/0.0",
        "@connectedTo(1.1.1.1)",
        "@interfaceGroup(a, b)",
        "@interfaceType(physical)",
        "@vrf(vrf1)",
        "@zone(zone1)",
        "node1[Ethernet0]",
        "/eth.*/",
        "eth.*",
        "eth1,eth2",
        "eth1&eth2\\eth3");
  }

  @Test
  public void testFilter() {
    assertAllSame(
        Grammar.FILTER_SPECIFIER,
        "acl1",
        "@in(Ethernet0)",
        "@out(Ethernet0)",
        "node1[acl1]",
        "acl1,acl2",
        "/acl.*/",
        "acl.*",
        "acl1&acl2\\acl3");
  }

  @Test
  public void testIpSpace() {
    assertAllSame(
        Grammar.IP_SPACE_SPECIFIER,
        "1.1.1.0/24",
        "1.1.1.1",
        "1.1.1.1:255.255.255.0",
        "1.1.1.1 - 1.1.1.2",
        "@addressgroup(a, b)",
        "1.1.1.0/24, 2.2.2.0/24",
        "1.1.1.1&2.2.2.2");
  }

  @Test
  public void testLocation() {
    assertAllSame(
        Grammar.LOCATION_SPECIFIER,
        "node1",
        "node1[Ethernet0]",
        "@enter(node1[Ethernet0])",
        "internet",
        "@vrf(vrf1)",
        "node1,node2");
  }

  @Test
  public void testRoutingPolicy() {
    assertAllSame(
        Grammar.ROUTING_POLICY_SPECIFIER, "rp1", "/rp.*/", "rp.*", "rp1,rp2", "rp1&rp2\\rp3");
  }

  @Test
  public void testIpProtocol() {
    assertAllSame(Grammar.IP_PROTOCOL_SPECIFIER, "tcp", "51", "!tcp", "tcp,udp", "!tcp,51");
  }

  @Test
  public void testApp() {
    assertAllSame(
        Grammar.APPLICATION_SPECIFIER,
        "ssh",
        "icmp",
        "icmp/8",
        "icmp/8/0",
        "tcp",
        "tcp/80",
        "tcp/80,443",
        "udp/53",
        "tcp/80-90",
        "ssh,tcp/80");
  }

  @Test
  public void testOneApp() {
    assertAllSame(Grammar.SINGLE_APPLICATION_SPECIFIER, "ssh", "icmp/8/0", "tcp/80", "udp/53");
  }

  @Test
  public void testEnumSet() {
    // Use a concrete enum-backed grammar.
    assertAllSame(Grammar.ROUTING_PROTOCOL_SPECIFIER, "bgp", "!bgp", "/gp/", "bgp,ospf");
  }
}
