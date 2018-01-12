package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.List;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;

@AutoService(Plugin.class)
public class SmtPathPreferenceQuestion extends QuestionPlugin {

  public static class PathPreferenceAnswerer extends Answerer {

    public PathPreferenceAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      PathPreferenceQuestion q = (PathPreferenceQuestion) _question;

      return _batfish.smtPathPreferences(q, q.getPathPrefs());
    }
  }

  public static class PathPreferenceQuestion extends HeaderLocationQuestion {

    private static final String PATHS_VAR = "prefs";

    private List<List<String>> _pathPrefs;

    public PathPreferenceQuestion() {
      _pathPrefs = null;
    }

    @JsonProperty(PATHS_VAR)
    public List<List<String>> getPathPrefs() {
      return _pathPrefs;
    }

    @JsonProperty(PATHS_VAR)
    public void setPathPrefs(List<List<String>> p) {
      this._pathPrefs = p;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-path-preference";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new PathPreferenceAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new PathPreferenceQuestion();
  }
}
