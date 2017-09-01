package org.batfish.question;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

public class AbstractionQuestionPlugin extends QuestionPlugin {

  public static class AbstractionAnswerer extends Answerer {

    public AbstractionAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      return _batfish.abstraction();
    }
  }

  public static class AbstractionQuestion extends Question  {

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "abstraction";
    }

    @Override
    public boolean getTraffic() {
      return false;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new AbstractionQuestionPlugin.AbstractionAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AbstractionQuestionPlugin.AbstractionQuestion();
  }

}
