package org.batfish.question;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.BgpAdvertisementsAnswerElement;
import org.batfish.datamodel.questions.BgpAdvertisementsQuestion;
import org.batfish.main.Batfish;

public class BgpAdvertisementsAnswer extends Answer {

   public BgpAdvertisementsAnswer(Batfish batfish,
         BgpAdvertisementsQuestion question) {

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
      batfish.initBgpAdvertisements(configurations);
      BgpAdvertisementsAnswerElement answerElement = new BgpAdvertisementsAnswerElement(
            configurations, nodeRegex, question.getEbgp(), question.getIbgp(),
            question.getReceived(), question.getSent());
      addAnswerElement(answerElement);
   }

}
