package org.batfish.question;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.RoutesAnswerElement;
import org.batfish.datamodel.questions.RoutesQuestion;
import org.batfish.main.Batfish;

public class RoutesAnswer extends Answer {

   public RoutesAnswer(Batfish batfish, RoutesQuestion question) {

      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      batfish.checkDataPlaneQuestionDependencies();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initRoutes(configurations);
      RoutesAnswerElement answerElement = new RoutesAnswerElement(
            configurations, nodeRegex);
      addAnswerElement(answerElement);
   }

}
