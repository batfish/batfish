package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class RolesQuestionPlugin extends QuestionPlugin {

  public static class RolesAnswerElement implements AnswerElement {

    private static final String PROP_ROLE_SPECIFIER = "roleSpecifier";

    private static final String PROP_NODES = "nodes";

    private NodeRoleSpecifier _roleSpecifier;

    private List<String> _nodes;

    public RolesAnswerElement() {}

    @JsonProperty(PROP_ROLE_SPECIFIER)
    public NodeRoleSpecifier getRoleSpecifier() {
      return _roleSpecifier;
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

      SortedMap<String, SortedSet<String>> roleNodesMap =
          _roleSpecifier.createRoleNodesMap(new TreeSet<String>(_nodes));
      sb.append("The complete map from roles to nodes:\n");
      for (Map.Entry<String, SortedSet<String>> entry : roleNodesMap.entrySet()) {
        sb.append("   " + entry + "\n");
      }

      return sb.toString();
    }

    @JsonProperty(PROP_ROLE_SPECIFIER)
    public void setRoleSpecifier(NodeRoleSpecifier roleSpecifier) {
      _roleSpecifier = roleSpecifier;
    }

    @JsonProperty(PROP_NODES)
    public void setNodes(List<String> nodes) {
      _nodes = nodes;
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
      List<String> nodes =
          CommonUtil.getMatchingStrings(question.getNodeRegex(), configurations.keySet());

      answerElement.setNodes(nodes);

      NodeRoleSpecifier roleSpecifier = _batfish.getNodeRoleSpecifier(question.getInferred());
      answerElement.setRoleSpecifier(roleSpecifier);

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

    private String _nodeRegex;

    private boolean _inferred;

    public RolesQuestion() {
      _nodeRegex = ".*";
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
    public String getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_INFERRED)
    public boolean getInferred() {
      return _inferred;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(String regex) {
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
