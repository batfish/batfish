package org.batfish.question.filtertable;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/**
 * Plugin for the {@link FilterTableQuestion}. Takes in a table answer and filters it based on
 * specified criteria.
 */
@AutoService(Plugin.class)
public class FilterTableQuestionPlugin extends QuestionPlugin {

  @Override
  protected FilterTableAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new FilterTableAnswerer(question, batfish);
  }

  @Override
  protected FilterTableQuestion createQuestion() {
    return new FilterTableQuestion();
  }
}
