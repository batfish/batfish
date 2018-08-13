package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GetAnalysisAnswerMetricsAnswer {

  private static final String PROP_RESULTS = "results";

  @JsonCreator
  private static @Nonnull GetAnalysisAnswerMetricsAnswer create(
      @JsonProperty(PROP_RESULTS) Map<String, AnalysisAnswerMetricsResult> results) {
    return new GetAnalysisAnswerMetricsAnswer(firstNonNull(results, ImmutableMap.of()));
  }

  private final Map<String, AnalysisAnswerMetricsResult> _results;

  public GetAnalysisAnswerMetricsAnswer(@Nonnull Map<String, AnalysisAnswerMetricsResult> results) {
    _results = results;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GetAnalysisAnswerMetricsAnswer)) {
      return false;
    }
    return _results.equals(((GetAnalysisAnswerMetricsAnswer) obj)._results);
  }

  @JsonProperty(PROP_RESULTS)
  public @Nonnull Map<String, AnalysisAnswerMetricsResult> getResults() {
    return _results;
  }

  @Override
  public int hashCode() {
    return _results.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_RESULTS, _results).toString();
  }
}
