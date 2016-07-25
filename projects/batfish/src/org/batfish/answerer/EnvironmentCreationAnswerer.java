package org.batfish.answerer;

import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.EnvironmentCreationQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class EnvironmentCreationAnswerer extends Answerer {

   public EnvironmentCreationAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      EnvironmentCreationQuestion question = (EnvironmentCreationQuestion) _question;
      // TODO: add flag to question determining whether or not to compute data
      // plane
      boolean dp = false;
      return _batfish.createEnvironment(_batfish.getTestrigSettings(),
            question, dp);
   }

}
