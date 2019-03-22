package org.batfish.question.testroutepolicies;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** QuestionPlugin for {@link TestRoutePoliciesQuestion}. */
@AutoService(Plugin.class)
public final class TestRoutePoliciesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new TestRoutePoliciesAnswerer((TestRoutePoliciesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new TestRoutePoliciesQuestion();
  }
}
