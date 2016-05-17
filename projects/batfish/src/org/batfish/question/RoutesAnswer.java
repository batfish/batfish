package org.batfish.question;

import java.util.Map;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.RoutesAnswerElement;
import org.batfish.datamodel.questions.RoutesQuestion;
import org.batfish.main.Batfish;

public class RoutesAnswer extends Answer {

   public RoutesAnswer(Batfish batfish, RoutesQuestion question) {
      setQuestion(question);
      batfish.checkDataPlaneQuestionDependencies();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initRoutes(configurations);
      setStatus(AnswerStatus.SUCCESS);
      RoutesAnswerElement answerElement = new RoutesAnswerElement(
            configurations);
      addAnswerElement(answerElement);
   }

}
