package org.batfish.minesweeper.question.searchroutepolicies;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** QuestionPlugin for {@link SearchRoutePoliciesQuestion}. */
@AutoService(Plugin.class)
public final class SearchRoutePoliciesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new SearchRoutePoliciesAnswerer((SearchRoutePoliciesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new SearchRoutePoliciesQuestion();
  }
}
