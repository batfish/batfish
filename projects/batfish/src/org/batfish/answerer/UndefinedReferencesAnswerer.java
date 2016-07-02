package org.batfish.answerer;

import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.UndefinedReferencesAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.UndefinedReferencesQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.representation.VendorConfiguration;

public class UndefinedReferencesAnswerer extends Answerer {

   public UndefinedReferencesAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      UndefinedReferencesQuestion question = (UndefinedReferencesQuestion) _question;

      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      _batfish.checkConfigurations(testrigSettings);
      UndefinedReferencesAnswerElement answerElement = new UndefinedReferencesAnswerElement();
      ConvertConfigurationAnswerElement ccae = (ConvertConfigurationAnswerElement) _batfish
            .deserializeObject(testrigSettings.getConvertAnswerPath());
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
      return answerElement;
   }
}
