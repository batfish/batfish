package org.batfish.specifier.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.junit.Test;

/** Tests for {@link ParboiledEnumSetSpecifier} */
public class ParboiledEnumSetSpecifierTest {

  private static final Set<String> ALL_NAMED_STRUCTURE_TYPES =
      NamedStructurePropertySpecifier.JAVA_MAP.keySet();

  @Test
  public void testResolveType() {
    assertThat(
        new ParboiledEnumSetSpecifier<>(
                new ValueEnumSetAstNode<>(
                    NamedStructurePropertySpecifier.ROUTING_POLICY, ALL_NAMED_STRUCTURE_TYPES),
                ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(ImmutableSet.of(NamedStructurePropertySpecifier.ROUTING_POLICY)));
  }

  @Test
  public void testResolveTypeRegex() {
    assertThat(
        new ParboiledEnumSetSpecifier<>(
                new RegexEnumSetAstNode("ip.*list"), ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(
            ImmutableSet.of(
                NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                NamedStructurePropertySpecifier.IP_6_ACCESS_LIST)));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledEnumSetSpecifier<String>(
                new UnionEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        NamedStructurePropertySpecifier.IP_ACCESS_LIST, ALL_NAMED_STRUCTURE_TYPES),
                    new ValueEnumSetAstNode<>(
                        NamedStructurePropertySpecifier.IP_6_ACCESS_LIST,
                        ALL_NAMED_STRUCTURE_TYPES)),
                ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(
            ImmutableSet.of(
                NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                NamedStructurePropertySpecifier.IP_6_ACCESS_LIST)));
  }

  /** Test for resolving a simple not value like '! IP_Access_List' */
  @Test
  public void testResolveNotSimple() {
    assertThat(
        new ParboiledEnumSetSpecifier<>(
                new NotEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        NamedStructurePropertySpecifier.IP_ACCESS_LIST, ALL_NAMED_STRUCTURE_TYPES)),
                ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(
            Sets.difference(
                ALL_NAMED_STRUCTURE_TYPES,
                ImmutableSet.of(NamedStructurePropertySpecifier.IP_ACCESS_LIST))));
  }

  /** Test for resolving a regex not value like '! /ip.*list/' */
  @Test
  public void testResolveNotRegex() {
    assertThat(
        new ParboiledEnumSetSpecifier<>(
                new NotEnumSetAstNode(new RegexEnumSetAstNode("ip.*list")),
                ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(
            Sets.difference(
                ALL_NAMED_STRUCTURE_TYPES,
                ImmutableSet.of(
                    NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                    NamedStructurePropertySpecifier.IP_6_ACCESS_LIST))));
  }

  /** Test for resolving an effective subtraction as in '/ip.*list/, !IP_Access_List' */
  @Test
  public void testResolveNotSubtraction() {
    assertThat(
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new RegexEnumSetAstNode("ip.*list"),
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                            ALL_NAMED_STRUCTURE_TYPES))),
                ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(ImmutableSet.of(NamedStructurePropertySpecifier.IP_6_ACCESS_LIST)));
  }

  /**
   * Test for resolving an effective subtraction in reverse order as in '!IP_Access_List,
   * /ip.*list/'
   */
  @Test
  public void testResolveNotSubtractionReverse() {
    assertThat(
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                            ALL_NAMED_STRUCTURE_TYPES)),
                    new RegexEnumSetAstNode("ip.*list")),
                ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(ImmutableSet.of(NamedStructurePropertySpecifier.IP_6_ACCESS_LIST)));
  }

  /** Test for resolving a union of nots as in '!IP_6_Access_List, !IP_Access_List' */
  @Test
  public void testResolveNotUnion() {
    assertThat(
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            NamedStructurePropertySpecifier.IP_6_ACCESS_LIST,
                            ALL_NAMED_STRUCTURE_TYPES)),
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                            ALL_NAMED_STRUCTURE_TYPES))),
                ALL_NAMED_STRUCTURE_TYPES)
            .resolve(),
        equalTo(
            Sets.difference(
                ALL_NAMED_STRUCTURE_TYPES,
                ImmutableSet.of(
                    NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                    NamedStructurePropertySpecifier.IP_6_ACCESS_LIST))));
  }

  @Test
  public void testResolveGroupsSimpleNegation() {
    Set<String> expectedSet =
        Sets.difference(
            RoutingProtocolSpecifier.getAtomicProtocols(),
            ImmutableSet.builder()
                .add(RoutingProtocolSpecifier.EBGP)
                .add(RoutingProtocolSpecifier.IBGP)
                .build());

    assertThat(
        // ! bgp
        new ParboiledEnumSetSpecifier<>(
                new NotEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        RoutingProtocolSpecifier.BGP,
                        Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER))),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(expectedSet));

    assertThat(
        // ! /bgp/
        new ParboiledEnumSetSpecifier<>(
                new NotEnumSetAstNode(new RegexEnumSetAstNode("bgp")),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(expectedSet));
  }

  @Test
  public void testResolveGroupsNegateAll() {
    assertThat(
        // ! all
        new ParboiledEnumSetSpecifier<>(
                new NotEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        RoutingProtocolSpecifier.ALL,
                        Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER))),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveGroupsNegateSubset() {
    assertThat(
        // bgp, !ebgp
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        RoutingProtocolSpecifier.BGP,
                        Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)),
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            RoutingProtocolSpecifier.EBGP,
                            Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)))),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(ImmutableSet.of(RoutingProtocolSpecifier.IBGP)));
  }

  @Test
  public void testResolveGroupsNegateSuperset() {
    assertThat(
        // ebgp, !bgp
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        RoutingProtocolSpecifier.EBGP,
                        Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)),
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            RoutingProtocolSpecifier.BGP,
                            Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)))),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(ImmutableSet.of(RoutingProtocolSpecifier.EBGP)));
  }

  @Test
  public void testResolveGroupsNegateSame() {
    assertThat(
        // bgp, !bgp
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        RoutingProtocolSpecifier.BGP,
                        Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)),
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            RoutingProtocolSpecifier.BGP,
                            Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)))),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveGroupsNegateUnion() {
    assertThat(
        // bgp, !ibgp, !ebgp
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        RoutingProtocolSpecifier.BGP,
                        Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)),
                    new UnionEnumSetAstNode(
                        new NotEnumSetAstNode(
                            new ValueEnumSetAstNode<>(
                                RoutingProtocolSpecifier.IBGP,
                                Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER))),
                        new NotEnumSetAstNode(
                            new ValueEnumSetAstNode<>(
                                RoutingProtocolSpecifier.EBGP,
                                Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER))))),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveGroupsNegateTwoLevels() {
    assertThat(
        // ospf, !ospf-ext1
        new ParboiledEnumSetSpecifier<>(
                new UnionEnumSetAstNode(
                    new ValueEnumSetAstNode<>(
                        RoutingProtocolSpecifier.OSPF,
                        Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)),
                    new NotEnumSetAstNode(
                        new ValueEnumSetAstNode<>(
                            RoutingProtocolSpecifier.OSPF_EXT1,
                            Grammar.getEnumValues(Grammar.ROUTING_PROTOCOL_SPECIFIER)))),
                Grammar.ROUTING_PROTOCOL_SPECIFIER)
            .resolve(),
        equalTo(
            ImmutableSet.of(
                RoutingProtocolSpecifier.OSPF_INTER,
                RoutingProtocolSpecifier.OSPF_INTRA,
                RoutingProtocolSpecifier.OSPF_EXT2)));
  }
}
