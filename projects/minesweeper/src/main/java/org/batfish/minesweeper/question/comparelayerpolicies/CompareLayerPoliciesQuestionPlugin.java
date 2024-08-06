package org.batfish.minesweeper.question.comparelayerpolicies;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** QuestionPlugin for {@link CompareLayerPolicies}. */
@AutoService(Plugin.class)
public final class CompareLayerPoliciesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new CompareLayerPoliciesAnswerer((CompareLayerPolicies) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new CompareLayerPolicies();
  }
}
