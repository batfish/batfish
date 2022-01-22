package org.batfish.question.hsrpproperties;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Exposes {@link HsrpPropertiesQuestion}. */
@AutoService(Plugin.class)
public final class HsrpPropertiesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    checkArgument(
        question instanceof HsrpPropertiesQuestion,
        "Unsupported question type %s",
        question.getClass());
    return new HsrpPropertiesAnswerer((HsrpPropertiesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new HsrpPropertiesQuestion(null, null, null, false);
  }
}
