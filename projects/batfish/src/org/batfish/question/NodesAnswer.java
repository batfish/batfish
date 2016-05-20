package org.batfish.question;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.NodesAnswerElement;
import org.batfish.datamodel.questions.NodesQuestion;
import org.batfish.main.Batfish;

public class NodesAnswer extends Answer {

   public NodesAnswer(Batfish batfish, NodesQuestion question) {
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
    		  if (!nodeRegex.matcher(node).matches())
    			  continue;
    		  nodes.addAll(configurations.keySet());
    	  }
      }
      Map<String, Configuration> answerNodes = new TreeMap<String, Configuration>();
      answerNodes.putAll(configurations);
      answerNodes.keySet().retainAll(nodes);

      _answerElements.add(new NodesAnswerElement(answerNodes, question
            .getSummary()));
   }

}
