package org.batfish.question.mlag;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for {@link org.batfish.question.mlag.MlagPropertiesQuestion} */
@AutoService(Plugin.class)
public final class MlagPropertiesQuestionPlugin extends QuestionPlugin {

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new MlagPropertiesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new MlagPropertiesQuestion();
  }
}
