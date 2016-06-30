package org.batfish.answerer;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class ErrorAnswerer extends Answerer {

   public ErrorAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      throw new BatfishException("error question debugging outer exception",
            new BatfishException("error question debugging inner exception"));
   }

}
