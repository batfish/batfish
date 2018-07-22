package org.batfish.question.reachfilter;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answer {@link ReachFilterQuestion}. */
@AutoService(Plugin.class)
public class ReachFilterPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new ReachFilterAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new ReachFilterQuestion();
  }
}
