package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.answers.AnswerElement;

public class CompressionAnswerElement implements AnswerElement {

  @JsonCreator
  public CompressionAnswerElement() {
  }

  @Override
  public String prettyPrint() {
    return "";
  }
}