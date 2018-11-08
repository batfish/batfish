package org.batfish.question.loop;

import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for {@link DetectLoopsQuestion}. */
@AutoService(Plugin.class)
public class DetectLoopsPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new DetectLoopsAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new DetectLoopsQuestion(DEFAULT_MAX_TRACES);
  }
}
