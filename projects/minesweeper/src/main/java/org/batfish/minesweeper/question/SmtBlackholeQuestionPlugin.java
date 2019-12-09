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
public class SmtBlackholeQuestionPlugin extends QuestionPlugin {

  public static class BlackholeAnswerer extends Answerer {

    public BlackholeAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer(NetworkSnapshot snapshot) {
      BlackholeQuestion q = (BlackholeQuestion) _question;
      PropertyChecker p = new PropertyChecker(new BDDPacket(), _batfish);
      return p.checkBlackHole(snapshot, q);
    }
  }

  public static class BlackholeQuestion extends HeaderQuestion {

    public BlackholeQuestion() {}

    @Override
    public String getName() {
      return "smt-blackhole";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new BlackholeAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new BlackholeQuestion();
  }
}
