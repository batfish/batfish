package org.batfish.question.referencedstructures;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for answer {@link ReferencedStructuresQuestion}. */
@AutoService(Plugin.class)
public class ReferencedStructuresQuestionPlugin extends QuestionPlugin {

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new ReferencedStructuresAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new ReferencedStructuresQuestion(".*", NodesSpecifier.ALL, ".*");
  }
}
