package org.batfish.answerer;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NodesAnswerElement;
import org.batfish.datamodel.questions.NodesQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class NodesAnswerer extends Answerer {

   public NodesAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      NodesQuestion question = (NodesQuestion) _question;
      
      _batfish.checkConfigurations(testrigSettings);
      Map<String, Configuration> configurations = _batfish.loadConfigurations(testrigSettings);

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
            if (!nodeRegex.matcher(node).matches()) {
               continue;
            }
            nodes.addAll(configurations.keySet());
         }
      }
      SortedMap<String, Configuration> answerNodes = new TreeMap<String, Configuration>();
      answerNodes.putAll(configurations);
      answerNodes.keySet().retainAll(nodes);

      return new NodesAnswerElement(answerNodes, question
            .getSummary());
   }

}
