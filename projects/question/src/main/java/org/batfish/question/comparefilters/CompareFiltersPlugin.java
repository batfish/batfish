package org.batfish.question.comparefilters;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for {@link CompareFiltersQuestion}. */
@AutoService(Plugin.class)
public final class CompareFiltersPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new CompareFiltersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new CompareFiltersQuestion();
  }
}
