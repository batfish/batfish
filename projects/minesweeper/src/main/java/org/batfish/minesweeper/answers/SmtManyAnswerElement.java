package org.batfish.minesweeper.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.minesweeper.smt.VerificationResult;

public class SmtManyAnswerElement extends AnswerElement {
  private static final String PROP_RESULT = "result";

  private SortedMap<String, VerificationResult> _result;

  @JsonCreator
  public SmtManyAnswerElement(
      @JsonProperty(PROP_RESULT) SortedMap<String, VerificationResult> result) {
    _result = result;
  }

  @JsonProperty(PROP_RESULT)
  public SortedMap<String, VerificationResult> getResult() {
    return _result;
  }
}
