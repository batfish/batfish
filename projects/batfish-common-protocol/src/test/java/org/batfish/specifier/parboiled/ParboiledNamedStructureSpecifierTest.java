package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.junit.Test;

/** Tests for {@link ParboiledNamedStructureSpecifier} */
public class ParboiledNamedStructureSpecifierTest {

  @Test
  public void testResolveType() {
    assertThat(
        new ParboiledNamedStructureSpecifier(
                new TypeNamedStructureAstNode(NamedStructurePropertySpecifier.ROUTING_POLICY))
            .resolve(),
        equalTo(ImmutableSet.of(NamedStructurePropertySpecifier.ROUTING_POLICY)));
  }

  @Test
  public void testResolveTypeRegex() {
    assertThat(
        new ParboiledNamedStructureSpecifier(new TypeRegexNamedStructureAstNode("ip.*list"))
            .resolve(),
        equalTo(
            ImmutableSet.of(
                NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                NamedStructurePropertySpecifier.IP_6_ACCESS_LIST)));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledNamedStructureSpecifier(
                new UnionNamedStructureAstNode(
                    new TypeNamedStructureAstNode(NamedStructurePropertySpecifier.IP_ACCESS_LIST),
                    new TypeNamedStructureAstNode(
                        NamedStructurePropertySpecifier.IP_6_ACCESS_LIST)))
            .resolve(),
        equalTo(
            ImmutableSet.of(
                NamedStructurePropertySpecifier.IP_ACCESS_LIST,
                NamedStructurePropertySpecifier.IP_6_ACCESS_LIST)));
  }
}
