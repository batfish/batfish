package org.batfish.question.initialization;

import org.batfish.datamodel.questions.Question;

/** A question that returns a table with the parse warnings for each file. */
public final class ParseWarningQuestion extends Question {
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "parsewarning";
  }

  ParseWarningQuestion() {} // package-private constructor
}
