package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.answers.SelfAdjacenciesAnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.SelfAdjacenciesQuestion;
import org.batfish.main.Batfish;

public class SelfAdjacenciesAnswer extends Answer {

   public SelfAdjacenciesAnswer(Batfish batfish,
         SelfAdjacenciesQuestion question) {
      setQuestion(question);
      setStatus(AnswerStatus.SUCCESS);
      SelfAdjacenciesAnswerElement answerElement = new SelfAdjacenciesAnswerElement();
      addAnswerElement(answerElement);
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         Configuration c = e.getValue();
         MultiSet<Prefix> nodePrefixes = new TreeMultiSet<Prefix>();
         for (Interface iface : c.getInterfaces().values()) {
            Set<Prefix> ifaceBasePrefixes = new HashSet<Prefix>();
            if (iface.getActive()) {
               for (Prefix prefix : iface.getAllPrefixes()) {
                  Prefix basePrefix = prefix.getNetworkPrefix();
                  if (!ifaceBasePrefixes.contains(basePrefix)) {
                     ifaceBasePrefixes.add(basePrefix);
                  }
                  nodePrefixes.add(basePrefix);
               }
            }
         }
         for (Interface iface : c.getInterfaces().values()) {
            for (Prefix prefix : iface.getAllPrefixes()) {
               Prefix basePrefix = prefix.getNetworkPrefix();
               if (nodePrefixes.count(basePrefix) > 1) {
                  Ip address = prefix.getAddress();
                  String interfaceName = iface.getName();
                  answerElement.add(hostname, basePrefix, interfaceName,
                        address);
               }
            }
         }
      }
   }

}
