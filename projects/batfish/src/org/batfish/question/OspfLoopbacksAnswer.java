package org.batfish.question;

import java.util.Map;
import java.util.Map.Entry;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.OspfLoopbacksAnswerElement;
import org.batfish.datamodel.questions.OspfLoopbacksQuestion;
import org.batfish.main.Batfish;

public class OspfLoopbacksAnswer extends Answer {

   public OspfLoopbacksAnswer(Batfish batfish, OspfLoopbacksQuestion question) {
      OspfLoopbacksAnswerElement answerElement = new OspfLoopbacksAnswerElement();
      addAnswerElement(answerElement);
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         Configuration c = e.getValue();
         for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
            String interfaceName = e2.getKey();
            Interface iface = e2.getValue();
            if (iface.isLoopback(c.getVendor())) {
               if (iface.getOspfEnabled()) {
                  answerElement.add(answerElement.getRunning(), hostname,
                        interfaceName);
                  if (iface.getOspfPassive()) {
                     answerElement.add(answerElement.getPassive(), hostname,
                           interfaceName);
                  }
                  else {
                     answerElement.add(answerElement.getActive(), hostname,
                           interfaceName);
                  }
               }
               else {
                  answerElement.add(answerElement.getInactive(), hostname,
                        interfaceName);
               }
            }
         }
      }
   }

}
