package org.batfish.question;

import java.io.File;
import java.util.Map.Entry;

import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.UnusedStructuresAnswerElement;
import org.batfish.datamodel.questions.UnusedStructuresQuestion;
import org.batfish.main.Batfish;
import org.batfish.representation.VendorConfiguration;

public class UnusedStructuresAnswer extends Answer {

   public UnusedStructuresAnswer(Batfish batfish,
         UnusedStructuresQuestion question) {
      batfish.checkConfigurations();
      UnusedStructuresAnswerElement answerElement = new UnusedStructuresAnswerElement();
      addAnswerElement(answerElement);
      ConvertConfigurationAnswerElement ccae = (ConvertConfigurationAnswerElement) batfish
            .deserializeObject(new File(batfish.getSettings()
                  .getConvertAnswerPath()));
      for (Entry<String, Warnings> e : ccae.getWarnings().entrySet()) {
         String hostname = e.getKey();
         Warnings warnings = e.getValue();
         for (Warning warning : warnings.getRedFlagWarnings()) {
            String tag = warning.getTag();
            String text = warning.getText();
            if (tag.equals(VendorConfiguration.UNUSED)) {
               answerElement.add(hostname, text);
            }
         }
      }
   }

}
