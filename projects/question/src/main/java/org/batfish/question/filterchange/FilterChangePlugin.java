package org.batfish.question.filterchange;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class FilterChangePlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new FilterChangeAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new FilterChangeQuestion();
  }
}
