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
public class SmtLoadBalanceQuestionPlugin extends QuestionPlugin {

  public static class LoadBalanceAnswerer extends Answerer {

    public LoadBalanceAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      LoadBalanceQuestion q = (LoadBalanceQuestion) _question;
      return _batfish.smtLoadBalance(q, q.getThreshold());
    }
  }

  public static class LoadBalanceQuestion extends HeaderLocationQuestion {

    private static final String THRESHOLD_VAR = "threshold";

    private int _threshold;

    public LoadBalanceQuestion() {
      _threshold = 0;
    }

    @JsonProperty(THRESHOLD_VAR)
    public int getThreshold() {
      return _threshold;
    }

    @JsonProperty(THRESHOLD_VAR)
    public void setThreshold(int i) {
      this._threshold = i;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-load-balance";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new LoadBalanceAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new LoadBalanceQuestion();
  }
}
