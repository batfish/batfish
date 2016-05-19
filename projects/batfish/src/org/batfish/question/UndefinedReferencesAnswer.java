package org.batfish.question;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.batfish.common.Pair;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.UndefinedReferencesAnswerElement;
import org.batfish.datamodel.answers.UniqueIpAssignmentsAnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.UndefinedReferencesQuestion;
import org.batfish.datamodel.questions.UniqueIpAssignmentsQuestion;
import org.batfish.main.Batfish;
import org.batfish.representation.VendorConfiguration;

public class UndefinedReferencesAnswer extends Answer {

   public UndefinedReferencesAnswer(Batfish batfish,
         UndefinedReferencesQuestion question) {
      batfish.checkConfigurations();
      UndefinedReferencesAnswerElement answerElement = new UndefinedReferencesAnswerElement();
      addAnswerElement(answerElement);
      ConvertConfigurationAnswerElement ccae = (ConvertConfigurationAnswerElement) batfish
            .deserializeObject(new File(batfish.getSettings()
                  .getConvertAnswerPath()));
      for (Entry<String, Warnings> e : ccae.getWarnings().entrySet()) {
         String hostname = e.getKey();
         Warnings warnings = e.getValue();
         for (Pair<String, String> warning : warnings.getRedFlagWarnings()) {
            String tag = warning.getSecond();
            String text = warning.getFirst();
            if (tag.equals(VendorConfiguration.UNDEFINED)) {
               answerElement.add(hostname, text);
            }
         }
      }
   }

}
