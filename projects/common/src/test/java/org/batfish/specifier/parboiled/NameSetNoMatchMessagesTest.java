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
import org.batfish.specifier.Grammar;
import org.junit.Test;

/** Tests for {@link NameSetNoMatchMessages} */
public class NameSetNoMatchMessagesTest {

  private static List<String> getMessages(
      NameSetNoMatchMessages nodeNoMatchMessages, CompletionMetadata completionMetadata) {
    return nodeNoMatchMessages.get(
        completionMetadata, NodeRolesData.builder().build(), new ReferenceLibrary(null));
  }

  private static String NAME_TYPE = Grammar.getNameType(Grammar.MLAG_ID_SPECIFIER);

  @Test
  public void testName() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setMlagIds(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(
            new NameSetNoMatchMessages(new SingletonNameSetAstNode("a"), Grammar.MLAG_ID_SPECIFIER),
            completionMetadata),
        equalTo(ImmutableList.of(getErrorMessageMissingName("a", NAME_TYPE))));
    assertThat(
        getMessages(
            new NameSetNoMatchMessages(
                new SingletonNameSetAstNode("b1"), Grammar.MLAG_ID_SPECIFIER),
            completionMetadata),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testNameRegex() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setMlagIds(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(
            new NameSetNoMatchMessages(new RegexNameSetAstNode("a"), Grammar.MLAG_ID_SPECIFIER),
            completionMetadata),
        equalTo(ImmutableList.of((getErrorMessageEmptyNameRegex("a", NAME_TYPE)))));
    assertThat(
        getMessages(
            new NameSetNoMatchMessages(new RegexNameSetAstNode("b"), Grammar.MLAG_ID_SPECIFIER),
            completionMetadata),
        equalTo(ImmutableList.of()));
  }

  @Test
  public void testSetOp() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setMlagIds(ImmutableSet.of("b1", "b2")).build();
    List<String> expected =
        ImmutableList.of(
            getErrorMessageMissingName("a1", NAME_TYPE),
            getErrorMessageMissingName("a2", NAME_TYPE));
    // union
    assertThat(
        getMessages(
            new NameSetNoMatchMessages(
                new UnionNameSetAstNode(
                    new SingletonNameSetAstNode("a1"), new SingletonNameSetAstNode("a2")),
                Grammar.MLAG_ID_SPECIFIER),
            completionMetadata),
        equalTo(expected));
  }

  /** a subset of the input is empty */
  @Test
  public void testEmptySubset() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setMlagIds(ImmutableSet.of("b1", "b2")).build();
    assertThat(
        getMessages(
            new NameSetNoMatchMessages(
                new UnionNameSetAstNode(
                    new SingletonNameSetAstNode("a1"), new SingletonNameSetAstNode("b1")),
                Grammar.MLAG_ID_SPECIFIER),
            completionMetadata),
        equalTo(ImmutableList.of(getErrorMessageMissingName("a1", NAME_TYPE))));
  }
}
