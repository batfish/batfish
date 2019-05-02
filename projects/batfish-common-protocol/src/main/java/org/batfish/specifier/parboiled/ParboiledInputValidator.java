package org.batfish.specifier.parboiled;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.answers.InputValidationNotes;
import org.batfish.datamodel.answers.InputValidationNotes.Validity;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.SpecifierContext;
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
  private final SpecifierContext _specifierContext;

  static class ValidatorSpecifierContext implements SpecifierContext {

    private final CompletionMetadata _completionMetadata;
    private final NodeRolesData _nodeRolesData;
    private final ReferenceLibrary _referenceLibrary;

    ValidatorSpecifierContext(
        CompletionMetadata completionMetadata,
        NodeRolesData nodeRolesData,
        ReferenceLibrary referenceLibrary) {
      _completionMetadata = completionMetadata;
      _nodeRolesData = nodeRolesData;
      _referenceLibrary = referenceLibrary;
    }

    @Nonnull
    @Override
    public Map<String, Configuration> getConfigs() {
      return _completionMetadata.getNodes().stream()
          .collect(
              ImmutableMap.toImmutableMap(
                  n -> n, n -> new Configuration(n, ConfigurationFormat.UNKNOWN)));
    }

    @Override
    public Optional<ReferenceBook> getReferenceBook(String bookName) {
      return _referenceLibrary.getReferenceBook(bookName);
    }

    @Nonnull
    @Override
    public Optional<NodeRoleDimension> getNodeRoleDimension(@Nullable String dimension) {
      return _nodeRolesData.getNodeRoleDimension(dimension);
    }

    @Override
    public Map<String, Map<String, IpSpace>> getInterfaceOwnedIps() {
      return ImmutableMap.of();
    }

    @Override
    public IpSpace getSnapshotDeviceOwnedIps() {
      return EmptyIpSpace.INSTANCE;
    }
  }

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
    _specifierContext =
        new ValidatorSpecifierContext(completionMetadata, nodeRolesData, referenceLibrary);
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
      if (e.getCause() instanceof IllegalArgumentException) {
        return new InputValidationNotes(
            Validity.INVALID, getErrorMessage((IllegalArgumentException) e.getCause()), -1);
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
    List<String> emptyMessages = emptyMessages(Iterables.getOnlyElement(stack));
    if (!emptyMessages.isEmpty()) {
      return new InputValidationNotes(Validity.EMPTY, emptyMessages.get(0));
    }

    Set<String> expansions = expand(stack.peek());
    return new InputValidationNotes(Validity.VALID, ImmutableList.copyOf(expansions));
  }

  private List<String> emptyMessages(AstNode astNode) {
    if (astNode instanceof NodeAstNode) {
      return new NodeValidator((NodeAstNode) astNode).emptyMessages(_specifierContext);
    }
    return ImmutableList.of();
  }

  private Set<String> expand(AstNode astNode) {
    if (astNode instanceof NodeAstNode) {
      return new ParboiledNodeSpecifier((NodeAstNode) astNode).resolve(_specifierContext);
    }
    return ImmutableSet.of();
  }

  @VisibleForTesting
  static String getErrorMessage(String grammarName, int startIndex) {
    return String.format("Cannot parse input as %s at index %d", grammarName, startIndex);
  }

  @VisibleForTesting
  static String getErrorMessage(IllegalArgumentException exception) {
    return exception.getMessage();
  }

  static String getErrorMessageEmptyDeviceRegex(String regex) {
    return String.format("Regex /%s/ does not match any device name", regex);
  }

  static String getErrorMessageMissingDevice(String nodeName) {
    return String.format("Device %s does not exist", CommonParser.escapeNameIfNeeded(nodeName));
  }

  static String getErrorMessageMissingNodeRole(String role, String dimension) {
    return String.format(
        "Node role %s does not exist in dimension %s.",
        CommonParser.escapeNameIfNeeded(role), CommonParser.escapeNameIfNeeded(dimension));
  }

  static String getErrorMessageMissingNodeRoleDimension(String dimension) {
    return String.format(
        "Node role dimension %s does not exist.", CommonParser.escapeNameIfNeeded(dimension));
  }
}
