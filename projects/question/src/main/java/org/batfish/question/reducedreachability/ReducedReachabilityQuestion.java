package org.batfish.question.reducedreachability;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.questions.Question;

/** A zero-input question to check for reduced reachability between base and delta snapshots. */
public final class ReducedReachabilityQuestion extends Question {

  @JsonCreator
  public ReducedReachabilityQuestion() {
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
