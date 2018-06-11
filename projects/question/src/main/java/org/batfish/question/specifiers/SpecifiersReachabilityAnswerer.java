package org.batfish.question.specifiers;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

public final class SpecifiersReachabilityAnswerer extends Answerer {

  public SpecifiersReachabilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    SpecifiersReachabilityQuestion question = (SpecifiersReachabilityQuestion) _question;
    return _batfish.standard(question.getReachabilityParameters());
  }
}
