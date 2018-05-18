package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class AIRoutesQuestionPlugin extends QuestionPlugin {

  public static class AIRoutesAnswerer extends Answerer {

    public AIRoutesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      AIRoutesQuestion q = (AIRoutesQuestion) _question;
      NodesSpecifier ns = new NodesSpecifier(q.getNodeRegex());
      return _batfish.aiRoutes(ns);
    }
  }

  public static class AIRoutesQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private String _nodeRegex;

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
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new AIRoutesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AIRoutesQuestion();
  }
}
