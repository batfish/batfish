package org.batfish.minesweeper.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.EquivalenceType;
import org.batfish.datamodel.questions.smt.RoleQuestion;
import org.batfish.minesweeper.abstraction.Roles;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class SmtRoleQuestionPlugin extends QuestionPlugin {

  public static class RoleAnswerer extends Answerer {

    public RoleAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer(NetworkSnapshot snapshot) {
      RoleQuestion q = (RoleQuestion) _question;
      Roles roles =
          Roles.create(snapshot, _batfish, q.getDstIps(), new NodesSpecifier(q.getNodeRegex()));
      return roles.asAnswer(q.getType());
    }
  }

  public static class OldRoleQuestion extends Question {
    private static final String PROP_NODE_REGEX = "nodeRegex";
    private static final String PROP_EQUIVALENCE_TYPE = "equivType";

    private NodesSpecifier _nodeRegex = NodesSpecifier.ALL;

    private EquivalenceType _type = EquivalenceType.NODE;

    OldRoleQuestion() {
      _type = EquivalenceType.NODE;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_EQUIVALENCE_TYPE)
    public EquivalenceType getType() {
      return _type;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier x) {
      _nodeRegex = x;
    }

    @JsonProperty(PROP_EQUIVALENCE_TYPE)
    public void setType(EquivalenceType x) {
      _type = x;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-roles";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new RoleAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new RoleQuestion();
  }
}
