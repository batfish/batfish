package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.NoSuchElementException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RoleNameNodeSpecifierTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

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
                      .setRoles(
                          ImmutableSortedSet.of(
                              new NodeRole("role1", "node1.*"), new NodeRole("role2", "node2.*")))
                      .build(),
                  NodeRoleDimension.builder()
                      .setName("dim2")
                      .setRoles(ImmutableSortedSet.of(new NodeRole("role1", ".*")))
                      .build()))
          .build();

  @Test
  public void testResolve() {
    assertThat(
        new RoleNameNodeSpecifier("role1", "dim1").resolve(_ctxt),
        equalTo(ImmutableSet.of("node1")));
  }

  @Test
  public void testResolveMissingDimenstion() {
    _thrown.expect(NoSuchElementException.class);
    new RoleNameNodeSpecifier("role1", "nodim").resolve(_ctxt);
  }

  @Test
  public void testResolveMissingRole() {
    _thrown.expect(NoSuchElementException.class);
    new RoleNameNodeSpecifier("norole", "dim1").resolve(_ctxt);
  }
}
