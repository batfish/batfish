package org.batfish.question.traceroute;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answering {@link BidirectionalTracerouteQuestion}. */
@AutoService(Plugin.class)
public class BidirectionalTraceroutePlugin extends QuestionPlugin {

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new BidirectionalTracerouteAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new BidirectionalTracerouteQuestion();
  }
}
