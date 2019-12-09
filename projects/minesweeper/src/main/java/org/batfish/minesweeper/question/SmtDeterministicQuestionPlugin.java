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
public class SmtDeterministicQuestionPlugin extends QuestionPlugin {

  public static class DeterministicAnswerer extends Answerer {

    public DeterministicAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer(NetworkSnapshot snapshot) {
      DeterministicQuestion q = (DeterministicQuestion) _question;
      PropertyChecker p = new PropertyChecker(new BDDPacket(), _batfish);
      return p.checkDeterminism(snapshot, q);
    }
  }

  public static class DeterministicQuestion extends HeaderQuestion {

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-deterministic";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new DeterministicAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new DeterministicQuestion();
  }
}
