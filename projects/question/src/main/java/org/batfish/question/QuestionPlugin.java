package org.batfish.question;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IClient;
import org.batfish.common.plugin.IQuestionPlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;

public abstract class QuestionPlugin extends Plugin implements IQuestionPlugin {

  protected abstract Answerer createAnswerer(Question question, IBatfish batfish);

  protected abstract Question createQuestion();

  @Override
  protected final void pluginInitialize() {
    Question question = createQuestion();
    String questionName = question.getName();
    String questionClassName = question.getClass().getCanonicalName();
    switch (_pluginConsumer.getType()) {
      case BATFISH:
        {
          IBatfish batfish = (IBatfish) _pluginConsumer;
          batfish.registerAnswerer(questionName, questionClassName, this::createAnswerer);
          break;
        }
      case CLIENT:
        {
          IClient client = (IClient) _pluginConsumer;
          client.registerQuestion(questionName, this::createQuestion);
          break;
        }
      default:
        break;
    }
  }
}
