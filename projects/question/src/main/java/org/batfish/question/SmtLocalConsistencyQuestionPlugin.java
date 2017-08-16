package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderQuestion;


public class SmtLocalConsistencyQuestionPlugin extends QuestionPlugin {

  public static class LocalConsistencyAnswerer extends Answerer {

    public LocalConsistencyAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      LocalConsistencyQuestion q = (LocalConsistencyQuestion) _question;

      Pattern routerRegex;

      try {
        routerRegex = Pattern.compile(q.getRouterRegex());
      } catch (PatternSyntaxException e) {
        throw new BatfishException(
            String.format(
                "One of the supplied regexes %s is not a valid java regex.", q.getRouterRegex()),
            e);
      }

      return _batfish.smtLocalConsistency(routerRegex, q.getStrict(), q.getFullModel());
    }
  }

  public static class LocalConsistencyQuestion extends HeaderQuestion {

    private static final String NODE_REGEX_VAR = "nodeRegex";

    private static final String STRICT_VAR = "strict";

    private String _routerRegex;

    private boolean _strict;

    public LocalConsistencyQuestion() {
      _routerRegex = ".*";
      _strict = false;
    }

    @JsonProperty(NODE_REGEX_VAR)
    public String getRouterRegex() {
      return _routerRegex;
    }

    @JsonProperty(STRICT_VAR)
    public boolean getStrict() {
      return _strict;
    }

    @JsonProperty(NODE_REGEX_VAR)
    public void setRouterRegex(String routerRegex) {
      this._routerRegex = routerRegex;
    }

    @JsonProperty(STRICT_VAR)
    public void setStrict(boolean strict) {
      this._strict = strict;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-local-consistency";
    }

    @Override
    public boolean getTraffic() {
      return false;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new LocalConsistencyAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new LocalConsistencyQuestion();
  }
}
