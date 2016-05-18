package org.batfish.question;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.CompareSameNameAnswerElement;
import org.batfish.datamodel.questions.CompareSameNameQuestion;
import org.batfish.main.Batfish;

public class CompareSameNameAnswer extends Answer {

   public CompareSameNameAnswer(Batfish batfish,
         CompareSameNameQuestion question) {
      CompareSameNameAnswerElement answerElement = new CompareSameNameAnswerElement();
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();

      // collect nodes nodes
      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      Set<String> nodes = new TreeSet<String>();
      if (nodeRegex != null) {
         for (String node : configurations.keySet()) {
            Matcher nodeMatcher = nodeRegex.matcher(node);
            if (nodeMatcher.matches()) {
               nodes.add(node);
            }
         }
      }
      else {
         nodes.addAll(configurations.keySet());
      }

      addAnswerElement(answerElement);
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated

   }

}
