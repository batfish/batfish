package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessage;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.function.Function;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.InputValidationNotes;
import org.batfish.datamodel.answers.InputValidationNotes.Validity;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.junit.Test;

/** Tests for {@link ParboiledInputValidator} */
public class ParboiledInputValidatorTest {

  private static ParboiledInputValidator getTestPIV(String query) {
    return getTestPIV(
        query,
        CompletionMetadata.builder().build(),
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
  }

  private static ParboiledInputValidator getTestPIV(
      String query, CompletionMetadata completionMetadata) {
    return getTestPIV(
        query, completionMetadata, NodeRolesData.builder().build(), new ReferenceLibrary(null));
  }

  private static ParboiledInputValidator getTestPIV(String query, NodeRolesData nodeRolesData) {
    return getTestPIV(
        query, CompletionMetadata.builder().build(), nodeRolesData, new ReferenceLibrary(null));
  }

  private static ParboiledInputValidator getTestPIV(
      String query,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    TestParser parser = TestParser.instance();
    return new ParboiledInputValidator(
        parser, Grammar.NODE_SPECIFIER, query, completionMetadata, nodeRolesData, referenceLibrary);
  }

  private static IllegalArgumentException getException(
      String query, Function<String, AstNode> getter) {
    try {
      AstNode ignore = getter.apply(query);
      assert ignore != null; // prevent unused warning
    } catch (IllegalArgumentException e) {
      return e;
    }
    return null;
  }

  @Test
  public void testInvalidInput() {
    String query = "(a";
    assertThat(
        getTestPIV(query).run(),
        equalTo(
            new InputValidationNotes(
                Validity.INVALID,
                getErrorMessage(Grammar.NODE_SPECIFIER.getFriendlyName(), query.length()),
                query.length())));
  }

  @Test
  public void testInvalidIp() {
    String query = "1.1.1.345";

    IllegalArgumentException exception = getException(query, IpAstNode::new);

    assertThat(
        getTestPIV(query).run(),
        equalTo(new InputValidationNotes(Validity.INVALID, getErrorMessage(exception), -1)));
  }

  @Test
  public void testInvalidRegex() {
    String pattern = "*a";

    IllegalArgumentException exception = getException(pattern, RegexAstNode::new);

    assertThat(
        getTestPIV("/" + pattern + "/").run(),
        equalTo(new InputValidationNotes(Validity.INVALID, getErrorMessage(exception), -1)));
  }

  /**
   * Test that we create the right type of notes object when encountering things that don't match.
   * The actual content of the messages is tested inside individual validators
   */
  @Test
  public void testNoMatch() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2")).build();
    String query = "a";
    assertThat(
        getTestPIV(query, completionMetadata).run(),
        equalTo(
            new InputValidationNotes(
                Validity.NO_MATCH, getErrorMessageMissingName(query, "Device"))));
  }

  /** Test that we create the right type of notes object when expanding things. */
  @Test
  public void testExpand() {
    CompletionMetadata completionMetadata =
        CompletionMetadata.builder().setNodes(ImmutableSet.of("b1", "b2", "a1")).build();
    String query = "/b/";
    assertThat(
        getTestPIV(query, completionMetadata).run(),
        equalTo(new InputValidationNotes(Validity.VALID, ImmutableList.of("b1", "b2"))));
  }
}
