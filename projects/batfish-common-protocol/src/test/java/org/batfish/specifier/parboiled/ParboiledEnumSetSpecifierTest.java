package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.junit.Test;

/** Tests for {@link ParboiledEnumSetSpecifier} */
public class ParboiledEnumSetSpecifierTest {

  private static final Collection<String> ALL_NAMED_STRUCTURE_TYPES =
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
}
