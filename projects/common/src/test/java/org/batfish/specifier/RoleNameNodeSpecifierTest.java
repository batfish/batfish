package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.NoSuchElementException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.RoleDimensionMapping;
import org.junit.jupiter.api.Test;

class RoleNameNodeSpecifierTest {

  private final MockSpecifierContext _ctxt =
      MockSpecifierContext.builder()
          .setConfigs(
              ImmutableMap.of(
                  "node1",
                  new Configuration("node1", ConfigurationFormat.CISCO_IOS),
                  "node2",
                  new Configuration("node2", ConfigurationFormat.CISCO_IOS)))
          .setNodeRoleDimensions(
              ImmutableSet.of(
                  NodeRoleDimension.builder()
                      .setName("dim1")
                      .setRoleDimensionMappings(
                          ImmutableList.of(
                              new RoleDimensionMapping(
                                  "(.*)",
                                  null,
                                  ImmutableMap.of("node1", "role1", "node2", "role2"))))
                      .build(),
                  NodeRoleDimension.builder()
                      .setName("dim2")
                      .setRoleDimensionMappings(
                          ImmutableList.of(
                              new RoleDimensionMapping(
                                  "\\(.*\\)",
                                  null,
                                  ImmutableMap.of("node1", "role1", "node2", "role1"))))
                      .build()))
          .build();

  @Test
  void testResolve() {
    assertThat(
        new RoleNameNodeSpecifier("role1", "dim1").resolve(_ctxt),
        equalTo(ImmutableSet.of("node1")));
  }

  @Test
  void testResolveMissingDimenstion() {
    assertThrows(
        NoSuchElementException.class,
        () -> new RoleNameNodeSpecifier("role1", "nodim").resolve(_ctxt));
  }

  @Test
  void testResolveMissingRole() {
    assertThrows(
        NoSuchElementException.class,
        () -> new RoleNameNodeSpecifier("norole", "dim1").resolve(_ctxt));
  }
}
