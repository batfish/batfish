package org.batfish.smt.answers;

import org.batfish.datamodel.FlowHistory;

public class SmtReachabilityAnswerElement extends SmtOneAnswerElement {

  private FlowHistory _flowHistory;

  public FlowHistory getFlowHistory() {
    return _flowHistory;
  }

  public void setFlowHistory(FlowHistory flowHistory) {
    this._flowHistory = flowHistory;
  }

  @Override
  public String prettyPrint() {

    StringBuilder sb = new StringBuilder();
    if (_result.isVerified()) {
      sb.append("\nVerified");
    } else {
      sb.append(_result.prettyPrintEnv());
      sb.append(_result.prettyPrintFailures());
    }
    return sb + _flowHistory.prettyPrint();
  }
}
