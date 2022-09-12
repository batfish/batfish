package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Response to WorkMgrv2 autocomplete REST request */
@ParametersAreNonnullByDefault
public final class AutocompleteResponse {

  @JsonProperty(PROP_QUERY_METADATA)
  public @Nonnull InputValidationNotes getQueryMetadata() {
    return _queryMetadata;
  }

  @JsonProperty(PROP_SUGGESTIONS)
  public @Nonnull List<AutocompleteSuggestion> getSuggestions() {
    return _suggestions;
  }

  public static @Nonnull AutocompleteResponse of(
      InputValidationNotes queryMetadata, List<AutocompleteSuggestion> suggestions) {
    return new AutocompleteResponse(queryMetadata, suggestions);
  }

  @JsonCreator
  private static @Nonnull AutocompleteResponse create(
      @JsonProperty(PROP_QUERY_METADATA) @Nullable InputValidationNotes queryMetadata,
      @JsonProperty(PROP_SUGGESTIONS) @Nullable List<AutocompleteSuggestion> suggestions) {
    checkArgument(queryMetadata != null, "Missing %s", PROP_QUERY_METADATA);
    return of(queryMetadata, ImmutableList.copyOf(firstNonNull(suggestions, ImmutableList.of())));
  }

  private AutocompleteResponse(
      InputValidationNotes queryMetadata, List<AutocompleteSuggestion> suggestions) {
    _queryMetadata = queryMetadata;
    _suggestions = suggestions;
  }

  private static final String PROP_SUGGESTIONS = "suggestions";
  private static final String PROP_QUERY_METADATA = "querymetadata";

  private final @Nonnull List<AutocompleteSuggestion> _suggestions;
  private final @Nonnull InputValidationNotes _queryMetadata;
}
