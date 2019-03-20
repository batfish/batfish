package org.batfish.question.testpolicies;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** QuestionPlugin for TestPoliciesQuestion. */
@AutoService(Plugin.class)
public final class TestPoliciesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new TestPoliciesAnswerer((TestPoliciesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new TestPoliciesQuestion();
  }
}
