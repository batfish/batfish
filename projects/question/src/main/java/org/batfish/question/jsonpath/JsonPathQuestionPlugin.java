package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;
import org.batfish.question.jsonpath.JsonPathResult.JsonPathResultEntry;

public class JsonPathQuestionPlugin extends QuestionPlugin {

  public static class JsonPathAnswerElement implements AnswerElement {

    private static final String PROP_RESULTS = "results";

    static String prettyPrint(SortedMap<Integer, JsonPathResult> results) {
      StringBuilder sb = new StringBuilder("Results for JsonPath\n");
      for (Integer index : results.keySet()) {
        JsonPathResult result = results.get(index);
        sb.append(
            String.format(
                "  [%d]: %d results for %s\n",
                index, result.getNumResults(), result.getPath().toString()));
        if (result.getAssertionResult() != null) {
          sb.append(String.format("    Assertion : %s\n", result.getAssertionResult()));
        }
        for (JsonPathResultEntry resultEntry : result.getResult().values()) {
          ConcreteJsonPath path = resultEntry.getConcretePath();
          JsonNode suffix = resultEntry.getSuffix();
          String pathString = path.toString();
          if (suffix != null) {
            sb.append(String.format("    %s : %s\n", pathString, suffix.toString()));
          } else {
            sb.append(String.format("    %s\n", pathString));
          }
        }
      }
      return sb.toString();
    }

    private SortedMap<Integer, JsonPathResult> _results;

    public JsonPathAnswerElement() {
      _results = new TreeMap<>();
    }

    @JsonProperty(PROP_RESULTS)
    public SortedMap<Integer, JsonPathResult> getResults() {
      return _results;
    }

    @Override
    public String prettyPrint() {
      return prettyPrint(_results);
    }

    @JsonProperty(PROP_RESULTS)
    public void setResults(SortedMap<Integer, JsonPathResult> results) {
      _results = results;
    }
  }

  public static class JsonPathAnswerer extends Answerer {

