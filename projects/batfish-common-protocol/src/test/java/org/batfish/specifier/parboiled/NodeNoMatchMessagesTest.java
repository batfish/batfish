package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageEmptyNameRegex;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingBook;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingGroup;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingName;
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
import org.junit.Test;

/** Tests for {@link NodeNoMatchMessages} */
public class NodeNoMatchMessagesTest {

  private static List<String> getMessages(
      NodeNoMatchMessages nodeNoMatchMessages, CompletionMetadata completionMetadata) {
    return nodeNoMatchMessages.get(
        completionMetadata, NodeRolesData.builder().build(), new ReferenceLibrary(null));
  }

  private static List<String> getMessages(
      NodeNoMatchMessages nodeNoMatchMessages, NodeRolesData nodeRolesData) {
    return nodeNoMatchMessages.get(
        CompletionMetadata.builder().build(), nodeRolesData, new ReferenceLibrary(null));
  }

  @Test
  public void testNodeName() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(new NodeNoMatchMessages(new NameNodeAstNode("a")), completionMetadata),
        equalTo(ImmutableList.of(getErrorMessageMissingName("a", "device"))));
    assertThat(
        getMessages(new NodeNoMatchMessages(new NameNodeAstNode("b1")), completionMetadata),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testNodeRegex() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(new NodeNoMatchMessages(new NameRegexNodeAstNode("a")), completionMetadata),
        equalTo(ImmutableList.of((getErrorMessageEmptyNameRegex("a", "device")))));
    assertThat(
        getMessages(new NodeNoMatchMessages(new NameRegexNodeAstNode("b")), completionMetadata),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testNodeRole() {
    NodeRolesData nodeRolesData =
        NodeRolesData.builder()
            .setRoleDimensions(
                ImmutableSortedSet.of(
                    NodeRoleDimension.builder()
                        .setName("dim1")
                        .setRoles(
                            ImmutableSet.of(new NodeRole("r1", ".*"), new NodeRole("r2", ".*")))
                        .build()))
            .build();
    assertThat(
        getMessages(new NodeNoMatchMessages(new RoleNodeAstNode("r1", "nodim")), nodeRolesData),
        equalTo(ImmutableList.of((getErrorMessageMissingBook("nodim", "Node role dimension")))));
    assertThat(
        getMessages(new NodeNoMatchMessages(new RoleNodeAstNode("norole", "dim1")), nodeRolesData),
        equalTo(
            ImmutableList.of(
                (getErrorMessageMissingGroup("norole", "Node role", "dim1", "dimension")))));
    assertThat(
        getMessages(new NodeNoMatchMessages(new RoleNodeAstNode("r1", "dim1")), nodeRolesData),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testSetOp() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build();
    List<String> expected =
        ImmutableList.of(
            getErrorMessageMissingName("a1", "device"), getErrorMessageMissingName("a2", "device"));
    // union
    assertThat(
        getMessages(
            new NodeNoMatchMessages(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("a2"))),
            completionMetadata),
        equalTo(expected));
    // difference
    assertThat(
        getMessages(
            new NodeNoMatchMessages(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("a2"))),
            completionMetadata),
        equalTo(expected));
    // intersection
    assertThat(
        getMessages(
            new NodeNoMatchMessages(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("a2"))),
            completionMetadata),
        equalTo(expected));
  }

  /** a subset of the input is empty */
  @Test
  public void testEmptySubset() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(
            new NodeNoMatchMessages(
                new UnionNodeAstNode(new NameNodeAstNode("a1"), new NameNodeAstNode("b1"))),
            completionMetadata),
        equalTo(ImmutableList.of(getErrorMessageMissingName("a1", "device"))));
  }
}
