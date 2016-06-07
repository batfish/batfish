package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.IpsecVpnCheckAnswerElement;
import org.batfish.datamodel.questions.IpsecVpnCheckQuestion;
import org.batfish.main.Batfish;

public class IpsecVpnCheckAnswer extends Answer {

   public IpsecVpnCheckAnswer(Batfish batfish, IpsecVpnCheckQuestion question) {
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initRemoteIpsecVpns(configurations);
      IpsecVpnCheckAnswerElement answerElement = new IpsecVpnCheckAnswerElement();
      Set<Ip> allInterfaceIps = new HashSet<Ip>();
      Set<Ip> loopbackIps = new HashSet<Ip>();
      for (Configuration c : configurations.values()) {
         for (Interface i : c.getInterfaces().values()) {
            if (i.getPrefix() != null) {
               for (Prefix prefix : i.getAllPrefixes()) {
                  if (i.isLoopback(c.getConfigurationFormat())) {
                     loopbackIps.add(prefix.getAddress());
                  }
                  allInterfaceIps.add(prefix.getAddress());
               }
            }
         }
      }
      for (Configuration c : configurations.values()) {
         for (IpsecVpn ipsecVpn : c.getIpsecVpns().values()) {
            if (ipsecVpn.getRemoteIpsecVpn() == null) {
               answerElement.addIpsecVpn(answerElement.getMissingEndpoint(), c,
                     ipsecVpn);
            }
            else {
               if (ipsecVpn.getCandidateRemoteIpsecVpns().size() != 1) {
                  for (IpsecVpn remoteIpsecVpn : ipsecVpn
                        .getCandidateRemoteIpsecVpns()) {
                     answerElement.addIpsecVpnPair(
                           answerElement.getNonUniqueEndpoint(), c, ipsecVpn,
                           remoteIpsecVpn);
                  }
               }
               IpsecVpn remoteIpsecVpn = ipsecVpn.getRemoteIpsecVpn();
               if (!ipsecVpn.compatibleIkeProposals(remoteIpsecVpn)) {
                  answerElement.addIpsecVpnPair(
                        answerElement.getIncompatibleIkeProposals(), c,
                        ipsecVpn, remoteIpsecVpn);
               }
               if (!ipsecVpn.compatibleIpsecProposals(remoteIpsecVpn)) {
                  answerElement.addIpsecVpnPair(
                        answerElement.getIncompatibleIpsecProposals(), c,
                        ipsecVpn, remoteIpsecVpn);
               }
               if (!ipsecVpn
                     .getGateway()
                     .getIkePolicy()
                     .getPreSharedKeyHash()
                     .equals(
                           remoteIpsecVpn.getGateway().getIkePolicy()
                                 .getPreSharedKeyHash())) {
                  answerElement.addIpsecVpnPair(
                        answerElement.getPreSharedKeyMismatch(), c, ipsecVpn,
                        remoteIpsecVpn);
               }
            }
         }
      }
      addAnswerElement(answerElement);
   }

}
