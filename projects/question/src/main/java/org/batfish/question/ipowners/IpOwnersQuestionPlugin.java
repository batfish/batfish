package org.batfish.question.ipowners;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Return information about which IP/Node/Interface owns which IP in the network. */
@AutoService(Plugin.class)
public class IpOwnersQuestionPlugin extends QuestionPlugin {

  @Override
  protected IpOwnersAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new IpOwnersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new IpOwnersQuestion(false);
  }
}
