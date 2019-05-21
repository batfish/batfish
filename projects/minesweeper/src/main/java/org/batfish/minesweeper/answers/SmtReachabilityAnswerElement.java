package org.batfish.minesweeper.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.minesweeper.smt.VerificationResult;

public class SmtReachabilityAnswerElement extends AnswerElement {
  private static final String PROP_RESULT = "result";
  private static final String PROP_FLOW_HISTORY = "flowHistory";

  private VerificationResult _result;

  private FlowHistory _flowHistory;

  @JsonCreator
  public SmtReachabilityAnswerElement(
      @JsonProperty(PROP_RESULT) VerificationResult result,
      @JsonProperty(PROP_FLOW_HISTORY) FlowHistory flowHistory) {
    _result = result;
    _flowHistory = flowHistory;
  }

  @JsonProperty(PROP_RESULT)
  public VerificationResult getResult() {
    return _result;
  }

  @JsonProperty(PROP_FLOW_HISTORY)
  public FlowHistory getFlowHistory() {
    return _flowHistory;
  }
}
