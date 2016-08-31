package org.batfish.plugin;

import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.main.Batfish;

public class NullTask extends TaskPlugin {

   public NullTask(Batfish batfish) {
      super(batfish);
   }

   @Override
   public Answer run() {
      Answer answer = new Answer();
      answer.addAnswerElement(new StringAnswerElement("Null task"));
      return answer;
   }

}
