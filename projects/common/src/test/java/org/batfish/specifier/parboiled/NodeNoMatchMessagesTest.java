package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageEmptyNameRegex;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingBook;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingGroup;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.role.RoleDimensionMapping;
import org.junit.Test;

/** Tests for {@link NodeNoMatchMessages} */
public class NodeNoMatchMessagesTest {

  private static List<String> getMessages(
      NodeNoMatchMessages nodeNoMatchMessages, CompletionMetadata completionMetadata) {
    return getMessages(nodeNoMatchMessages, completionMetadata, NodeRolesData.builder().build());
  }

  private static List<String> getMessages(
      NodeNoMatchMessages nodeNoMatchMessages,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData) {
    return nodeNoMatchMessages.get(completionMetadata, nodeRolesData, new ReferenceLibrary(null));
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
                ImmutableList.of(
                    NodeRoleDimension.builder()
                        .setName("dim1")
                        .setRoleDimensionMappings(
                            ImmutableList.of(new RoleDimensionMapping("(.*)")))
                        .build()))
            .build();
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("r1", "r2")).build();
    assertThat(
        getMessages(
            new NodeNoMatchMessages(new RoleNodeAstNode("nodim", "r1")),
            completionMetadata,
            nodeRolesData),
        equalTo(ImmutableList.of((getErrorMessageMissingBook("nodim", "Node role dimension")))));
    assertThat(
        getMessages(
            new NodeNoMatchMessages(new RoleNodeAstNode("dim1", "norole")),
            completionMetadata,
            nodeRolesData),
        equalTo(
            ImmutableList.of(
                (getErrorMessageMissingGroup("norole", "Node role", "dim1", "dimension")))));
    assertThat(
        getMessages(
            new NodeNoMatchMessages(new RoleNodeAstNode("dim1", "r1")),
            completionMetadata,
            nodeRolesData),
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
