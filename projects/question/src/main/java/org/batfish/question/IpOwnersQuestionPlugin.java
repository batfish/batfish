package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Map;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.IpOwnersAnswerElement;
import org.batfish.datamodel.questions.Question;

/** Return the output of {@link CommonUtil#computeIpOwners(Map, boolean)} as an answer */
@AutoService(Plugin.class)
public class IpOwnersQuestionPlugin extends QuestionPlugin {

  private static class IpOwnersAnswerer extends Answerer {

    IpOwnersAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      IpOwnersQuestion question = (IpOwnersQuestion) _question;
      return new IpOwnersAnswerElement(
          CommonUtil.computeIpOwners(_batfish.loadConfigurations(), question.isExcludeInactive()));
    }
  }

  public static class IpOwnersQuestion extends Question {
    private static final String NAME = "ipowners";

    private static final String PROP_EXCLUDE_INACTIVE = "excludeInactive";

    /** Whether to exclude inactive interfaces from the computation */
    private boolean _excludeInactive;

    public IpOwnersQuestion() {}

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return NAME;
    }

    @JsonProperty(PROP_EXCLUDE_INACTIVE)
    public boolean isExcludeInactive() {
      return _excludeInactive;
    }

    @JsonProperty(PROP_EXCLUDE_INACTIVE)
    public void setExcludeInactive(boolean excludeInactive) {
      this._excludeInactive = excludeInactive;
    }
  }

  @Override
  protected IpOwnersAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new IpOwnersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new IpOwnersQuestion();
  }
}
