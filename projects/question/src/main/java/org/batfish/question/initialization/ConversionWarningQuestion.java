package org.batfish.question.initialization;

import org.batfish.datamodel.questions.Question;

/**
 * A question that returns a table with warnings generated while converting to vendor independent
 * configuration.
 */
public final class ConversionWarningQuestion extends Question {
  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "conversionWarning";
  }

  ConversionWarningQuestion() {} // package-private constructor
}
