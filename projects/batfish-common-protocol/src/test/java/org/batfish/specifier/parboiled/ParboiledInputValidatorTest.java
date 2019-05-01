package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledInputValidator.getErrorMessage;
import static org.batfish.specifier.parboiled.ParboiledInputValidator.getErrorMessageRegex;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.InputValidationNotes;
import org.batfish.datamodel.answers.InputValidationNotes.Validity;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.junit.Test;

/** Tests for {@link ParboiledInputValidator} */
public class ParboiledInputValidatorTest {

  private static ParboiledInputValidator getTestPIV(String query) {
    TestParser parser = TestParser.instance();
    return new ParboiledInputValidator(
        parser,
        Grammar.NODE_SPECIFIER,
        TestParser.ANCHORS,
        "network",
        "snapshot",
        query,
        Integer.MAX_VALUE,
        CompletionMetadata.builder().build(),
        NodeRolesData.builder().build(),
        new ReferenceLibrary(null));
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
  public void testInvalidRegex() {
    String pattern = "*a";

    PatternSyntaxException exception = null;
    try {
      Pattern.compile("*a", Pattern.CASE_INSENSITIVE);
    } catch (PatternSyntaxException e) {
      exception = e;
    }

    assertThat(
        getTestPIV("/" + pattern + "/").run(),
        equalTo(new InputValidationNotes(Validity.INVALID, getErrorMessageRegex(exception), -1)));
  }
}
