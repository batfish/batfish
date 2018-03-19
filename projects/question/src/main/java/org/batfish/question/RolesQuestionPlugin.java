package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class RolesQuestionPlugin extends QuestionPlugin {

  public static class RolesAnswerElement extends AnswerElement {

    private static final String PROP_ROLE_SPECIFIER = "roleSpecifier";

    private static final String PROP_COMPLETE_ROLE_MAP = "completeRoleMap";

    private NodeRoleSpecifier _roleSpecifier;

    private SortedMap<String, SortedSet<String>> _completeRoleMap;

    public RolesAnswerElement() {
      _roleSpecifier = new NodeRoleSpecifier();
      _completeRoleMap = new TreeMap<>();
    }

    @JsonProperty(PROP_ROLE_SPECIFIER)
    public NodeRoleSpecifier getRoleSpecifier() {
      return _roleSpecifier;
    }

    @JsonProperty(PROP_COMPLETE_ROLE_MAP)
    public SortedMap<String, SortedSet<String>> getCompleteRoleMap() {
      return _completeRoleMap;
    }

    @Override
    public String prettyPrint() {

      StringBuilder sb;
      sb = new StringBuilder("Results for roles\n");

      sb.append(
          "The following role specifier was "
              + (_roleSpecifier.getInferred() ? "inferred" : "user-provided")
              + ":\n");

      sb.append("Role regexes: \n");
      for (String regex : _roleSpecifier.getRoleRegexes()) {
        sb.append("  " + regex + "\n");
      }

      sb.append("Role map: \n");
      for (Map.Entry<String, SortedSet<String>> entry : _roleSpecifier.getRoleMap().entrySet()) {
        sb.append("  " + entry + "\n");
      }

      sb.append("\n\n");

      sb.append("The complete map from roles to nodes:\n");
      for (Map.Entry<String, SortedSet<String>> entry : _completeRoleMap.entrySet()) {
        sb.append("   " + entry + "\n");
      }

      return sb.toString();
    }

    @JsonProperty(PROP_ROLE_SPECIFIER)
    public void setRoleSpecifier(NodeRoleSpecifier roleSpecifier) {
      _roleSpecifier = roleSpecifier;
    }

    @JsonProperty(PROP_COMPLETE_ROLE_MAP)
    public void setCompleteRoleMap(SortedMap<String, SortedSet<String>> completeRoleMap) {
      _completeRoleMap = completeRoleMap;
    }
  }

  public static class RolesAnswerer extends Answerer {

    public RolesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public RolesAnswerElement answer() {

      RolesQuestion question = (RolesQuestion) _question;
      RolesAnswerElement answerElement = new RolesAnswerElement();

      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      // collect relevant nodes in a list.
      Set<String> nodes = question.getNodeRegex().getMatchingNodes(configurations);

      NodeRoleSpecifier roleSpecifier = _batfish.getNodeRoleSpecifier(question.getInferred());
      answerElement.setRoleSpecifier(roleSpecifier);
      answerElement.setCompleteRoleMap(roleSpecifier.createRoleNodesMap(nodes));

      return answerElement;
    }
  }

  // <question_page_comment>
  /**
   * List the roles of each node.
   *
   * @type Roles multifile
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @param inferred Boolean indicating whether to show the roles that were automatically inferred,
   *     even if an explicit NodeRoleSpecifier was provided when the testrig was uploaded. Default
   *     value is false.
   */
  public static final class RolesQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private static final String PROP_INFERRED = "inferred";

    private NodesSpecifier _nodeRegex;

    private boolean _inferred;

    public RolesQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "roles";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_INFERRED)
    public boolean getInferred() {
      return _inferred;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier regex) {
      _nodeRegex = regex;
    }

    @JsonProperty(PROP_INFERRED)
    public void setInferred(boolean inferred) {
      _inferred = inferred;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new RolesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new RolesQuestion();
  }
}
