package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageEmptyNameRegex;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.junit.Test;

/** Tests for {@link FilterNoMatchMessages} */
public class FilterNoMatchMessagesTest {

  private static List<String> getMessages(
      FilterNoMatchMessages nodeNoMatchMessages, CompletionMetadata completionMetadata) {
    return nodeNoMatchMessages.get(
        completionMetadata, NodeRolesData.builder().build(), new ReferenceLibrary(null));
  }

  @Test
  public void testName() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setFilterNames(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(new FilterNoMatchMessages(new NameFilterAstNode("a")), completionMetadata),
        equalTo(ImmutableList.of(getErrorMessageMissingName("a", "filter"))));
    assertThat(
        getMessages(new FilterNoMatchMessages(new NameFilterAstNode("b1")), completionMetadata),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testNameRegex() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setFilterNames(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(new FilterNoMatchMessages(new NameRegexFilterAstNode("a")), completionMetadata),
        equalTo(ImmutableList.of((getErrorMessageEmptyNameRegex("a", "filter")))));
    assertThat(
        getMessages(new FilterNoMatchMessages(new NameRegexFilterAstNode("b")), completionMetadata),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testWithNode() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder()
            .setNodes(ImmutableSet.of("h1", "h2"))
            .setFilterNames(ImmutableSet.of("f1", "f2"))
            .build();
    assertThat(
        getMessages(
            new FilterNoMatchMessages(
                new FilterWithNodeFilterAstNode(
                    new NameNodeAstNode("no"), new NameFilterAstNode("no"))),
            completionMetadata),
        equalTo(
            ImmutableList.of(
                getErrorMessageMissingName("no", "device"),
                getErrorMessageMissingName("no", "filter"))));
    // no no_match messages since the logic is not context sensitive
    assertThat(
        getMessages(
            new FilterNoMatchMessages(
                new FilterWithNodeFilterAstNode(
                    new NameNodeAstNode("h1"), new NameFilterAstNode("f2"))),
            completionMetadata),
        equalTo(ImmutableList.of()));
    assertThat(
        getMessages(
            new FilterNoMatchMessages(
                new FilterWithNodeFilterAstNode(
                    new NameNodeAstNode("h1"), new NameFilterAstNode("f1"))),
            completionMetadata),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testSetOp() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setFilterNames(ImmutableSet.of("b1", "b2")).build();
    List<String> expected =
        ImmutableList.of(
            getErrorMessageMissingName("a1", "filter"), getErrorMessageMissingName("a2", "filter"));
    // union
    assertThat(
        getMessages(
            new FilterNoMatchMessages(
                new UnionFilterAstNode(new NameFilterAstNode("a1"), new NameFilterAstNode("a2"))),
            completionMetadata),
        equalTo(expected));
    // difference
    assertThat(
        getMessages(
            new FilterNoMatchMessages(
                new UnionFilterAstNode(new NameFilterAstNode("a1"), new NameFilterAstNode("a2"))),
            completionMetadata),
        equalTo(expected));
    // intersection
    assertThat(
        getMessages(
            new FilterNoMatchMessages(
                new UnionFilterAstNode(new NameFilterAstNode("a1"), new NameFilterAstNode("a2"))),
            completionMetadata),
        equalTo(expected));
  }

  /** a subset of the input is empty */
  @Test
  public void testEmptySubset() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setFilterNames(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(
            new FilterNoMatchMessages(
                new UnionFilterAstNode(new NameFilterAstNode("a1"), new NameFilterAstNode("b1"))),
            completionMetadata),
        equalTo(ImmutableList.of(getErrorMessageMissingName("a1", "filter"))));
  }
}
