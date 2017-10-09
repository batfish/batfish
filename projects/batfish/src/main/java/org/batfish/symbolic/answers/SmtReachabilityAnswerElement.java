package org.batfish.symbolic.answers;

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
    }
    return sb + _flowHistory.prettyPrint();
  }
}
