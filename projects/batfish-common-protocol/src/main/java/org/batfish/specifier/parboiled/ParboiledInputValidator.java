package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.InputValidationNotes;
import org.batfish.datamodel.answers.InputValidationNotes.Validity;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ValueStack;

/** A helper class that provides auto complete suggestions */
@ParametersAreNonnullByDefault
public final class ParboiledInputValidator {

  private final CommonParser _parser;
  private final Grammar _grammar;
  private final Map<String, Anchor.Type> _completionTypes;

  private final String _network;
  private final String _snapshot;
  private final String _query;
  private final int _maxSuggestions;
  private final CompletionMetadata _completionMetadata;
  private final NodeRolesData _nodeRolesData;
  private final ReferenceLibrary _referenceLibrary;

  ParboiledInputValidator(
      CommonParser parser,
      Grammar grammar,
      Map<String, Anchor.Type> completionTypes,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    _parser = parser;
    _grammar = grammar;
    _completionTypes = completionTypes;
    _network = network;
    _snapshot = snapshot;
    _query = query;
    _maxSuggestions = maxSuggestions;
    _completionMetadata = completionMetadata;
    _nodeRolesData = nodeRolesData;
    _referenceLibrary = referenceLibrary;
  }

  public static InputValidationNotes validate(
      Grammar grammar,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    Parser parser = Parser.instance();
    return new ParboiledInputValidator(
            parser,
            grammar,
            Parser.ANCHORS,
            network,
            snapshot,
            query,
            maxSuggestions,
            completionMetadata,
            nodeRolesData,
            referenceLibrary)
        .run();
  }

  /** This is the entry point for all auto completions */
  InputValidationNotes run() {

    ParsingResult<AstNode> result = null;
    try {
      result = new ReportingParseRunner<AstNode>(_parser.getInputRule(_grammar)).run(_query);
    } catch (ParserRuntimeException e) {
      if (e.getCause() instanceof PatternSyntaxException) {
        return new InputValidationNotes(
            Validity.INVALID, getErrorMessageRegex((PatternSyntaxException) e.getCause()), -1);
      } else {
        return new InputValidationNotes(Validity.INVALID, e.getMessage(), -1);
      }
    }

    if (!result.parseErrors.isEmpty()) {
      InvalidInputError error = (InvalidInputError) result.parseErrors.get(0);
      return new InputValidationNotes(
          Validity.INVALID,
          getErrorMessage(_grammar.getFriendlyName(), error.getStartIndex()),
          error.getStartIndex());
    }

    ValueStack<AstNode> stack = _parser.getShadowStack().getValueStack();
    checkArgument(stack.size() == 1, "Unexpected stack size for input '%s'", _query);

    List<String> emptyMessages = emptyMessages(stack.peek());

    if (!emptyMessages.isEmpty()) {
      return new InputValidationNotes(Validity.EMPTY, emptyMessages.get(0));
    }

    List<String> expansions = expand(stack.peek());

    return new InputValidationNotes(Validity.VALID, expansions);
  }

  @VisibleForTesting
  static String getErrorMessage(String grammarName, int startIndex) {
    return String.format("Cannot parse input as %s at index %d", grammarName, startIndex);
  }

  @VisibleForTesting
  static String getErrorMessageRegex(PatternSyntaxException exception) {
    return String.format(
        "Invalid regular expression '%s': %s", exception.getPattern(), exception.getDescription());
  }

  @VisibleForTesting
  List<String> emptyMessages(AstNode astNode) {
    // TODO: other types
    if (astNode instanceof NameNodeAstNode) {
      // TODO: case insensitive match
      if (!_completionMetadata.getNodes().contains(((NameNodeAstNode) astNode).getName())) {
        return ImmutableList.of(
            String.format("%s does not match any device", ((NameNodeAstNode) astNode).getName()));
      }
    }

    return ImmutableList.of();
  }

  @VisibleForTesting
  List<String> expand(AstNode astNode) {
    if (astNode instanceof NameRegexNodeAstNode) {
      return _completionMetadata.getNodes().stream()
          .filter(name -> ((NameRegexNodeAstNode) astNode).getPattern().matcher(name).find())
          .collect(ImmutableList.toImmutableList());
    }
    return ImmutableList.of();
  }
}
