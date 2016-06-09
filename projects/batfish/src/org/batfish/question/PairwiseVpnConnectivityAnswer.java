package org.batfish.question;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.PairwiseVpnConnectivityAnswerElement;
import org.batfish.datamodel.questions.PairwiseVpnConnectivityQuestion;
import org.batfish.main.Batfish;

public class PairwiseVpnConnectivityAnswer extends Answer {

   public PairwiseVpnConnectivityAnswer(Batfish batfish,
         PairwiseVpnConnectivityQuestion question) {
      PairwiseVpnConnectivityAnswerElement answerElement = new PairwiseVpnConnectivityAnswerElement();
      addAnswerElement(answerElement);
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initRemoteIpsecVpns(configurations);
      Set<String> ipsecVpnNodes = answerElement.getIpsecVpnNodes();
      for (Configuration c : configurations.values()) {
         if (!c.getIpsecVpns().isEmpty()) {
            ipsecVpnNodes.add(c.getHostname());
         }
      }
      for (Configuration c : configurations.values()) {
         SortedSet<String> currentNeighbors = new TreeSet<String>();
         if (!c.getIpsecVpns().isEmpty()) {
            for (IpsecVpn ipsecVpn : c.getIpsecVpns().values()) {
               if (ipsecVpn.getRemoteIpsecVpn() != null) {
                  for (IpsecVpn remoteIpsecVpn : ipsecVpn
                        .getCandidateRemoteIpsecVpns()) {
                     String remoteHost = remoteIpsecVpn.getOwner()
                           .getHostname();
                     currentNeighbors.add(remoteHost);
                  }
               }
            }
            SortedSet<String> missingNeighbors = new TreeSet<String>();
            missingNeighbors.addAll(ipsecVpnNodes);
            missingNeighbors.removeAll(currentNeighbors);
            String hostname = c.getHostname();
            answerElement.getConnectedNeighbors().put(hostname,
                  currentNeighbors);
            answerElement.getMissingNeighbors().put(hostname, missingNeighbors);
         }
      }
   }

}
