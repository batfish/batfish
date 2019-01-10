package org.batfish.question.vxlanproperties;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Exposes {@link VxlanVniPropertiesQuestion}. */
@AutoService(Plugin.class)
public final class VxlanVniPropertiesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    checkArgument(
        question instanceof VxlanVniPropertiesQuestion,
        "Unsupported question type %s",
        question.getClass());
    return new VxlanVniPropertiesAnswerer((VxlanVniPropertiesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new VxlanVniPropertiesQuestion(null, null);
  }
}
