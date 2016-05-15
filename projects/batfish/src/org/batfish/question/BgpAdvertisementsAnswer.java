package org.batfish.question;

import java.util.Map;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.BgpAdvertisementsAnswerElement;
import org.batfish.datamodel.questions.BgpAdvertisementsQuestion;
import org.batfish.main.Batfish;

public class BgpAdvertisementsAnswer extends Answer {

   public BgpAdvertisementsAnswer(Batfish batfish,
         BgpAdvertisementsQuestion question) {
      setQuestion(question);
      batfish.checkDataPlaneQuestionDependencies();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initBgpAdvertisements(configurations);
      setStatus(AnswerStatus.SUCCESS);
      BgpAdvertisementsAnswerElement answerElement = new BgpAdvertisementsAnswerElement(
            configurations, question.getEbgp(), question.getIbgp(),
            question.getReceived(), question.getSent());
      addAnswerElement(answerElement);
   }

}
