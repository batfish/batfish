package org.batfish.datamodel.questions;

import org.batfish.datamodel.answers.StringAnswerElement;

public class InvalidReachabilitySettingsException extends Exception {

  /** */
  private static final long serialVersionUID = 1L;

  private StringAnswerElement _invalidSetingsAnswer;

  public InvalidReachabilitySettingsException(String msg) {
    super(msg);
    _invalidSetingsAnswer = new StringAnswerElement(msg);
  }

  public StringAnswerElement getInvalidSettingsAnswer() {
    return _invalidSetingsAnswer;
  }
}
