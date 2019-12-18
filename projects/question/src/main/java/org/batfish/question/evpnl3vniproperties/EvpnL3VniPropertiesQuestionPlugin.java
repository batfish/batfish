package org.batfish.question.evpnl3vniproperties;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Exposes {@link EvpnL3VniPropertiesQuestion}. */
@AutoService(Plugin.class)
public final class EvpnL3VniPropertiesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    checkArgument(
        question instanceof EvpnL3VniPropertiesQuestion,
        "Unsupported question type %s",
        question.getClass());
    return new EvpnL3VniPropertiesAnswerer((EvpnL3VniPropertiesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new EvpnL3VniPropertiesQuestion(null);
  }
}
