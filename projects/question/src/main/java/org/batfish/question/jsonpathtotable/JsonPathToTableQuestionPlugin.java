package org.batfish.question.jsonpathtotable;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class JsonPathToTableQuestionPlugin extends QuestionPlugin {
  @Override
  protected JsonPathToTableAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new JsonPathToTableAnswerer(question, batfish);
  }

  @Override
  protected JsonPathToTableQuestion createQuestion() {
    return new JsonPathToTableQuestion(null, null, null);
  }
}
