package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.ainterpreter.DomainType;

@AutoService(Plugin.class)
public class AiRoutesQuestionPlugin extends QuestionPlugin {

  public static class AiRoutesAnswerer extends Answerer {

    public AiRoutesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      AiRoutesQuestion q = (AiRoutesQuestion) _question;

      NodesSpecifier ns = new NodesSpecifier(q.getNodeRegex());
      return _batfish.aiRoutes(ns, q.getDomainType());
    }
  }

  public static class AiRoutesQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private static final String PROP_DOMAIN_TYPE = "domainType";

    private String _nodeRegex = ".*";

    private DomainType _domainType = DomainType.REACHABILITY;

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "ai-routes";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public String getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(String x) {
      _nodeRegex = x;
    }

    @JsonProperty(PROP_DOMAIN_TYPE)
    public DomainType getDomainType() {
      return _domainType;
    }

    @JsonProperty(PROP_DOMAIN_TYPE)
    public void setDomainType(DomainType x) {
      this._domainType = x;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new AiRoutesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AiRoutesQuestion();
  }
}
