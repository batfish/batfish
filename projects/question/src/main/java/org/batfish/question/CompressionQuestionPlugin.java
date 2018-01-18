package org.batfish.question;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.IQuestion;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class CompressionQuestionPlugin extends QuestionPlugin {

  public static class CompressionAnswerer extends Answerer {

    public CompressionAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      CompressionQuestion q = (CompressionQuestion) _question;
      return _batfish.compression();
    }
  }

  public static class CompressionQuestion extends Question implements IQuestion {

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "compression";
    }

    @Override
    public boolean getTraffic() {
      return true;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new CompressionAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new CompressionQuestion();
  }
}
