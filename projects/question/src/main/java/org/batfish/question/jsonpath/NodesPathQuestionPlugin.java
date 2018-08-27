package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.NodeType;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.NodesQuestionPlugin.NodesQuestion;
import org.batfish.question.QuestionPlugin;
import org.batfish.question.jsonpath.JsonPathQuestionPlugin.JsonPathAnswerElement;
import org.batfish.question.jsonpath.JsonPathQuestionPlugin.JsonPathAnswerer;
import org.batfish.question.jsonpath.JsonPathQuestionPlugin.JsonPathQuestion;

@AutoService(Plugin.class)
public class NodesPathQuestionPlugin extends QuestionPlugin {

  public static class NodesPathAnswerer extends Answerer {

    public NodesPathAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public JsonPathAnswerElement answer() {
      NodesPathQuestion question = (NodesPathQuestion) _question;
      NodesQuestion nq = new NodesQuestion();
      nq.setNodes(question.getNodeRegex());
      nq.setNodeTypes(question.getNodeTypes());
      nq.setSummary(false);
      JsonPathQuestion jq = new JsonPathQuestion();
      jq.setInnerQuestion(nq);
      jq.setPaths(question.getPaths());
      JsonPathAnswerer answerer = new JsonPathAnswerer(jq, _batfish);
      return answerer.answer();
    }

    @Override
    public AnswerElement answerDiff() {
      NodesPathQuestion question = (NodesPathQuestion) _question;
      NodesQuestion nq = new NodesQuestion();
      nq.setNodes(question.getNodeRegex());
      nq.setNodeTypes(question.getNodeTypes());
      nq.setSummary(false);
      JsonPathQuestion jq = new JsonPathQuestion();
      jq.setDifferential(true);
      jq.setInnerQuestion(nq);
      jq.setPaths(question.getPaths());
      JsonPathAnswerer answerer = new JsonPathAnswerer(jq, _batfish);
      return answerer.answerDiff();
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
   * @type NodesPath onefile
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
  public static class NodesPathQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private static final String PROP_NODE_TYPES = "nodeTypes";

    private static final String PROP_PATHS = "paths";

    private NodesSpecifier _nodeRegex;

    private SortedSet<NodeType> _nodeTypes;

    private List<JsonPathQuery> _paths;

    public NodesPathQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
      _nodeTypes = new TreeSet<>();
      _paths = Collections.emptyList();
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "nodespath";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_NODE_TYPES)
    public SortedSet<NodeType> getNodeTypes() {
      return _nodeTypes;
    }

    @JsonProperty(PROP_PATHS)
    public List<JsonPathQuery> getPaths() {
      return _paths;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format("%s %s%s=\"%s\"", getName(), prettyPrintBase(), PROP_PATHS, _paths);
      return retString;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier nodeRegex) {
      _nodeRegex = nodeRegex;
    }

    @JsonProperty(PROP_NODE_TYPES)
    public void setNodeTypes(SortedSet<NodeType> nodeTypes) {
      _nodeTypes = nodeTypes;
    }

    @JsonProperty(PROP_PATHS)
    public void setPaths(List<JsonPathQuery> paths) {
      _paths = paths;
    }
  }

  @Override
  protected NodesPathAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new NodesPathAnswerer(question, batfish);
  }

  @Override
  protected NodesPathQuestion createQuestion() {
    return new NodesPathQuestion();
  }
}
