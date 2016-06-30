package org.batfish.answerer;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.BgpAdvertisementsAnswerElement;
import org.batfish.datamodel.questions.BgpAdvertisementsQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class BgpAdvertisementsAnswerer extends Answerer {

   public BgpAdvertisementsAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      BgpAdvertisementsQuestion question = (BgpAdvertisementsQuestion) _question;
      
      Pattern nodeRegex;

      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      _batfish.checkDataPlaneQuestionDependencies();
      Map<String, Configuration> configurations = _batfish.loadConfigurations(testrigSettings);
      _batfish.initBgpAdvertisements(configurations);
      
      BgpAdvertisementsAnswerElement answerElement = new BgpAdvertisementsAnswerElement(
            configurations, nodeRegex, question.getEbgp(), question.getIbgp(),
            question.getPrefixSpace(), question.getReceived(),
            question.getSent());

      return answerElement;
   }

}
