package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class SleepQuestionPlugin extends QuestionPlugin {

  public static class SleepAnswerer extends Answerer {

    public SleepAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      SleepQuestion question = (SleepQuestion) _question;
      try {
        Thread.sleep(question.getDuration());
      } catch (InterruptedException e) {
        throw new BatfishException("Sleep interrupted", e);
      }
      return new StringAnswerElement("Sleep completed! I feel rested!");
    }
  }

  /** Since this is not really a question; we do not document it as such. */
  public static class SleepQuestion extends Question {

    private static final String PROP_DURATION = "duration";

    private long _durationMs;

    public SleepQuestion() {
      _durationMs = 0;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(PROP_DURATION)
    public long getDuration() {
      return _durationMs;
    }

    @Override
    public String getName() {
      return "sleep";
    }

    @JsonProperty(PROP_DURATION)
    public void setDuration(long duration) {
      _durationMs = duration;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new SleepAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new SleepQuestion();
  }
}
