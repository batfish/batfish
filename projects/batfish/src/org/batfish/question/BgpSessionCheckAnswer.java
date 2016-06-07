package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.BgpNeighbor.BgpNeighborSummary;
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
                  if (i.isLoopback(c.getConfigurationFormat())) {
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
               BgpNeighborSummary bgpNeighborSummary = new BgpNeighborSummary(
                     bgpNeighbor);
               answerElement.add(answerElement.getAllBgpNeighbors(), c,
                     bgpNeighborSummary);
               boolean foreign = bgpNeighbor.getGroup() != null
                     && question.getForeignBgpGroups().contains(
                           bgpNeighbor.getGroup());
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
                           bgpNeighborSummary);
                  }
                  if (localIp == null) {
                     answerElement.add(answerElement.getBroken(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getMissingLocalIp(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getEbgpBroken(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getEbgpMissingLocalIp(),
                           c, bgpNeighborSummary);
                  }
               }
               else {
                  // ibgp
                  if (!loopbackIps.contains(localIp)) {
                     answerElement.add(
                           answerElement.getIbgpLocalIpOnNonLoopback(), c,
                           bgpNeighborSummary);
                  }
                  if (localIp == null) {
                     answerElement.add(answerElement.getBroken(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getMissingLocalIp(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getIbgpBroken(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getIbgpMissingLocalIp(),
                           c, bgpNeighborSummary);
                  }
               }
               if (foreign) {
                  answerElement.add(answerElement.getIgnoredForeignEndpoints(),
                        c, bgpNeighborSummary);
               }
               else {
                  // not foreign
                  if (ebgp) {
                     if (localIp != null && !allInterfaceIps.contains(localIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getLocalIpUnknown(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getEbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(
                              answerElement.getEbgpLocalIpUnknown(), c,
                              bgpNeighborSummary);
                     }
                     if (!allInterfaceIps.contains(remoteIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getRemoteIpUnknown(),
                              c, bgpNeighborSummary);
                        answerElement.add(answerElement.getEbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(
                              answerElement.getEbgpRemoteIpUnknown(), c,
                              bgpNeighborSummary);
                     }
                     else {
                        if (!ebgpMultihop && loopbackIps.contains(remoteIp)) {
                           answerElement.add(
                                 answerElement.getEbgpRemoteIpOnLoopback(), c,
                                 bgpNeighborSummary);
                        }
                     }
                     // check half open
                     if (localIp != null && allInterfaceIps.contains(remoteIp)) {
                        if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                           answerElement.add(answerElement.getBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getHalfOpen(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getEbgpBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getEbgpHalfOpen(),
                                 c, bgpNeighborSummary);
                        }
                        else if (bgpNeighbor.getCandidateRemoteBgpNeighbors()
                              .size() != 1) {
                           answerElement.add(
                                 answerElement.getNonUniqueEndpoint(), c,
                                 bgpNeighborSummary);
                           answerElement.add(
                                 answerElement.getEbgpNonUniqueEndpoint(), c,
                                 bgpNeighborSummary);
                        }
                     }
                  }
                  else {
                     // ibgp
                     if (localIp != null && !allInterfaceIps.contains(localIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getIbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(
                              answerElement.getIbgpLocalIpUnknown(), c,
                              bgpNeighborSummary);
                     }
                     if (!allInterfaceIps.contains(remoteIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getIbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(
                              answerElement.getIbgpRemoteIpUnknown(), c,
                              bgpNeighborSummary);
                     }
                     else {
                        if (!loopbackIps.contains(remoteIp)) {
                           answerElement.add(
                                 answerElement.getIbgpRemoteIpOnNonLoopback(),
                                 c, bgpNeighborSummary);
                        }
                     }
                     if (localIp != null && allInterfaceIps.contains(remoteIp)) {
                        if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                           answerElement.add(answerElement.getBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getHalfOpen(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getIbgpBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getIbgpHalfOpen(),
                                 c, bgpNeighborSummary);
                        }
                        else if (bgpNeighbor.getCandidateRemoteBgpNeighbors()
                              .size() != 1) {
                           answerElement.add(
                                 answerElement.getNonUniqueEndpoint(), c,
                                 bgpNeighborSummary);
                           answerElement.add(
                                 answerElement.getIbgpNonUniqueEndpoint(), c,
                                 bgpNeighborSummary);
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
