package org.batfish.question.traceroute;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answering {@link TracerouteQuestion}. */
@AutoService(Plugin.class)
public class TraceroutePlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new TracerouteAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new TracerouteQuestion();
  }
}
