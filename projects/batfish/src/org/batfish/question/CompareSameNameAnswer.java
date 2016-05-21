package org.batfish.question;

import java.util.List;
import java.util.Map;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.CompareSameNameAnswerElement;
import org.batfish.datamodel.questions.CompareSameNameQuestion;
import org.batfish.main.Batfish;
import org.batfish.util.Util;

public class CompareSameNameAnswer extends Answer {

   public CompareSameNameAnswer(Batfish batfish,
         CompareSameNameQuestion question) {
      
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();

      // collect relevant nodes in a list.
      List<String> nodes = Util.getMatchingStrings(question.getNodeRegex(), configurations.keySet());
      
      // Add 
      CompareSameNameAnswerElement<AsPathAccessList> asPathAccessListAnswerElement = new CompareSameNameAnswerElement<AsPathAccessList>();
      addAnswerElement(asPathAccessListAnswerElement);
      
      for (String node : nodes) {     
         // Process AsAccessPathList structures.
         Map<String, AsPathAccessList> asPathAccessLists =  configurations.get(node).getAsPathAccessLists();
            for (String asPathAccessListName : asPathAccessLists.keySet()) {
               asPathAccessListAnswerElement.add(node, asPathAccessListName, asPathAccessLists.get(asPathAccessListName));
         }
        
      }
      
   }   
}

