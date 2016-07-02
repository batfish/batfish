package org.batfish.answerer;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.RoutesAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.RoutesQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class RoutesAnswerer extends Answerer {

   public RoutesAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      RoutesQuestion question = (RoutesQuestion) _question;
      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);
      _batfish.initRoutes(configurations);
      RoutesAnswerElement answerElement = new RoutesAnswerElement(
            configurations, nodeRegex);
      return answerElement;
   }

}
