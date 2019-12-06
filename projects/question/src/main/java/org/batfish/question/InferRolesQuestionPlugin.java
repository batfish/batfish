package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.role.InferRoles;
import org.batfish.role.RoleMapping;

@AutoService(Plugin.class)
public class InferRolesQuestionPlugin extends QuestionPlugin {

  public static class InferRolesAnswerElement extends AnswerElement {
    private static final String PROP_ROLE_MAPPING = "roleMapping";

    @Nonnull private final Optional<RoleMapping> _roleMapping;

    @JsonCreator
    public InferRolesAnswerElement(
        @JsonProperty(PROP_ROLE_MAPPING) Optional<RoleMapping> roleMapping) {
      _roleMapping = roleMapping;
    }

    @JsonProperty(PROP_ROLE_MAPPING)
    public Optional<RoleMapping> getRoleDimensions() {
      return _roleMapping;
    }
  }

  public static class InferRolesAnswerer extends Answerer {

    public InferRolesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public InferRolesAnswerElement answer(NetworkSnapshot snapshot) {

      InferRolesQuestion question = (InferRolesQuestion) _question;

      // collect relevant nodes in a list.
      Set<String> nodes = question.getNodeRegex().getMatchingNodes(_batfish, snapshot);

      Optional<RoleMapping> roleMapping =
          new InferRoles(nodes, _batfish.getTopologyProvider().getInitialLayer3Topology(snapshot))
              .inferRoles();
      return new InferRolesAnswerElement(roleMapping);
    }
  }

  /**
   * Infer a regex that identifies a role from a node name.
   *
   * <p>Uses heuristics to identify a part of a node's name that represents its role.
   */
  public static final class InferRolesQuestion extends Question {
    private static final String PROP_NODE_REGEX = "nodeRegex";

    private NodesSpecifier _nodeRegex;

    public InferRolesQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "inferroles";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier regex) {
      _nodeRegex = regex;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new InferRolesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new InferRolesQuestion();
  }
}
