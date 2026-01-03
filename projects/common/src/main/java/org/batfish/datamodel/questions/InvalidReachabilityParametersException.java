package org.batfish.datamodel.questions;

import org.batfish.datamodel.answers.StringAnswerElement;

public class InvalidReachabilityParametersException extends Exception {

  private final String _invalidParametersMessage;

  public InvalidReachabilityParametersException(String msg) {
    super(msg);
    _invalidParametersMessage = msg;
  }

  public StringAnswerElement getInvalidParametersAnswer() {
    return new StringAnswerElement(_invalidParametersMessage);
  }
}
