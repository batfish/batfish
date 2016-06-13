package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.PairwiseVpnConnectivityAnswerElement;
import org.batfish.datamodel.questions.PairwiseVpnConnectivityQuestion;
import org.batfish.main.Batfish;

public class PairwiseVpnConnectivityAnswer extends Answer {

   public PairwiseVpnConnectivityAnswer(Batfish batfish,
         PairwiseVpnConnectivityQuestion question) {
      Pattern node1Regex;
      Pattern node2Regex;

      try {
         node1Regex = Pattern.compile(question.getNode1Regex());
         node2Regex = Pattern.compile(question.getNode2Regex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               String.format(
                     "One of the supplied regexes (%s  OR  %s) is not a valid java regex.",
                     question.getNode1Regex(), question.getNode2Regex()), e);
      }

      PairwiseVpnConnectivityAnswerElement answerElement = new PairwiseVpnConnectivityAnswerElement();
      addAnswerElement(answerElement);
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initRemoteIpsecVpns(configurations);
      Set<String> ipsecVpnNodes = answerElement.getIpsecVpnNodes();
      Set<String> node2RegexNodes = new HashSet<String>();

      for (Configuration c : configurations.values()) {
         String hostname = c.getHostname();
         if (!c.getIpsecVpns().isEmpty()) {
            if (node1Regex.matcher(hostname).matches()) {
               ipsecVpnNodes.add(c.getHostname());
            }
            if (node2Regex.matcher(hostname).matches()) {
               node2RegexNodes.add(hostname);
            }
         }
      }
      for (Configuration c : configurations.values()) {
         String hostname = c.getHostname();
         if (!ipsecVpnNodes.contains(hostname)) {
            continue;
         }
         SortedSet<String> currentNeighbors = new TreeSet<String>();
         if (!c.getIpsecVpns().isEmpty()) {
            for (IpsecVpn ipsecVpn : c.getIpsecVpns().values()) {
               if (ipsecVpn.getRemoteIpsecVpn() != null) {
                  for (IpsecVpn remoteIpsecVpn : ipsecVpn
                        .getCandidateRemoteIpsecVpns()) {
                     String remoteHost = remoteIpsecVpn.getOwner()
                           .getHostname();
                     if (node2RegexNodes.contains(remoteHost)) {
                        currentNeighbors.add(remoteHost);
                     }
                  }
               }
            }
            SortedSet<String> missingNeighbors = new TreeSet<String>();
            missingNeighbors.addAll(node2RegexNodes);
            missingNeighbors.removeAll(currentNeighbors);
            missingNeighbors.remove(hostname);
            answerElement.getConnectedNeighbors().put(hostname,
                  currentNeighbors);
            answerElement.getMissingNeighbors().put(hostname, missingNeighbors);
         }
      }
   }

}
