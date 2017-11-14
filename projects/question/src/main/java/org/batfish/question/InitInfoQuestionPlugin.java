package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.InitInfoComponent;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class InitInfoQuestionPlugin extends QuestionPlugin {

  public static class InitInfoAnswerer extends Answerer {

    public InitInfoAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public InitInfoAnswerElement answer() {
      InitInfoQuestion question = (InitInfoQuestion) _question;
      InitInfoAnswerElement answerElement;
      if (question._components.contains(InitInfoComponent.CONFIGS)) {
        answerElement = _batfish.initInfo(question._summary, question._verboseError);
      } else {
        answerElement = new InitInfoAnswerElement();
      }
      question
          ._components
          .stream()
          .filter(c -> c != InitInfoComponent.CONFIGS)
          .forEach(
              initInfoComponent ->
                  answerElement
                      .getExtraComponents()
                      .put(
                          initInfoComponent,
                          _batfish.initInfoExtraComponent(
                              question._summary, question._verboseError, initInfoComponent)));
      return answerElement;
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

    private static final String PROP_COMPONENTS = "components";

    private static final String PROP_SUMMARY = "summary";

    private static final String PROP_VERBOSE_ERROR = "verboseError";

    private SortedSet<InitInfoComponent> _components;

    private boolean _summary;

    private boolean _verboseError;

    public InitInfoQuestion() {
      _components = new TreeSet<>(Collections.singleton(InitInfoComponent.CONFIGS));
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(PROP_COMPONENTS)
    public SortedSet<InitInfoComponent> getComponents() {
      return _components;
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

    @JsonProperty(PROP_COMPONENTS)
    public void setComponents(SortedSet<InitInfoComponent> components) {
      _components = components;
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
