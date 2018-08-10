package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BfConsts;

public class AnalysisAnswerMetricsResult {
  private static final String PROP_METRICS = "metrics";

  private final Metrics _metrics;

  private final AnswerStatus _status;

  public AnalysisAnswerMetricsResult(
      @JsonProperty(PROP_METRICS) @Nullable Metrics metrics,
      @JsonProperty(BfConsts.PROP_STATUS) @Nonnull AnswerStatus status) {
    _metrics = metrics;
    _status = status;
  }

  @JsonProperty(PROP_METRICS)
  public Metrics getMetrics() {
    return _metrics;
  }

  @JsonProperty(BfConsts.PROP_STATUS)
  public AnswerStatus getStatus() {
    return _status;
  }
}
