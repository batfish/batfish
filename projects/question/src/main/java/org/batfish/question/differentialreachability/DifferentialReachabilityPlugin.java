package org.batfish.question.differentialreachability;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answer {@link DifferentialReachabilityQuestion}. */
@AutoService(Plugin.class)
public class DifferentialReachabilityPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new DifferentialReachabilityAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new DifferentialReachabilityQuestion();
  }
}
