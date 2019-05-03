package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessage;

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
  private final String _query;
  private final CompletionMetadata _completionMetadata;
  private final NodeRolesData _nodeRolesData;
  private final ReferenceLibrary _referenceLibrary;
  private final SpecifierContext _specifierContext;

  /**
   * Builds a best-effort config object from provided information so that we can expand the
   * specifier. We are creating this object because the actual, fully-populated context is not
   * available as this functionality is invoked by the coordinator and not the worker.
   *
   * <p>The current implementation is good only for node names, regexes, and roles. We can do
   * better, e.g., with interfaces by pulling out interface information from completion metadata and
   * attaching to nodes in configs. Those enhancements are left for the future.
   */
  static class ValidatorSpecifierContext implements SpecifierContext {

    private final Map<String, Configuration> _configs;
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

      _configs =
          _completionMetadata.getNodes().stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      n -> n, n -> new Configuration(n, ConfigurationFormat.UNKNOWN)));
    }

    @Nonnull
    @Override
    public Map<String, Configuration> getConfigs() {
      return _configs;
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
      String query,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    _parser = parser;
    _grammar = grammar;
    _query = query;
    _completionMetadata = completionMetadata;
    _nodeRolesData = nodeRolesData;
    _referenceLibrary = referenceLibrary;
    _specifierContext =
        new ValidatorSpecifierContext(_completionMetadata, _nodeRolesData, _referenceLibrary);
  }

  public static InputValidationNotes validate(
      Grammar grammar,
      String query,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    Parser parser = Parser.instance();
    return new ParboiledInputValidator(
            parser, grammar, query, completionMetadata, nodeRolesData, referenceLibrary)
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
    List<String> noMatchMessages = noMatchMessages(Iterables.getOnlyElement(stack));
    if (!noMatchMessages.isEmpty()) {
      return new InputValidationNotes(Validity.NO_MATCH, noMatchMessages.get(0));
    }

    Set<String> expansions = expand(stack.peek());
    return new InputValidationNotes(Validity.VALID, ImmutableList.copyOf(expansions));
  }

  private List<String> noMatchMessages(AstNode astNode) {
    if (astNode instanceof FilterAstNode) {
      return new FilterNoMatchMessages((FilterAstNode) astNode)
          .get(_completionMetadata, _nodeRolesData, _referenceLibrary);
    } else if (astNode instanceof InterfaceAstNode) {
      return new InterfaceNoMatchMessages((InterfaceAstNode) astNode)
          .get(_completionMetadata, _nodeRolesData, _referenceLibrary);
    } else if (astNode instanceof NodeAstNode) {
      return new NodeNoMatchMessages((NodeAstNode) astNode)
          .get(_completionMetadata, _nodeRolesData, _referenceLibrary);
    }
    return ImmutableList.of();
  }

  /** Only expand for nodes for now */
  private Set<String> expand(AstNode astNode) {
    if (astNode instanceof NodeAstNode) {
      return new ParboiledNodeSpecifier((NodeAstNode) astNode).resolve(_specifierContext);
    }
    return ImmutableSet.of();
  }
}
