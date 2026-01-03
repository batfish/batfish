package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BfConsts;

public class GetAnalysisAnswerMetricsAnswer {

  @JsonCreator
  private static @Nonnull GetAnalysisAnswerMetricsAnswer create(
      @JsonProperty(BfConsts.PROP_RESULTS) Map<String, AnswerMetadata> results) {
    return new GetAnalysisAnswerMetricsAnswer(firstNonNull(results, ImmutableMap.of()));
  }

  private final Map<String, AnswerMetadata> _results;

  public GetAnalysisAnswerMetricsAnswer(@Nonnull Map<String, AnswerMetadata> results) {
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

  @JsonProperty(BfConsts.PROP_RESULTS)
  public @Nonnull Map<String, AnswerMetadata> getResults() {
    return _results;
  }

  @Override
  public int hashCode() {
    return _results.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(BfConsts.PROP_RESULTS, _results).toString();
  }
}
