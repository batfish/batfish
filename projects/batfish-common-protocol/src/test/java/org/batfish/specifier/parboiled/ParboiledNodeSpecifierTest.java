package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceType;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Before;
import org.junit.Test;

public class ParboiledNodeSpecifierTest {

  private MockSpecifierContext.Builder _ctxtB;
  private MockSpecifierContext _ctxt;

  @Before
  public void init() {
    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration node2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);
    node1.setDeviceType(DeviceType.HOST);
    node2.setDeviceType(DeviceType.ROUTER);
    _ctxtB =
        MockSpecifierContext.builder().setConfigs(ImmutableMap.of("node1", node1, "node2", node2));
    _ctxt = _ctxtB.build();
  }

  @Test
  public void testResolveDifference() {
    assertThat(
        new ParboiledNodeSpecifier(
                new DifferenceNodeAstNode(
                    new NameRegexNodeAstNode("node.*"), new NameNodeAstNode("node1")))
            .resolve(_ctxt),
        equalTo(ImmutableSet.of("node2")));
  }

  @Test
  public void testResolveRole() {
    String roleName = "role";
    String dimensionName = "dim";
    _ctxtB.setNodeRoleDimensions(
        ImmutableSortedSet.of(
            NodeRoleDimension.builder()
                .setName(dimensionName)
                .setRoles(ImmutableSet.of(new NodeRole(roleName, "node1.*")))
                .build()));
    assertThat(
        new ParboiledNodeSpecifier(new RoleNodeAstNode(roleName, dimensionName))
            .resolve(_ctxtB.build()),
        equalTo(ImmutableSet.of("node1")));
  }

  @Test
  public void testResolveIntersection() {
    assertThat(
        new ParboiledNodeSpecifier(
                new IntersectionNodeAstNode(
                    new NameRegexNodeAstNode("node.*"), new NameNodeAstNode("node1")))
            .resolve(_ctxt),
        equalTo(ImmutableSet.of("node1")));
  }

  @Test
  public void testResolveName() {
    assertThat(
        new ParboiledNodeSpecifier(new NameNodeAstNode("node1")).resolve(_ctxt),
        equalTo(ImmutableSet.of("node1")));
    // This regex looking name below should not match anything
    assertThat(
        new ParboiledNodeSpecifier(new NameNodeAstNode("node.*")).resolve(_ctxt),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveNameRegex() {
    assertThat(
        new ParboiledNodeSpecifier(new NameRegexNodeAstNode("node1.*")).resolve(_ctxt),
        equalTo(ImmutableSet.of("node1")));
  }

  @Test
  public void testResolveType() {
    assertThat(
        new ParboiledNodeSpecifier(new TypeNodeAstNode("host")).resolve(_ctxt),
        equalTo(ImmutableSet.of("node1")));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledNodeSpecifier(
                new UnionNodeAstNode(new NameNodeAstNode("node1"), new NameNodeAstNode("node2")))
            .resolve(_ctxt),
        equalTo(ImmutableSet.of("node1", "node2")));
  }
}
