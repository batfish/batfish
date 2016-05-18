package org.batfish.question;

import java.util.Map;
import java.util.Map.Entry;

import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.UniqueBgpPrefixOriginationAnswerElement;
import org.batfish.datamodel.questions.UniqueBgpPrefixOriginationQuestion;
import org.batfish.main.Batfish;

public class UniqueBgpPrefixOriginationAnswer extends Answer {

   public UniqueBgpPrefixOriginationAnswer(Batfish batfish,
         UniqueBgpPrefixOriginationQuestion question) {
      UniqueBgpPrefixOriginationAnswerElement answerElement = new UniqueBgpPrefixOriginationAnswerElement();
      addAnswerElement(answerElement);
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initBgpOriginationSpaceExplicit(configurations);
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String node1 = e.getKey();
         Configuration c1 = e.getValue();
         BgpProcess proc1 = c1.getBgpProcess();
         if (proc1 != null) {
            PrefixSpace space1 = proc1.getOriginationSpace();
            answerElement.getPrefixSpaces().put(node1, space1);
            for (Entry<String, Configuration> e2 : configurations.entrySet()) {
               String node2 = e2.getKey();
               if (node1.equals(node2)) {
                  continue;
               }
               Configuration c2 = e2.getValue();
               BgpProcess proc2 = c2.getBgpProcess();
               if (proc2 != null) {
                  PrefixSpace space2 = proc2.getOriginationSpace();
                  if (space1.overlaps(space2)) {
                     PrefixSpace intersection = space1.intersection(space2);
                     answerElement.addIntersection(node1, node2, intersection);
                  }
               }
            }
         }
      }
   }

}