    public JsonPathAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public JsonPathAnswerElement answer() {

      Configuration.setDefaults(BatfishJsonPathDefaults.INSTANCE);
      ConfigurationBuilder b = new ConfigurationBuilder();
      final Configuration c = b.build();

      JsonPathQuestion question = (JsonPathQuestion) _question;
      List<JsonPathQuery> paths = question.getPaths();

      Question innerQuestion = question._innerQuestion;
      String innerQuestionName = innerQuestion.getName();
      Answerer innerAnswerer =
          _batfish.getAnswererCreators().get(innerQuestionName).apply(innerQuestion, _batfish);
      AnswerElement innerAnswer = innerAnswerer.answer();

      BatfishObjectMapper mapper = new BatfishObjectMapper();
      String innerAnswerStr = null;
      try {
        innerAnswerStr = mapper.writeValueAsString(innerAnswer);
      } catch (IOException e) {
        throw new BatfishException("Could not get JSON string from inner answer", e);
      }
      Object jsonObject = JsonPath.parse(innerAnswerStr, c).json();
      Map<Integer, JsonPathResult> results = new ConcurrentHashMap<>();
      List<Integer> indices = new ArrayList<>();
      for (int i = 0; i < paths.size(); i++) {
        indices.add(i);
      }
      AtomicInteger completed = _batfish.newBatch("JsonPath queries", indices.size());
      indices
          .parallelStream()
          .forEach(
              i -> {
                JsonPathQuery nodesPath = paths.get(i);
                String path = nodesPath.getPath();

                ConfigurationBuilder prefixCb = new ConfigurationBuilder();
                prefixCb.options(Option.ALWAYS_RETURN_LIST);
                prefixCb.options(Option.AS_PATH_LIST);
                Configuration prefixC = prefixCb.build();

                ConfigurationBuilder suffixCb = new ConfigurationBuilder();
                suffixCb.options(Option.ALWAYS_RETURN_LIST);
                Configuration suffixC = suffixCb.build();

                ArrayNode prefixes = null;
                ArrayNode suffixes = null;
                JsonPath jsonPath;
                try {
                  jsonPath = JsonPath.compile(path);
                } catch (InvalidPathException e) {
                  throw new BatfishException("Invalid JsonPath: " + path, e);
                }

                try {
                  prefixes = jsonPath.read(jsonObject, prefixC);
                  suffixes = jsonPath.read(jsonObject, suffixC);
                } catch (PathNotFoundException e) {
                  suffixes = JsonNodeFactory.instance.arrayNode();
                  prefixes = JsonNodeFactory.instance.arrayNode();
                } catch (Exception e) {
                  throw new BatfishException("Error reading JSON path: " + path, e);
                }
                int numResults = prefixes.size();
                JsonPathResult nodePathResult = new JsonPathResult();
                nodePathResult.setPath(nodesPath);
                nodePathResult.setNumResults(numResults);
                boolean includeSuffix = nodesPath.getSuffix();
                if (nodesPath.hasValidAssertion()) {
                  boolean assertion = nodesPath.getAssertion().evaluate(suffixes);
                  nodePathResult.setAssertionResult(assertion);
                } else if (!nodesPath.getSummary()) {
                  SortedMap<String, JsonPathResultEntry> result = new TreeMap<>();
                  Iterator<JsonNode> p = prefixes.iterator();
                  Iterator<JsonNode> s = suffixes.iterator();
                  while (p.hasNext()) {
                    JsonNode prefix = p.next();
                    JsonNode suffix = includeSuffix ? s.next() : null;
                    String prefixStr = prefix.textValue();
                    if (prefixStr == null) {
                      throw new BatfishException("Did not expect null value");
                    }
                    ConcreteJsonPath concretePath = new ConcreteJsonPath(prefixStr);
                    result.put(
                        concretePath.toString(), new JsonPathResultEntry(concretePath, suffix));
                  }
                  nodePathResult.setResult(result);
                }
                results.put(i, nodePathResult);
                completed.incrementAndGet();
              });
      JsonPathAnswerElement answerElement = new JsonPathAnswerElement();
      answerElement.getResults().putAll(results);

      return answerElement;
    }

    @Override
    public JsonPathDiffAnswerElement answerDiff() {
      _batfish.pushBaseEnvironment();
      _batfish.checkEnvironmentExists();
      _batfish.popEnvironment();
      _batfish.pushDeltaEnvironment();
      _batfish.checkEnvironmentExists();
      _batfish.popEnvironment();
      _batfish.pushBaseEnvironment();
      JsonPathAnswerer beforeAnswerer = (JsonPathAnswerer) create(_question, _batfish);
      JsonPathAnswerElement before = beforeAnswerer.answer();
      _batfish.popEnvironment();
      _batfish.pushDeltaEnvironment();
      JsonPathAnswerer afterAnswerer = (JsonPathAnswerer) create(_question, _batfish);
      JsonPathAnswerElement after = afterAnswerer.answer();
      _batfish.popEnvironment();
      return new JsonPathDiffAnswerElement(before, after);
    }
  }

  public static class JsonPathDiffAnswerElement implements AnswerElement {

    static String prettyPrint(SortedMap<Integer, JsonPathDiffResult> results) {
      StringBuilder sb = new StringBuilder();
      results.forEach(
          (index, diff) -> {
            SortedMap<String, JsonPathResultEntry> added = diff.getAdded();
            SortedMap<String, JsonPathResultEntry> removed = diff.getRemoved();
            sb.append(
                String.format(
                    "  [%d]: %d added and %d removed for %s\n",
                    index, added.size(), removed.size(), diff.getPath().toString()));
            SortedSet<String> allKeys =
                CommonUtil.union(added.keySet(), removed.keySet(), TreeSet::new);
            for (String key : allKeys) {
              if (removed.containsKey(key)) {
                JsonNode removedNode = removed.get(key).getSuffix();
                if (removedNode != null) {
                  sb.append(String.format("-   %s : %s\n", key.toString(), removedNode.toString()));
                } else {
                  sb.append(String.format("-   %s\n", key.toString()));
                }
              }
              if (added.containsKey(key)) {
                JsonNode addedNode = added.get(key).getSuffix();
                if (addedNode != null) {
                  sb.append(String.format("+   %s : %s\n", key.toString(), addedNode.toString()));
                } else {
                  sb.append(String.format("+   %s\n", key.toString()));
                }
              }
            }
          });
      String result = sb.toString();
      return result;
    }

