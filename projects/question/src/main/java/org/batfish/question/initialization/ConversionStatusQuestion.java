package org.batfish.question.initialization;

import org.batfish.datamodel.questions.Question;

/** A question that returns a table with the list of nodes converted and their conversion status. */
public final class ConversionStatusQuestion extends Question {
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "conversionStatus";
  }

  ConversionStatusQuestion() {} // package-private constructor
}
