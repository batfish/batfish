package org.batfish.minesweeper.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.minesweeper.smt.PropertyChecker;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class SmtBoundedLengthQuestionPlugin extends QuestionPlugin {

  public static class BoundedLengthAnswerer extends Answerer {

    public BoundedLengthAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer(NetworkSnapshot snapshot) {
      BoundedLengthQuestion q = (BoundedLengthQuestion) _question;

      if (q.getBound() == null) {
        throw new BatfishException("Missing parameter length bound: (e.g., bound=3)");
      }
      PropertyChecker p = new PropertyChecker(new BDDPacket(), _batfish);
      return p.checkBoundedLength(snapshot, q, q.getBound());
    }
  }

  public static class BoundedLengthQuestion extends HeaderLocationQuestion {

    private static final String LENGTH_VAR = "bound";

    private Integer _bound;

    public BoundedLengthQuestion() {
      _bound = null;
    }

    @JsonProperty(LENGTH_VAR)
    public Integer getBound() {
      return _bound;
    }

    @JsonProperty(LENGTH_VAR)
    public void setBound(int i) {
      _bound = i;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-bounded-length";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new BoundedLengthAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new BoundedLengthQuestion();
  }
}
