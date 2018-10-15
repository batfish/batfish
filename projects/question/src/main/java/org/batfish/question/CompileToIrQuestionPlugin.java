package org.batfish.question;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class CompileToIrQuestionPlugin extends QuestionPlugin {

  public static class CompileToIrAnswerer extends Answerer {

    public CompileToIrAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      // CompileToIrQuestion question = (CompileToIrQuestion) _question;
      return this._batfish.compileToIr();
    }
  }

  public static class CompileToIrQuestion extends Question {

    public CompileToIrQuestion() {}

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "compile";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new CompileToIrAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new CompileToIrQuestion();
  }
}
