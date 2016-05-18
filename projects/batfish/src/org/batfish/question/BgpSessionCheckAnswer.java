package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.BgpSessionCheckAnswerElement;
import org.batfish.datamodel.questions.BgpSessionCheckQuestion;
import org.batfish.main.Batfish;

public class BgpSessionCheckAnswer extends Answer {

   public BgpSessionCheckAnswer(Batfish batfish,
         BgpSessionCheckQuestion question) {
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initRemoteBgpNeighbors(configurations);
      BgpSessionCheckAnswerElement answerElement = new BgpSessionCheckAnswerElement();
      Set<Ip> allInterfaceIps = new HashSet<Ip>();
      Set<Ip> loopbackIps = new HashSet<Ip>();
      for (Configuration c : configurations.values()) {
         for (Interface i : c.getInterfaces().values()) {
            if (i.getPrefix() != null) {
               for (Prefix prefix : i.getAllPrefixes()) {
                  if (i.isLoopback(c.getVendor())) {
                     loopbackIps.add(prefix.getAddress());
                  }
                  allInterfaceIps.add(prefix.getAddress());
               }
            }
         }
      }
      for (Configuration c : configurations.values()) {
         if (c.getBgpProcess() != null) {
            for (BgpNeighbor bgpNeighbor : c.getBgpProcess().getNeighbors()
                  .values()) {
               answerElement.add(answerElement.getAllBgpNeighbors(), c,
                     bgpNeighbor);
               boolean foreign = bgpNeighbor.getGroupName() != null
                     && question.getForeignBgpGroups().contains(
                           bgpNeighbor.getGroupName());
               boolean ebgp = bgpNeighbor.getRemoteAs() != bgpNeighbor
                     .getLocalAs();
               boolean ebgpMultihop = bgpNeighbor.getEbgpMultihop();
               Ip localIp = bgpNeighbor.getLocalIp();
               Ip remoteIp = bgpNeighbor.getAddress();
               if (bgpNeighbor.getPrefix().getPrefixLength() != 32) {
                  continue;
               }
               if (ebgp) {
                  if (!ebgpMultihop && loopbackIps.contains(localIp)) {
                     answerElement.add(
                           answerElement.getEbgpLocalIpOnLoopback(), c,
                           bgpNeighbor);
                  }
                  if (localIp == null) {
                     answerElement.add(answerElement.getBroken(), c,
                           bgpNeighbor);
                     answerElement.add(answerElement.getMissingLocalIp(), c,
                           bgpNeighbor);
                     answerElement.add(answerElement.getEbgpBroken(), c,
                           bgpNeighbor);
                     answerElement.add(answerElement.getEbgpMissingLocalIp(),
                           c, bgpNeighbor);
                  }
               }
               else {
                  // ibgp
                  if (!loopbackIps.contains(localIp)) {
                     answerElement.add(
                           answerElement.getIbgpLocalIpOnNonLoopback(), c,
                           bgpNeighbor);
                  }
                  if (localIp == null) {
                     answerElement.add(answerElement.getBroken(), c,
                           bgpNeighbor);
                     answerElement.add(answerElement.getMissingLocalIp(), c,
                           bgpNeighbor);
                     answerElement.add(answerElement.getIbgpBroken(), c,
                           bgpNeighbor);
                     answerElement.add(answerElement.getIbgpMissingLocalIp(),
                           c, bgpNeighbor);
                  }
               }
               if (foreign) {
                  answerElement.add(answerElement.getIgnoredForeignEndpoints(),
                        c, bgpNeighbor);
               }
               else {
                  // not foreign
                  if (ebgp) {
                     if (localIp != null && !allInterfaceIps.contains(localIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighbor);
                        answerElement.add(answerElement.getLocalIpUnknown(), c,
                              bgpNeighbor);
                        answerElement.add(answerElement.getEbgpBroken(), c,
                              bgpNeighbor);
                        answerElement.add(
                              answerElement.getEbgpLocalIpUnknown(), c,
                              bgpNeighbor);
                     }
                     if (!allInterfaceIps.contains(remoteIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighbor);
                        answerElement.add(answerElement.getRemoteIpUnknown(),
                              c, bgpNeighbor);
                        answerElement.add(answerElement.getEbgpBroken(), c,
                              bgpNeighbor);
                        answerElement.add(
                              answerElement.getEbgpRemoteIpUnknown(), c,
                              bgpNeighbor);
                     }
                     else {
                        if (!ebgpMultihop && loopbackIps.contains(remoteIp)) {
                           answerElement.add(
                                 answerElement.getEbgpRemoteIpOnLoopback(), c,
                                 bgpNeighbor);
                        }
                     }
                     // check half open
                     if (localIp != null && allInterfaceIps.contains(remoteIp)) {
                        if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                           answerElement.add(answerElement.getBroken(), c,
                                 bgpNeighbor);
                           answerElement.add(answerElement.getHalfOpen(), c,
                                 bgpNeighbor);
                           answerElement.add(answerElement.getEbgpBroken(), c,
                                 bgpNeighbor);
                           answerElement.add(answerElement.getEbgpHalfOpen(),
                                 c, bgpNeighbor);
                        }
                        else if (bgpNeighbor.getCandidateRemoteBgpNeighbors()
                              .size() != 1) {
                           answerElement.add(
                                 answerElement.getNonUniqueEndpoint(), c,
                                 bgpNeighbor);
                           answerElement.add(
                                 answerElement.getEbgpNonUniqueEndpoint(), c,
                                 bgpNeighbor);
                        }
                     }
                  }
                  else {
                     // ibgp
                     if (localIp != null && !allInterfaceIps.contains(localIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighbor);
                        answerElement.add(answerElement.getIbgpBroken(), c,
                              bgpNeighbor);
                        answerElement.add(
                              answerElement.getIbgpLocalIpUnknown(), c,
                              bgpNeighbor);
                     }
                     if (!allInterfaceIps.contains(remoteIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighbor);
                        answerElement.add(answerElement.getIbgpBroken(), c,
                              bgpNeighbor);
                        answerElement.add(
                              answerElement.getIbgpRemoteIpUnknown(), c,
                              bgpNeighbor);
                     }
                     else {
                        if (!loopbackIps.contains(remoteIp)) {
                           answerElement.add(
                                 answerElement.getIbgpRemoteIpOnNonLoopback(),
                                 c, bgpNeighbor);
                        }
                     }
                     if (localIp != null && allInterfaceIps.contains(remoteIp)) {
                        if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                           answerElement.add(answerElement.getBroken(), c,
                                 bgpNeighbor);
                           answerElement.add(answerElement.getHalfOpen(), c,
                                 bgpNeighbor);
                           answerElement.add(answerElement.getIbgpBroken(), c,
                                 bgpNeighbor);
                           answerElement.add(answerElement.getIbgpHalfOpen(),
                                 c, bgpNeighbor);
                        }
                        else if (bgpNeighbor.getCandidateRemoteBgpNeighbors()
                              .size() != 1) {
                           answerElement.add(
                                 answerElement.getNonUniqueEndpoint(), c,
                                 bgpNeighbor);
                           answerElement.add(
                                 answerElement.getIbgpNonUniqueEndpoint(), c,
                                 bgpNeighbor);
                        }
                     }
                  }
               }
            }
         }
      }
      addAnswerElement(answerElement);
   }

}