    private SortedMap<Integer, JsonPathDiffResult> _results;

    @JsonCreator
    public JsonPathDiffAnswerElement() {}

    public JsonPathDiffAnswerElement(JsonPathAnswerElement before, JsonPathAnswerElement after) {
      _results = new TreeMap<>();
      for (Integer index : before._results.keySet()) {
        JsonPathResult nprBefore = before._results.get(index);
        JsonPathResult nprAfter = after._results.get(index);
        JsonPathDiffResult diff = new JsonPathDiffResult(nprBefore, nprAfter);
        _results.put(index, diff);
      }
    }

    public SortedMap<Integer, JsonPathDiffResult> getResults() {
      return _results;
    }

    @Override
    public String prettyPrint() {
      return prettyPrint(_results);
    }

    public void setResults(SortedMap<Integer, JsonPathDiffResult> results) {
      _results = results;
    }
  }

  // <question_page_comment>

  /**
   * Runs JsonPath <a href=https://github.com/jayway/JsonPath></a> queries on the JSON data model
   * that is the output of the 'Nodes' question.
   *
   * <p>This query can be used to perform server-side queries for the presence or absence of
   * specified patterns in the data model induced by the configurations supplied in the test-rig.
   *
   * @type JsonPath onefile
   * @param paths A JSON list of path queries, each of which is a JSON object containing the
   *     remaining documented fields (path, suffix, summary). For each specified path query, the
   *     question returns a list of paths in the data model matching the criteria of the query.
   * @hparam path (Property of each element of 'paths') The JsonPath query to execute.
   * @hparam suffix (Property of each element of 'paths') Defaults to false. If true, then each path
   *     in the returned list will map to the remaining content of the datamodel at the end of that
   *     path. This can be useful for debugging, but can also be very verbose. If false, then each
   *     path will map to a null value.
   * @hparam summary (Property of each element of 'paths') Defaults to false. If true, then instead
   *     of outputting each matching path, only the count of matching paths will be output.
   * @example bf_answer("NodesPath",
   *     paths=[{"path":"$.nodes[*].interfaces[*][?(@.mtu!=1500)].mtu"}]) Return all interfaces with
   *     MTUs not equal to 1500
   */
  public static class JsonPathQuestion extends Question {

    private static final String PROP_PATHS = "paths";

    private Question _innerQuestion;

    private List<JsonPathQuery> _paths;

    public JsonPathQuestion() {
      _paths = Collections.emptyList();
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(BfConsts.PROP_INNER_QUESTION)
    public Question getInnerQuestion() {
      return _innerQuestion;
    }

    @Override
    public String getName() {
      return "jsonpath";
    }

    @JsonProperty(PROP_PATHS)
    public List<JsonPathQuery> getPaths() {
      return _paths;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format(
              "%s %s%s=\"%s\" %s=\"%s\"",
              getName(),
              prettyPrintBase(),
              PROP_PATHS,
              _paths,
              BfConsts.PROP_INNER_QUESTION,
              _innerQuestion.prettyPrint());
      return retString;
    }

    @JsonProperty(BfConsts.PROP_INNER_QUESTION)
    public void setInnerQuestion(Question innerQuestion) {
      _innerQuestion = innerQuestion;
    }

    @JsonProperty(PROP_PATHS)
    public void setPaths(List<JsonPathQuery> paths) {
      _paths = paths;
    }
  }

  @Override
  protected JsonPathAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new JsonPathAnswerer(question, batfish);
  }

  @Override
  protected JsonPathQuestion createQuestion() {
    return new JsonPathQuestion();
  }
}
