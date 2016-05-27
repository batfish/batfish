package org.batfish.question;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.UniqueIpAssignmentsAnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.UniqueIpAssignmentsQuestion;
import org.batfish.main.Batfish;

public class UniqueIpAssignmentsAnswer extends Answer {

   public UniqueIpAssignmentsAnswer(Batfish batfish,
         UniqueIpAssignmentsQuestion question) {

      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      UniqueIpAssignmentsAnswerElement answerElement = new UniqueIpAssignmentsAnswerElement();
      addAnswerElement(answerElement);
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      MultiSet<Ip> allIps = new TreeMultiSet<Ip>();
      MultiSet<Ip> enabledIps = new TreeMultiSet<Ip>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Configuration c = e.getValue();
         for (Interface iface : c.getInterfaces().values()) {
            for (Prefix prefix : iface.getAllPrefixes()) {
               Ip ip = prefix.getAddress();
               allIps.add(ip);
               if (iface.getActive()) {
                  enabledIps.add(ip);
               }

            }
         }
      }
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Configuration c = e.getValue();
         for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
            String interfaceName = e2.getKey();
            Interface iface = e2.getValue();
            for (Prefix prefix : iface.getAllPrefixes()) {
               Ip ip = prefix.getAddress();
               if (allIps.count(ip) != 1) {
                  answerElement.add(answerElement.getAllIps(), ip, hostname,
                        interfaceName);
               }
               if (iface.getActive()) {
                  if (enabledIps.count(ip) != 1) {
                     answerElement.add(answerElement.getEnabledIps(), ip,
                           hostname, interfaceName);
                  }
               }

            }
         }
      }
   }
}
