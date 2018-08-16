package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BfConsts;

public class AnalysisAnswerMetricsResult {

  @JsonCreator
  private static @Nonnull AnalysisAnswerMetricsResult create(
      @JsonProperty(BfConsts.PROP_METRICS) Metrics metrics,
      @JsonProperty(BfConsts.PROP_STATUS) AnswerStatus status) {
    return new AnalysisAnswerMetricsResult(metrics, requireNonNull(status));
  }

  private final Metrics _metrics;

  private final AnswerStatus _status;

  public AnalysisAnswerMetricsResult(@Nullable Metrics metrics, @Nonnull AnswerStatus status) {
    _metrics = metrics;
    _status = status;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AnalysisAnswerMetricsResult)) {
      return false;
    }
    AnalysisAnswerMetricsResult rhs = (AnalysisAnswerMetricsResult) obj;
    return Objects.equals(_metrics, rhs._metrics) && _status == rhs._status;
  }

  @JsonProperty(BfConsts.PROP_METRICS)
  public @Nullable Metrics getMetrics() {
    return _metrics;
  }

  @JsonProperty(BfConsts.PROP_STATUS)
  public @Nonnull AnswerStatus getStatus() {
    return _status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_metrics, _status.ordinal());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(BfConsts.PROP_METRICS, _metrics)
        .add(BfConsts.PROP_STATUS, _status)
        .toString();
  }
}
