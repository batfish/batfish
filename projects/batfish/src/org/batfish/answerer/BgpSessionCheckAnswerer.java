package org.batfish.answerer;

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
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.BgpSessionCheckAnswerElement;
import org.batfish.datamodel.questions.BgpSessionCheckQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class BgpSessionCheckAnswerer extends Answerer {

   public BgpSessionCheckAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      BgpSessionCheckQuestion question = (BgpSessionCheckQuestion) _question;

      Pattern node1Regex;
      Pattern node2Regex;
      try {
         node1Regex = Pattern.compile(question.getNode1Regex());
         node2Regex = Pattern.compile(question.getNode2Regex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(String.format(
               "One of the supplied regexes (%s  OR  %s) is not a valid java regex.",
               question.getNode1Regex(), question.getNode2Regex()), e);
      }
      _batfish.checkConfigurations();
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);
      _batfish.initRemoteBgpNeighbors(configurations);

      BgpSessionCheckAnswerElement answerElement = new BgpSessionCheckAnswerElement();
      Set<Ip> allInterfaceIps = new HashSet<>();
      Set<Ip> loopbackIps = new HashSet<>();
      Map<Ip, Set<String>> ipOwners = new HashMap<>();
      for (Configuration c : configurations.values()) {
         for (Interface i : c.getInterfaces().values()) {
            if (i.getPrefix() != null) {
               for (Prefix prefix : i.getAllPrefixes()) {
                  Ip address = prefix.getAddress();
                  if (i.isLoopback(c.getConfigurationFormat())) {
                     loopbackIps.add(address);
                  }
                  allInterfaceIps.add(address);
                  Set<String> currentIpOwners = ipOwners.get(address);
                  if (currentIpOwners == null) {
                     currentIpOwners = new HashSet<>();
                     ipOwners.put(address, currentIpOwners);
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
               boolean foreign = bgpNeighbor.getGroup() != null && question
                     .getForeignBgpGroups().contains(bgpNeighbor.getGroup());
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
                     answerElement.add(answerElement.getEbgpLocalIpOnLoopback(),
                           c, bgpNeighborSummary);
                  }
                  if (localIp == null) {
                     answerElement.add(answerElement.getBroken(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getMissingLocalIp(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getEbgpBroken(), c,
                           bgpNeighborSummary);
                     answerElement.add(answerElement.getEbgpMissingLocalIp(), c,
                           bgpNeighborSummary);
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
                     answerElement.add(answerElement.getIbgpMissingLocalIp(), c,
                           bgpNeighborSummary);
                  }
               }
               if (foreign) {
                  answerElement.add(answerElement.getIgnoredForeignEndpoints(),
                        c, bgpNeighborSummary);
               }
               else {
                  // not foreign
                  if (ebgp) {
                     if (localIp != null
                           && !allInterfaceIps.contains(localIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getLocalIpUnknown(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getEbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getEbgpLocalIpUnknown(),
                              c, bgpNeighborSummary);
                     }
                     if (!allInterfaceIps.contains(remoteIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getRemoteIpUnknown(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getEbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(
                              answerElement.getEbgpRemoteIpUnknown(), c,
                              bgpNeighborSummary);
                     }
                     else {
                        if (!ebgpMultihop && loopbackIps.contains(remoteIp)
                              && node2RegexMatchesIp(remoteIp, ipOwners,
                                    node2Regex)) {
                           answerElement.add(
                                 answerElement.getEbgpRemoteIpOnLoopback(), c,
                                 bgpNeighborSummary);
                        }
                     }
                     // check half open
                     if (localIp != null && allInterfaceIps.contains(remoteIp)
                           && node2RegexMatchesIp(remoteIp, ipOwners,
                                 node2Regex)) {
                        if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                           answerElement.add(answerElement.getBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getHalfOpen(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getEbgpBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getEbgpHalfOpen(), c,
                                 bgpNeighborSummary);
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
                     if (localIp != null
                           && !allInterfaceIps.contains(localIp)) {
                        answerElement.add(answerElement.getBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getIbgpBroken(), c,
                              bgpNeighborSummary);
                        answerElement.add(answerElement.getIbgpLocalIpUnknown(),
                              c, bgpNeighborSummary);
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
                        if (!loopbackIps.contains(remoteIp)
                              && node2RegexMatchesIp(remoteIp, ipOwners,
                                    node2Regex)) {
                           answerElement.add(
                                 answerElement.getIbgpRemoteIpOnNonLoopback(),
                                 c, bgpNeighborSummary);
                        }
                     }
                     if (localIp != null && allInterfaceIps.contains(remoteIp)
                           && node2RegexMatchesIp(remoteIp, ipOwners,
                                 node2Regex)) {
                        if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                           answerElement.add(answerElement.getBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getHalfOpen(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getIbgpBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(answerElement.getIbgpHalfOpen(), c,
                                 bgpNeighborSummary);
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
      return answerElement;
   }

   private boolean node2RegexMatchesIp(Ip ip, Map<Ip, Set<String>> ipOwners,
         Pattern node2Regex) {
      Set<String> owners = ipOwners.get(ip);
      if (owners == null) {
         throw new BatfishException(
               "Expected at least one owner of ip: " + ip.toString());
      }
      for (String owner : owners) {
         if (node2Regex.matcher(owner).matches()) {
            return true;
         }
      }
      return false;
   }

}
