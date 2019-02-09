package org.batfish.question.initialization;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Exposes {@link InitIssuesQuestion}. */
@AutoService(Plugin.class)
public class InitIssuesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    checkArgument(
        question instanceof InitIssuesQuestion,
        "Unsupported question type %s",
        question.getClass());
    return new InitIssuesAnswerer((InitIssuesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new InitIssuesQuestion();
  }
}
