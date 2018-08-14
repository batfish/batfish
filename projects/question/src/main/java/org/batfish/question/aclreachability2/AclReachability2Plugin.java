package org.batfish.question.aclreachability2;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class AclReachability2Plugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new AclReachability2Answerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AclReachability2Question();
  }
}
