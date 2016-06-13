package org.batfish.question;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
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

   private Map<Ip, Set<String>> _ipOwners;

   private Pattern _node2Regex;

   public BgpSessionCheckAnswer(Batfish batfish,
         BgpSessionCheckQuestion question) {
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
      _node2Regex = node2Regex;
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      batfish.initRemoteBgpNeighbors(configurations);
      BgpSessionCheckAnswerElement answerElement = new BgpSessionCheckAnswerElement();
      Set<Ip> allInterfaceIps = new HashSet<Ip>();
      Set<Ip> loopbackIps = new HashSet<Ip>();
      _ipOwners = new HashMap<Ip, Set<String>>();
      for (Configuration c : configurations.values()) {
         for (Interface i : c.getInterfaces().values()) {
            if (i.getPrefix() != null) {
               for (Prefix prefix : i.getAllPrefixes()) {
                  Ip address = prefix.getAddress();
                  if (i.isLoopback(c.getConfigurationFormat())) {
                     loopbackIps.add(address);
                  }
                  allInterfaceIps.add(address);
                  Set<String> currentIpOwners = _ipOwners.get(address);
                  if (currentIpOwners == null) {
                     currentIpOwners = new HashSet<String>();
                     _ipOwners.put(address, currentIpOwners);
                  }
                  currentIpOwners.add(c.getHostname());
               }
            }
         }
      }
      for (Configuration c : configurations.values()) {
         if (!node1Regex.matcher(c.getHostname()).matches()) {
            continue;
         }
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
                        if (!ebgpMultihop && loopbackIps.contains(remoteIp)
                              && node2RegexMatchesIp(remoteIp)) {
                           answerElement.add(
                                 answerElement.getEbgpRemoteIpOnLoopback(), c,
                                 bgpNeighborSummary);
                        }
                     }
                     // check half open
                     if (localIp != null && allInterfaceIps.contains(remoteIp)
                           && node2RegexMatchesIp(remoteIp)) {
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
                     if (!allInterfaceIps.contains(remoteIp)
                           && node2RegexMatchesIp(remoteIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getIbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(
                              answerElement.getIbgpRemoteIpUnknown(), c,
                              bgpNeighborSummary);
                     }
                     else {
                        if (!loopbackIps.contains(remoteIp)
                              && node2RegexMatchesIp(remoteIp)) {
                           answerElement.add(
                                 answerElement.getIbgpRemoteIpOnNonLoopback(),
                                 c, bgpNeighborSummary);
                        }
                     }
                     if (localIp != null && allInterfaceIps.contains(remoteIp)
                           && node2RegexMatchesIp(remoteIp)) {
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

   private boolean node2RegexMatchesIp(Ip ip) {
      Set<String> owners = _ipOwners.get(ip);
      if (owners == null) {
         throw new BatfishException("Expected at least one owner of ip: "
               + ip.toString());
      }
      for (String owner : owners) {
         if (_node2Regex.matcher(owner).matches()) {
            return true;
         }
      }
      return false;
   }

}
