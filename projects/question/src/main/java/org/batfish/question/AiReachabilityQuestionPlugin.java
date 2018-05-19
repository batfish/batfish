package org.batfish.question;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;

@AutoService(Plugin.class)
public class AiReachabilityQuestionPlugin extends QuestionPlugin {

  public static class AiReachabilityAnswerer extends Answerer {

    public AiReachabilityAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      HeaderLocationQuestion q = (HeaderLocationQuestion) _question;
      return _batfish.aiReachability(q);
    }
  }

  public static class AiReachabililtyQuestion extends HeaderLocationQuestion {

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "ai-reachability";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new AiReachabilityAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AiReachabililtyQuestion();
  }
}
