package org.batfish.question.specifiers;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;
import org.batfish.question.specifiers.SpecifiersQuestion.QueryType;

@AutoService(Plugin.class)
public final class SpecifiersQuestionPlugin extends QuestionPlugin {

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new SpecifiersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    // no reason for this to be the default but we must instantiate with something
    return new SpecifiersQuestion(QueryType.IP_SPACE);
  }
}
