package org.batfish.question;

import java.io.File;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.UndefinedReferencesAnswerElement;
import org.batfish.datamodel.questions.UndefinedReferencesQuestion;
import org.batfish.main.Batfish;
import org.batfish.representation.VendorConfiguration;

public class UndefinedReferencesAnswer extends Answer {

   public UndefinedReferencesAnswer(Batfish batfish,
         UndefinedReferencesQuestion question) {

      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      batfish.checkConfigurations();
      UndefinedReferencesAnswerElement answerElement = new UndefinedReferencesAnswerElement();
      addAnswerElement(answerElement);
      ConvertConfigurationAnswerElement ccae = (ConvertConfigurationAnswerElement) batfish
            .deserializeObject(new File(batfish.getSettings()
                  .getConvertAnswerPath()));
      for (Entry<String, Warnings> e : ccae.getWarnings().entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Warnings warnings = e.getValue();
         for (Warning warning : warnings.getRedFlagWarnings()) {
            String tag = warning.getTag();
            String text = warning.getText();
            if (tag.equals(VendorConfiguration.UNDEFINED)) {
               answerElement.add(hostname, text);
            }
         }
      }
   }
}
