package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class GetAnalysisAnswerMetricsAnswer {

  private static final String PROP_RESULTS = "results";

  private final Map<String, AnalysisAnswerMetricsResult> _results;

  @JsonCreator
  public GetAnalysisAnswerMetricsAnswer(
      @JsonProperty(PROP_RESULTS) Map<String, AnalysisAnswerMetricsResult> results) {
    _results = results;
  }

  @JsonProperty(PROP_RESULTS)
  public Map<String, AnalysisAnswerMetricsResult> getResults() {
    return _results;
  }
}
