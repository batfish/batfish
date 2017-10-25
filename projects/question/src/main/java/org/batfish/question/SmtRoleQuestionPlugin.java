package org.batfish.question;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.RoleQuestion;

public class SmtRoleQuestionPlugin extends QuestionPlugin {

  public static class RoleAnswerer extends Answerer {

    RoleAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      RoleQuestion q = (RoleQuestion) _question;
      return _batfish.smtRoles(q);
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
