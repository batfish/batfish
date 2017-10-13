package org.batfish.smt.answers;

import java.util.SortedSet;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.smt.VerificationResult;

public class SmtNondeterminismAnswerElement implements AnswerElement {

  private VerificationResult _result;

  private SortedSet<String> _forwardingCase1;

  private SortedSet<String> _forwardingCase2;

  public VerificationResult getResult() {
    return _result;
  }

  public SortedSet<String> getForwardingCase1() {
    return _forwardingCase1;
  }

  public SortedSet<String> getForwardingCase2() {
    return _forwardingCase2;
  }

  public void setResult(VerificationResult x) {
    this._result = x;
  }

  public void setForwardingCase1(SortedSet<String> x) {
    this._forwardingCase1 = x;
  }

  public void setForwardingCase2(SortedSet<String> x) {
    this._forwardingCase2 = x;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    if (_result.isVerified()) {
      sb.append("\nVerified\n");
    } else {
      sb.append("Delta forwarding case 1:\n");
      for (String s : _forwardingCase1) {
        sb.append("   ").append(s).append("\n");
      }
      sb.append("\nDelta forwarding case 2:\n");
      for (String s : _forwardingCase2) {
        sb.append("   ").append(s).append("\n");
      }
    }
    return sb.toString();
  }
}
