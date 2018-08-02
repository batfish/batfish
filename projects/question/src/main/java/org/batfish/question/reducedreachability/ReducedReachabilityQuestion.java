package org.batfish.question.reducedreachability;

import org.batfish.datamodel.questions.Question;

/** A zero-input question to check for reduced reachability between base and delta snapshots. */
public final class ReducedReachabilityQuestion extends Question {
  ReducedReachabilityQuestion() {
    setDifferential(true);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "reducedreachability";
  }
}
