package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
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
}
