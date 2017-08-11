package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.questions.Question;

public class InitInfoQuestionPlugin extends QuestionPlugin {

  public static class InitInfoAnswerer extends Answerer {

    public InitInfoAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public InitInfoAnswerElement answer() {
      InitInfoQuestion question = (InitInfoQuestion) _question;
      return _batfish.initInfo(
          question._summary, question._verboseError, question._environmentRoutes);
    }
  }

  // <question_page_comment>

  /**
   * Outputs results of test-rig initialization.
   *
   * @type InitInfo onefile
   * @example bf_answer("initinfo", summary=True") Get summary information about test-rig
   *     initialization
   */
  public static class InitInfoQuestion extends Question {

    private static final String PROP_SUMMARY = "summary";

    private static final String PROP_VERBOSE_ERROR = "verboseError";

    private boolean _environmentRoutes;

    private boolean _summary;

    private boolean _verboseError;

    public InitInfoQuestion() {}

    @Override
    public boolean getDataPlane() {
      return false;
    }

    public boolean getEnvironmentRoutes() {
      return _environmentRoutes;
    }

    @Override
    public String getName() {
      return "initinfo";
    }

    @JsonProperty(PROP_SUMMARY)
    public boolean getSummary() {
      return _summary;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @JsonProperty(PROP_VERBOSE_ERROR)
    public boolean getVerboseError() {
      return _verboseError;
    }

    @Override
    public String prettyPrint() {
      return getName()
          + " "
          + PROP_SUMMARY
          + "="
          + _summary
          + " "
          + PROP_VERBOSE_ERROR
          + "="
          + _verboseError;
    }

    public void setEnvironmentRoutes(boolean environmentRoutes) {
      _environmentRoutes = environmentRoutes;
    }

    @JsonProperty(PROP_SUMMARY)
    public void setSummary(boolean summary) {
      _summary = summary;
    }

    @JsonProperty(PROP_VERBOSE_ERROR)
    public void setVerboseError(boolean verboseError) {
      _verboseError = verboseError;
    }
  }

  @Override
  protected InitInfoAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new InitInfoAnswerer(question, batfish);
  }

  @Override
  protected InitInfoQuestion createQuestion() {
    return new InitInfoQuestion();
  }
}
