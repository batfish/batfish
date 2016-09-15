package org.batfish.question;

import org.batfish.answerer.Answerer;
import org.batfish.client.Client;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;

public abstract class QuestionPlugin extends Plugin {

   protected abstract Answerer createAnswerer(Question question,
         Batfish batfish);

   protected abstract Question createQuestion();

   protected abstract String getQuestionClassName();

   protected abstract String getQuestionName();

   @Override
   protected final void pluginInitialize() {
      switch (_pluginConsumer.getType()) {
      case BATFISH: {
         Batfish batfish = (Batfish) _pluginConsumer;
         batfish.registerAnswerer(getQuestionClassName(), this::createAnswerer);
         break;
      }
      case CLIENT: {
         Client client = (Client) _pluginConsumer;
         client.registerQuestion(getQuestionName(), this::createQuestion);
         break;
      }
      default:
         break;
      }
   }

}
