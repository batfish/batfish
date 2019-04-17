package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.AutocompleteSuggestion.DEFAULT_RANK;
import static org.batfish.specifier.parboiled.CommonParser.ESCAPE_CHAR;
import static org.batfish.specifier.parboiled.CommonParser.isEscapableNameAnchor;
import static org.batfish.specifier.parboiled.CommonParser.nameNeedsEscaping;

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
import org.batfish.datamodel.questions.Variable.Type;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** A helper class that provides auto complete suggestions */
@ParametersAreNonnullByDefault
public final class ParboiledAutoComplete {

  static final char ILLEGAL_CHAR = (char) 0x26bd;

  public static final int RANK_STRING_LITERAL = 1;

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
      Rule expression,
      Map<String, Anchor.Type> completionTypes,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
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

  public static List<AutocompleteSuggestion> autoComplete(
      Grammar grammar,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return new ParboiledAutoComplete(
            Parser.instance().getInputRule(grammar),
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
  List<AutocompleteSuggestion> run() {

    /**
     * Before passing the query to the parser, we make it illegal by adding a funny, non-ascii
     * character (soccer ball :)). We will not get any errors backs if the string is legal.
     */
    String testQuery = _query + new String(Character.toChars(ILLEGAL_CHAR));
    ParsingResult<AstNode> result = new ReportingParseRunner<AstNode>(_expression).run(testQuery);
    if (result.parseErrors.isEmpty()) {
      throw new IllegalStateException("Failed to force erroneous input");
    }

    InvalidInputError error = (InvalidInputError) result.parseErrors.get(0);

    Set<PotentialMatch> potentialMatches =
        ParserUtils.getPotentialMatches(error, _completionTypes, false);

    Set<AutocompleteSuggestion> allSuggestions =
        potentialMatches.stream()
            .map(pm -> autoCompletePotentialMatch(pm))
            .flatMap(Collection::stream)
            .collect(ImmutableSet.toImmutableSet());

    return allSuggestions.stream()
        .sorted(
            Comparator.comparing(AutocompleteSuggestion::getRank)
                .thenComparing(s -> s.getText().length()))
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  List<AutocompleteSuggestion> autoCompletePotentialMatch(PotentialMatch pm) {
    switch (pm.getAnchorType()) {
      case ADDRESS_GROUP_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case CHAR_LITERAL:
        /*
         Char and String literals get a lower rank so that the possibly many suggestions for dynamic values
         (e.g., all nodes in the snapshot) do not drown everything else
        */
        return ImmutableList.of(
            new AutocompleteSuggestion(
                pm.getMatch(), true, null, RANK_STRING_LITERAL, pm.getMatchStartIndex()));
      case EOI:
        return ImmutableList.of();
      case FILTER_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case FILTER_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case INTERFACE_GROUP_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case INTERFACE_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case INTERFACE_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case INTERFACE_TYPE:
        // Relies on STRING_LITERAL completion as it appears later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case IP_ADDRESS:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case IP_ADDRESS_MASK:
        // can't help with masks
        return ImmutableList.of();
      case IP_PROTOCOL_NUMBER:
        // don't help with numbers
        return ImmutableList.of();
      case IP_PREFIX:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case IP_RANGE:
        // Relies on IP_ADDRESS completion as it appears later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case IP_WILDCARD:
        // Relies on IP_ADDRESS and IP_ADDRESS_MASK completions as they appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case NODE_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case NODE_ROLE_DIMENSION_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case NODE_ROLE_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case NODE_TYPE:
        // Relies on STRING_LITERAL completion as it appears later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case REFERENCE_BOOK_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case ROUTING_POLICY_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case ROUTING_POLICY_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case STRING_LITERAL:
        /*
         Char and String literals get a lower rank so that the possibly many suggestions for dynamic values
         (e.g., all nodes in the snapshot) do not drown everything else
        */
        return ImmutableList.of(
            new AutocompleteSuggestion(
                pm.getMatch(), true, null, RANK_STRING_LITERAL, pm.getMatchStartIndex()));
      case VRF_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case WHITESPACE:
        // nothing useful to suggest for these completion types
        return ImmutableList.of();
      case ZONE_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      default:
        throw new IllegalArgumentException("Unhandled completion type " + pm.getAnchorType());
    }
  }

  private List<AutocompleteSuggestion> autoCompletePotentialMatch(PotentialMatch pm, int rank) {
    boolean quoted = false;
    String matchPrefix = pm.getMatchPrefix();
    if (isEscapableNameAnchor(pm.getAnchorType()) && matchPrefix.startsWith(ESCAPE_CHAR)) {
      quoted = true;
      matchPrefix = matchPrefix.substring(1);
    }
    final boolean finalizedQuoted = quoted;
    return AutoCompleteUtils.autoComplete(
            _network,
            _snapshot,
            anchorTypeToVariableType(pm.getAnchorType()),
            matchPrefix,
            _maxSuggestions,
            _completionMetadata,
            _nodeRolesData,
            _referenceLibrary)
        .stream()
        .map(
            s ->
                new AutocompleteSuggestion(
                    // escape if needed
                    finalizedQuoted
                            || (isEscapableNameAnchor(pm.getAnchorType())
                                && nameNeedsEscaping(s.getText()))
                        ? ESCAPE_CHAR + s.getText() + ESCAPE_CHAR
                        : s.getText(),
                    true,
                    s.getDescription(),
                    rank,
                    pm.getMatchStartIndex()))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Converts completion type to variable type for cases. Throws an exception when the mapping does
   * not exist
   */
  private static Variable.Type anchorTypeToVariableType(Anchor.Type anchorType) {
    switch (anchorType) {
      case ADDRESS_GROUP_NAME:
        return Variable.Type.ADDRESS_GROUP_NAME;
      case FILTER_NAME:
        return Variable.Type.FILTER_NAME;
      case INTERFACE_GROUP_NAME:
        return Variable.Type.INTERFACE_GROUP_NAME;
      case INTERFACE_NAME:
        return Type.INTERFACE_NAME;
      case INTERFACE_TYPE:
        return Variable.Type.INTERFACE_TYPE;
      case IP_ADDRESS:
        return Variable.Type.IP;
      case IP_PREFIX:
        return Variable.Type.PREFIX;
      case NODE_NAME:
        return Variable.Type.NODE_NAME;
      case NODE_ROLE_NAME:
        return Variable.Type.NODE_ROLE_NAME;
      case NODE_ROLE_DIMENSION_NAME:
        return Variable.Type.NODE_ROLE_DIMENSION_NAME;
      case REFERENCE_BOOK_NAME:
        return Variable.Type.REFERENCE_BOOK_NAME;
      case ROUTING_POLICY_NAME:
        return Variable.Type.ROUTING_POLICY_NAME;
      case VRF_NAME:
        return Variable.Type.VRF;
      case ZONE_NAME:
        return Variable.Type.ZONE;
      default:
        throw new IllegalArgumentException("No valid Variable type for Anchor type" + anchorType);
    }
  }
}
