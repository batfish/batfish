package org.batfish.question.searchfilters;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answer {@link SearchFiltersQuestion}. */
@AutoService(Plugin.class)
public class SearchFiltersPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new SearchFiltersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new SearchFiltersQuestion();
  }
}
