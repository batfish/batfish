package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.datamodel.questions.Question;

public class JsonPathToTableQuestion extends Question {

  private static final String PROP_DEBUG = "debug";

  private static final String PROP_PATH_QUERY = "pathQuery";

  private static final Pattern VARIABLE_MATCHER = Pattern.compile("\\$\\{([^\\}]+)\\}");

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
    _debug = debug != null && debug;

    // names in text description should correspond to those of entities or extraction vars
    if (_displayHints != null && _displayHints.getTextDesc() != null) {
      Set<String> namesInTextDesc = new HashSet<>();
      Matcher matcher = VARIABLE_MATCHER.matcher(_displayHints.getTextDesc());
      while (matcher.find()) {
        namesInTextDesc.add(matcher.group(1));
      }
      SetView<String> missingVars =
          Sets.difference(
              namesInTextDesc,
              Sets.union(
                  _pathQuery.getCompositions().keySet(), pathQuery.getExtractions().keySet()));
      if (!missingVars.isEmpty()) {
        throw new BatfishException(
            "textDesc has names that are neither entities nor extractions: " + missingVars);
      }
    }
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
