package org.batfish.minesweeper.question;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.minesweeper.smt.PropertyChecker;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class SmtEqualLengthQuestionPlugin extends QuestionPlugin {

  public static class EqualLengthAnswerer extends Answerer {

    public EqualLengthAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer(NetworkSnapshot snapshot) {
      PropertyChecker p = new PropertyChecker(new BDDPacket(), _batfish);
      return p.checkEqualLength(snapshot, (EqualLengthQuestion) _question);
    }
  }

  public static class EqualLengthQuestion extends HeaderLocationQuestion {

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-equal-length";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new EqualLengthAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new EqualLengthQuestion();
  }
}
