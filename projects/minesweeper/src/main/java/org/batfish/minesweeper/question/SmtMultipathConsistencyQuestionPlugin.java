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
public class SmtMultipathConsistencyQuestionPlugin extends QuestionPlugin {

  public static class MulipathConsistencyAnswerer extends Answerer {

    public MulipathConsistencyAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer(NetworkSnapshot snapshot) {
      MultipathConsistencyQuestion q = (MultipathConsistencyQuestion) _question;
      PropertyChecker p = new PropertyChecker(new BDDPacket(), _batfish);
      return p.checkMultipathConsistency(snapshot, q);
    }
  }

  public static class MultipathConsistencyQuestion extends HeaderLocationQuestion {

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-multipath-consistency";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new MulipathConsistencyAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new MultipathConsistencyQuestion();
  }
}
