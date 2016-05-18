package org.batfish.question;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.questions.ErrorQuestion;
import org.batfish.main.Batfish;

public class ErrorAnswer extends Answer {

   public ErrorAnswer(Batfish batfish, ErrorQuestion question) {
      throw new BatfishException("error question debugging outer exception",
            new BatfishException("error question debugging inner exception"));
   }

}
