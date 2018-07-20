package org.batfish.question.ipsecpeers;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Return status of all IPSec peers in the network */
@AutoService(Plugin.class)
public class IpsecPeersQuestionPlugin extends QuestionPlugin {

  @Override
  protected IpsecPeersAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new IpsecPeersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new IpsecPeersQuestion(null, null, null);
  }
}
