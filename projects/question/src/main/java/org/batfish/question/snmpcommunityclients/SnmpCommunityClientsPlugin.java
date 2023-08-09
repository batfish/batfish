package org.batfish.question.snmpcommunityclients;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** Plugin for {@link SnmpCommunityClientsQuestion}. */
@AutoService(Plugin.class)
public final class SnmpCommunityClientsPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    checkArgument(question instanceof SnmpCommunityClientsQuestion);
    return new SnmpCommunityClientsAnswerer((SnmpCommunityClientsQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new SnmpCommunityClientsQuestion();
  }
}
