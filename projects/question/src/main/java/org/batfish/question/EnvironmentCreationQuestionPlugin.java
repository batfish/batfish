package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.IEnvironmentCreationQuestion;
import org.batfish.datamodel.questions.Question;

public class EnvironmentCreationQuestionPlugin extends QuestionPlugin {

  public static class EnvironmentCreationAnswerer extends Answerer {

    public EnvironmentCreationAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      EnvironmentCreationQuestion question = (EnvironmentCreationQuestion) _question;
      // TODO: add flag to question determining whether or not to compute
      // data
      // plane
      boolean dp = false;
      return _batfish.createEnvironment(
          question.getEnvironmentName(),
          question.getNodeBlacklist(),
          question.getInterfaceBlacklist(),
          question.getEdgeBlacklist(),
          dp);
    }
  }

  /** Since this is not really a question; we do not document it as such. */
  public static class EnvironmentCreationQuestion extends Question
      implements IEnvironmentCreationQuestion {

    private static final String PROP_EDGE_BLACKLIST = "edgeBlacklist";

    private static final String PROP_ENVIRONMENT_NAME = ENVIRONMENT_NAME_KEY;

    private static final String PROP_INTERFACE_BLACKLIST = "interfaceBlacklist";

    private static final String PROP_NODE_BLACKLIST = "nodeBlacklist";

    private SortedSet<Edge> _edgeBlacklist;

    private String _environmentName;

    private SortedSet<NodeInterfacePair> _interfaceBlacklist;

    private SortedSet<String> _nodeBlacklist;

    @JsonCreator
    public EnvironmentCreationQuestion() {
      _nodeBlacklist = new NodeSet();
      _interfaceBlacklist = new TreeSet<>();
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    @JsonIgnore
    public boolean getDifferential() {
      return false;
    }

    @JsonProperty(PROP_EDGE_BLACKLIST)
    public SortedSet<Edge> getEdgeBlacklist() {
      return _edgeBlacklist;
    }

    @JsonProperty(PROP_ENVIRONMENT_NAME)
    public String getEnvironmentName() {
      return _environmentName;
    }

    @JsonProperty(PROP_INTERFACE_BLACKLIST)
    public SortedSet<NodeInterfacePair> getInterfaceBlacklist() {
      return _interfaceBlacklist;
    }

    @Override
    public String getName() {
      return NAME;
    }

    @JsonProperty(PROP_NODE_BLACKLIST)
    public SortedSet<String> getNodeBlacklist() {
      return _nodeBlacklist;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @JsonProperty(PROP_EDGE_BLACKLIST)
    public void setEdgeBlacklist(SortedSet<Edge> edgeBlacklist) {
      _edgeBlacklist = edgeBlacklist;
    }

    @JsonProperty(PROP_ENVIRONMENT_NAME)
    public void setEnvironmentName(String environmentName) {
      _environmentName = environmentName;
    }

    @JsonProperty(PROP_INTERFACE_BLACKLIST)
    public void setInterfaceBlacklist(SortedSet<NodeInterfacePair> blacklist) {
      _interfaceBlacklist = blacklist;
    }

    @JsonProperty(PROP_NODE_BLACKLIST)
    public void setNodeBlacklist(NodeSet blacklist) {
      _nodeBlacklist = blacklist;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new EnvironmentCreationAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new EnvironmentCreationQuestion();
  }
}
