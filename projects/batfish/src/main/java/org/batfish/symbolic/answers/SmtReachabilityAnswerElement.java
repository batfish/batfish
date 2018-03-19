package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.symbolic.smt.VerificationResult;

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

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    if (_result.isVerified()) {
      sb.append("\nVerified");
    }
    if (_result.getStats() != null) {
      sb.append("\n\n").append(_result.getStats().prettyPrint());
    }
    return sb + _flowHistory.prettyPrint();
  }
}
