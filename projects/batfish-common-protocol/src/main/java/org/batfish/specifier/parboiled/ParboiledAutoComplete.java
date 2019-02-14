package org.batfish.specifier.parboiled;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutoCompleteUtils;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.questions.Variable;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** A helper class that provides auto complete suggestions */
@ParametersAreNonnullByDefault
public final class ParboiledAutoComplete {

  public static final int RANK_STRING_LITERAL = 1;

  private final CommonParser _parser;
  private final Rule _expression;
  private final Map<String, Anchor.Type> _completionTypes;

  private final String _network;
  private final String _snapshot;
  private final String _query;
  private final int _maxSuggestions;
  private final CompletionMetadata _completionMetadata;
  private final NodeRolesData _nodeRolesData;
  private final ReferenceLibrary _referenceLibrary;

  ParboiledAutoComplete(
      CommonParser parser,
      Rule expression,
      Map<String, Anchor.Type> completionTypes,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    _parser = parser;
    _expression = expression;
    _completionTypes = completionTypes;
    _network = network;
    _snapshot = snapshot;
    _query = query;
    _maxSuggestions = maxSuggestions;
    _completionMetadata = completionMetadata;
    _nodeRolesData = nodeRolesData;
    _referenceLibrary = referenceLibrary;
  }

  /** Auto completes IpSpace queries */
  public static List<AutocompleteSuggestion> autoCompleteIpSpace(
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return new ParboiledAutoComplete(
            Parser.INSTANCE,
            Parser.INSTANCE.IpSpaceExpression(),
            Parser.COMPLETION_TYPES,
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
  List<AutocompleteSuggestion> run() {

    /**
     * Before passing the query to the parser, we make it illegal by adding a funny, non-ascii
     * character (soccer ball :)). We will not get any errors backs if the string is legal.
     */
    String testQuery = _query + new String(Character.toChars(0x26bd));
    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(_parser.input(_expression)).run(testQuery);
    if (result.parseErrors.isEmpty()) {
      throw new IllegalStateException("Failed to force erroneous input");
    }

    InvalidInputError error = (InvalidInputError) result.parseErrors.get(0);

    Set<PotentialMatch> potentialMatches =
        ParserUtils.getPotentialMatches(error, _completionTypes, false);

    Set<AutocompleteSuggestion> allSuggestions =
        potentialMatches.stream()
            .map(pm -> autoCompletePotentialMatch(pm, error.getStartIndex()))
            .flatMap(Collection::stream)
            .collect(ImmutableSet.toImmutableSet());

    return allSuggestions.stream()
        .sorted(Comparator.comparing(s -> s.getRank()))
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  List<AutocompleteSuggestion> autoCompletePotentialMatch(PotentialMatch pm, int startIndex) {

    List<AutocompleteSuggestion> suggestions = null;
    switch (pm.getAnchorType()) {
      case STRING_LITERAL:
        /*
         String literals get a lower rank because there can be many suggestions for dynamic values
         (e.g., all nodes in the snapshot) and we do not want them to drown everything else
        */
        return ImmutableList.of(
            new AutocompleteSuggestion(
                pm.getMatchCompletion(), true, null, RANK_STRING_LITERAL, startIndex));
      case ADDRESS_GROUP_AND_BOOK:
      case IP_ADDRESS:
      case IP_PREFIX:
        suggestions =
            AutoCompleteUtils.autoComplete(
                _network,
                _snapshot,
                anchorTypeToVariableType(pm.getAnchorType()),
                pm.getMatchPrefix(),
                _maxSuggestions,
                _completionMetadata,
                _nodeRolesData,
                _referenceLibrary);
        break;
      case IP_RANGE:
      case IP_WILDCARD:
        // These depend on other completion types that should be kicking in
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case EOI:
      case IP_ADDRESS_MASK:
      case WHITESPACE:
        // nothing useful to suggest for these completion types
        return ImmutableList.of();
      default:
        throw new IllegalArgumentException("Unhandled completion type " + pm.getAnchorType());
    }
    return suggestions.stream()
        .map(
            s ->
                new AutocompleteSuggestion(
                    s.getText(),
                    true,
                    s.getDescription(),
                    AutocompleteSuggestion.DEFAULT_RANK,
                    startIndex))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Converts completion type to variable type for cases. Throws an exception when the mapping does
   * not exist
   */
  private static Variable.Type anchorTypeToVariableType(Anchor.Type anchorType) {
    switch (anchorType) {
      case ADDRESS_GROUP_AND_BOOK:
        return Variable.Type.ADDRESS_GROUP_AND_BOOK;
      case IP_ADDRESS:
        return Variable.Type.IP;
      case IP_PREFIX:
        return Variable.Type.PREFIX;
      default:
        throw new IllegalArgumentException("No valid Variable type for Anchor type" + anchorType);
    }
  }
}
