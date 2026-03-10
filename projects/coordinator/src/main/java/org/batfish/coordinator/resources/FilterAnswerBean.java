package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.AnswerRowsOptions;

/** Bean holding filter-answer request options */
@ParametersAreNonnullByDefault
public final class FilterAnswerBean {
  private static final String PROP_SNAPSHOT = "snapshot";
  private static final String PROP_REFERENCE_SNAPSHOT = "referenceSnapshot";
  private static final String PROP_FILTER_OPTIONS = "filterOptions";

  /** Name of the snapshot the question was run on */
  @JsonProperty(PROP_SNAPSHOT)
  public final @Nonnull String snapshot;

  /** Name of the reference snapshot for the differential question, if applicable */
  @JsonProperty(PROP_REFERENCE_SNAPSHOT)
  public final @Nullable String referenceSnapshot;

  /** Filtering options to be applied to the retrieved answer */
  @JsonProperty(PROP_FILTER_OPTIONS)
  public final @Nonnull AnswerRowsOptions filterOptions;

  @JsonCreator
  @VisibleForTesting
  static FilterAnswerBean create(
      @JsonProperty(PROP_SNAPSHOT) @Nullable String snapshot,
      @JsonProperty(PROP_REFERENCE_SNAPSHOT) @Nullable String referenceSnapshot,
      @JsonProperty(PROP_FILTER_OPTIONS) @Nullable AnswerRowsOptions answerRowsOptions) {
    checkArgument(snapshot != null, "Snapshot must be specified to fetch question answer");
    return new FilterAnswerBean(
        snapshot, referenceSnapshot, firstNonNull(answerRowsOptions, AnswerRowsOptions.NO_FILTER));
  }

  public FilterAnswerBean(
      String snapshot, @Nullable String referenceSnapshot, AnswerRowsOptions filterOptions) {
    this.snapshot = snapshot;
    this.referenceSnapshot = referenceSnapshot;
    this.filterOptions = filterOptions;
  }
}
