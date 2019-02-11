package org.batfish.specifier.parboiled;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutoCompleteUtils;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.questions.Variable.Type;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** A helper class that provides auto complete suggestions */
public final class ParboiledAutoComplete {

  private static List<AutocompleteSuggestion> autoCompleteExpression(
      Rule expression,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    ParsingResult<?> result =
        new ReportingParseRunner<>(Parser.INSTANCE.input(expression)).run(query);

    // this is valid input, so we don't get any other potential matches
    // to force the issue, we make the string illegal by adding a non-ascii character (soccer ball)
    if (result.parseErrors.isEmpty()) {
      result =
          new ReportingParseRunner<>(Parser.INSTANCE.input(expression))
              .run(query + new String(Character.toChars(0x26bd)));
    }

    if (result.parseErrors.isEmpty()) {
      throw new IllegalStateException("Failed to force erroneous input");
    }

    InvalidInputError error = (InvalidInputError) result.parseErrors.get(0);
    Set<PartialMatch> partialMatches =
        ParserUtils.getPartialMatches(error, Parser.COMPLETION_TYPES);

    // first add string literals and then add others to the list. we do this because there can be
    // many suggestions based on dynamic completion (e.g., all nodes in the snapshot) and we do not
    // want them to drown everything else out
    List<AutocompleteSuggestion> suggestions =
        partialMatches.stream()
            .filter(pm -> pm.getCompletionType().equals(Completion.Type.STRING_LITERAL))
            .map(
                pm ->
                    autoCompletePartialMatch(
                        pm,
                        error.getStartIndex(),
                        maxSuggestions,
                        completionMetadata,
                        nodeRolesData,
                        referenceLibrary))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    suggestions.addAll(
        partialMatches.stream()
            .filter(pm -> !pm.getCompletionType().equals(Completion.Type.STRING_LITERAL))
            .map(
                pm ->
                    autoCompletePartialMatch(
                        pm,
                        error.getStartIndex(),
                        maxSuggestions,
                        completionMetadata,
                        nodeRolesData,
                        referenceLibrary))
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));

    return suggestions;
  }

  public static List<AutocompleteSuggestion> autoCompleteIpSpace(
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return autoCompleteExpression(
        Parser.INSTANCE.IpSpaceExpression(),
        query,
        maxSuggestions,
        completionMetadata,
        nodeRolesData,
        referenceLibrary);
  }

  @VisibleForTesting
  static List<AutocompleteSuggestion> autoCompletePartialMatch(
      PartialMatch pm,
      int startIndex,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {

    switch (pm.getCompletionType()) {
      case STRING_LITERAL:
        return ImmutableList.of(
            new AutocompleteSuggestion(pm.getMatchCompletion(), true, null, -1, startIndex));
      case IP_ADDRESS:
        return AutoCompleteUtils.autoComplete(
            null,
            null,
            Type.IP,
            pm.getMatchPrefix(),
            maxSuggestions,
            completionMetadata,
            nodeRolesData,
            referenceLibrary);
      default:
        // ignore things we do not know how to auto complete
        return ImmutableList.of();
    }
  }
}
