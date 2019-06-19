package org.batfish.question.switchedvlanproperties;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public final class SwitchedVlanPropertiesQuestionPlugin extends QuestionPlugin {

  @Override
  protected SwitchedVlanPropertiesAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new SwitchedVlanPropertiesAnswerer(question, batfish);
  }

  @Override
  protected SwitchedVlanPropertiesQuestion createQuestion() {
    return new SwitchedVlanPropertiesQuestion();
  }
}
