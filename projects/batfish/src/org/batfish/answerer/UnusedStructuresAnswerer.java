package org.batfish.answerer;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.UnusedStructuresAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.UnusedStructuresQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class UnusedStructuresAnswerer extends Answerer {

   public UnusedStructuresAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      UnusedStructuresQuestion question = (UnusedStructuresQuestion) _question;

      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"",
               e);
      }

      _batfish.checkConfigurations(testrigSettings);
      UnusedStructuresAnswerElement answerElement = new UnusedStructuresAnswerElement();
      ConvertConfigurationAnswerElement ccae = (ConvertConfigurationAnswerElement) _batfish
            .deserializeObject(testrigSettings.getConvertAnswerPath());
      for (Entry<String, SortedMap<String, SortedSet<String>>> e : ccae
            .getUnusedStructures().entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         SortedMap<String, SortedSet<String>> byType = e.getValue();
         answerElement.getUnusedStructures().put(hostname, byType);
      }
      return answerElement;
   }

}
