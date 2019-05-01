package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledInputValidator.getErrorMessageEmptyDeviceRegex;
import static org.batfish.specifier.parboiled.ParboiledInputValidator.getErrorMessageMissingDevice;
import static org.batfish.specifier.parboiled.ParboiledInputValidator.getErrorMessageMissingNodeRole;
import static org.batfish.specifier.parboiled.ParboiledInputValidator.getErrorMessageMissingNodeRoleDimension;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.parboiled.ParboiledInputValidator.ValidatorSpecifierContext;
import org.junit.Test;

/** Tests for {@link NodeValidator} */
public class NodeValidatorTest {

  private static SpecifierContext createSpecifierContext(CompletionMetadata completionMetadata) {
    return new ValidatorSpecifierContext(
        completionMetadata, NodeRolesData.builder().build(), new ReferenceLibrary(null));
  }

  private static SpecifierContext createSpecifierContext(NodeRolesData nodeRolesData) {
    return new ValidatorSpecifierContext(
        CompletionMetadata.builder().build(), nodeRolesData, new ReferenceLibrary(null));
  }

  @Test
  public void testNodeName() {
    SpecifierContext ctxt =
        createSpecifierContext(
            CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build());
    assertThat(
        new NodeValidator(new NameNodeAstNode("a")).emptyMessages(ctxt),
        equalTo(ImmutableList.of(getErrorMessageMissingDevice("a"))));
    assertThat(
        new NodeValidator(new NameNodeAstNode("b1")).emptyMessages(ctxt),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testNodeRegex() {
    SpecifierContext ctxt =
        createSpecifierContext(
            CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build());
    assertThat(
        new NodeValidator(new NameRegexNodeAstNode("a")).emptyMessages(ctxt),
        equalTo(ImmutableList.of((getErrorMessageEmptyDeviceRegex("a")))));
    assertThat(
        new NodeValidator(new NameRegexNodeAstNode("b")).emptyMessages(ctxt),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testNodeRole() {
    SpecifierContext ctxt =
        createSpecifierContext(
            NodeRolesData.builder()
                .setRoleDimensions(
                    ImmutableSortedSet.of(
                        NodeRoleDimension.builder()
                            .setName("dim1")
                            .setRoles(
                                ImmutableSet.of(new NodeRole("r1", ".*"), new NodeRole("r2", ".*")))
                            .build()))
                .build());
    assertThat(
        new NodeValidator(new RoleNodeAstNode("r1", "nodim")).emptyMessages(ctxt),
        equalTo(ImmutableList.of((getErrorMessageMissingNodeRoleDimension("nodim")))));
    assertThat(
        new NodeValidator(new RoleNodeAstNode("norole", "dim1")).emptyMessages(ctxt),
        equalTo(ImmutableList.of((getErrorMessageMissingNodeRole("norole", "dim1")))));
    assertThat(
        new NodeValidator(new RoleNodeAstNode("r1", "dim1")).emptyMessages(ctxt),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testSetOp() {
    SpecifierContext ctxt =
        createSpecifierContext(
            CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build());
    List<String> expected =
        ImmutableList.of(getErrorMessageMissingDevice("a1"), getErrorMessageMissingDevice("a2"));
    // union
    assertThat(
        new NodeValidator(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("a2")))
            .emptyMessages(ctxt),
        equalTo(expected));
    // difference
    assertThat(
        new NodeValidator(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("a2")))
            .emptyMessages(ctxt),
        equalTo(expected));
    // intersection
    assertThat(
        new NodeValidator(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("a2")))
            .emptyMessages(ctxt),
        equalTo(expected));
  }

  /** a subset of the input is empty */
  @Test
  public void testEmptySubset() {
    SpecifierContext ctxt =
        createSpecifierContext(
            CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build());
    assertThat(
        new NodeValidator(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("b1")))
            .emptyMessages(ctxt),
        equalTo(ImmutableList.of(getErrorMessageMissingDevice("a1"))));
  }
}
