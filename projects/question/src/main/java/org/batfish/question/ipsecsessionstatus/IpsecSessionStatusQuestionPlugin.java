package org.batfish.question.ipsecsessionstatus;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Return status of all IPSec sessions in the network */
@AutoService(Plugin.class)
public class IpsecSessionStatusQuestionPlugin extends QuestionPlugin {

  @Override
  protected IpsecSessionStatusAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new IpsecSessionStatusAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new IpsecSessionStatusQuestion(null, null, null);
  }
}
