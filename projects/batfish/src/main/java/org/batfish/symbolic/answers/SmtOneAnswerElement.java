package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.symbolic.smt.VerificationResult;

public class SmtOneAnswerElement extends AnswerElement {

  private static final String PROP_RESULT = "result";

  private VerificationResult _result;

  @JsonCreator
  public SmtOneAnswerElement(@JsonProperty(PROP_RESULT) VerificationResult result) {
    _result = result;
  }

  @JsonProperty(PROP_RESULT)
  public VerificationResult getResult() {
    return _result;
  }

  @Override
  public String prettyPrint() {
    return _result.prettyPrint(null);
  }
}
