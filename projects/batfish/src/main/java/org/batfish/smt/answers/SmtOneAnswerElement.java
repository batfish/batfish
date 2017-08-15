package org.batfish.smt.answers;

import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.smt.VerificationResult;

public class SmtOneAnswerElement implements AnswerElement {

  protected VerificationResult _result;

  public VerificationResult getResult() {
    return _result;
  }

  public void setResult(VerificationResult result) {
    this._result = result;
  }

  @Override
  public String prettyPrint() {
    return _result.prettyPrint(null);
  }
}
