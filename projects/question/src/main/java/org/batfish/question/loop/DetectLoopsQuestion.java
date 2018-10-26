package org.batfish.question.loop;

import org.batfish.datamodel.questions.Question;

/** A zero-input question to check for forwarding loops. */
public class DetectLoopsQuestion extends Question {
  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "detectLoops";
  }
}
