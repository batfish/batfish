package org.batfish.question;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class ErrorQuestionPlugin extends QuestionPlugin {

  public static class ErrorAnswerer extends Answerer {

    public ErrorAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer(NetworkSnapshot snapshot) {
      throw new BatfishException(
          "error question debugging outer exception",
          new BatfishException("error question debugging inner exception"));
    }
  }

  /** Since this is not really a question; we do not document it as such. */
  public static class ErrorQuestion extends Question {

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "error";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new ErrorAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new ErrorQuestion();
  }
}
