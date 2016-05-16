package org.batfish.question;

import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.questions.NeighborsQuestion;
import org.batfish.main.Batfish;

public class NeighborsAnswer extends Answer {

   public NeighborsAnswer(Batfish batfish, NeighborsQuestion question) {
      setQuestion(question);
      if (question.getNeighborTypes().isEmpty()) {
         question.getNeighborTypes().add(NeighborType.ANY);
      }
   }

}
