package org.batfish.question.initialization;

import org.batfish.datamodel.questions.Question;

/** A question that returns a table with the list of initialization warnings and errors. */
public class InitIssuesQuestion extends Question {
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "initIssues";
  }

  InitIssuesQuestion() {} // package-private constructor
}
