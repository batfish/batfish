package org.batfish.question.prefixtracer;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answering {@link PrefixTracerQuestion}. */
@AutoService(Plugin.class)
public class PrefixTracerPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new PrefixTracerAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new PrefixTracerQuestion();
  }
}
