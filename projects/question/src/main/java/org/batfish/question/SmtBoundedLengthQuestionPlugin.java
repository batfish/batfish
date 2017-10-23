package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;

@AutoService(Plugin.class)
public class SmtBoundedLengthQuestionPlugin extends QuestionPlugin {

  public static class BoundedLengthAnswerer extends Answerer {

    public BoundedLengthAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      BoundedLengthQuestion q = (BoundedLengthQuestion) _question;

      return _batfish.smtBoundedLength(q, q.getBound());
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
      this._bound = i;
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
