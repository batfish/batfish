package org.batfish.question.reducedreachability;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answer {@link ReducedReachabilityQuestion}. */
@AutoService(Plugin.class)
public class ReducedReachabilityPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new ReducedReachabilityAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new ReducedReachabilityQuestion();
  }
}
