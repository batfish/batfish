package org.batfish.question.routes;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Returns information about dataplane routes. */
@AutoService(Plugin.class)
public class RoutesQuestionPlugin extends QuestionPlugin {

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new RoutesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new RoutesQuestion();
  }
}
