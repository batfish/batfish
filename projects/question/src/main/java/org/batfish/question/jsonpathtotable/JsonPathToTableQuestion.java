package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BfConsts;
import org.batfish.datamodel.questions.Question;

public class JsonPathToTableQuestion extends Question {

  private static final String PROP_DEBUG = "debug";

  private static final String PROP_PATH_QUERY = "pathQuery";

  private boolean _debug;

  private Question _innerQuestion;

  private JsonPathToTableQuery _pathQuery;

  @JsonCreator
  public JsonPathToTableQuestion(
      @JsonProperty(BfConsts.PROP_INNER_QUESTION) Question innerQuestion,
      @JsonProperty(PROP_PATH_QUERY) JsonPathToTableQuery pathQuery,
      @JsonProperty(PROP_DEBUG) Boolean debug) {
    _innerQuestion = innerQuestion;
    _pathQuery = pathQuery;
    _debug = debug == null ? false : debug.booleanValue();
  }

  @Override
  public boolean getDataPlane() {
    return _innerQuestion.getDataPlane();
  }

  @JsonProperty(PROP_DEBUG)
  public boolean getDebug() {
    return _debug;
  }

  @JsonProperty(BfConsts.PROP_INNER_QUESTION)
  public Question getInnerQuestion() {
    return _innerQuestion;
  }

  @Override
  public String getName() {
    return "jsonpathtotable";
  }

  @JsonProperty(PROP_PATH_QUERY)
  public JsonPathToTableQuery getPathQuery() {
    return _pathQuery;
  }

  @Override
  public String prettyPrint() {
    String retString =
        String.format(
            "%s %s%s=\"%s\" %s=\"%s\"",
            getName(),
            prettyPrintBase(),
            PROP_PATH_QUERY,
            _pathQuery,
            BfConsts.PROP_INNER_QUESTION,
            _innerQuestion.prettyPrint());
    return retString;
  }
}
