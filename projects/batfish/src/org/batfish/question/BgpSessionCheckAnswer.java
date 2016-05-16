package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.questions.BgpSessionCheckQuestion;
import org.batfish.main.Batfish;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class BgpSessionCheckAnswer extends Answer {

   public class BgpSessionCheckAnswerElement implements AnswerElement {

      private final Map<String, Map<Prefix, BgpNeighbor>> _allBgpNeighbors;

      private final Map<String, Map<Prefix, BgpNeighbor>> _broken;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpBroken;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpHalfOpen;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpLocalIpOnLoopback;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpLocalIpUnknown;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpMissingLocalIp;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpNonUniqueEndpoint;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpRemoteIpOnLoopback;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ebgpRemoteIpUnknown;

      private final Map<String, Map<Prefix, BgpNeighbor>> _halfOpen;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpBroken;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpHalfOpen;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpLocalIpOnNonLoopback;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpLocalIpUnknown;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpMissingLocalIp;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpNonUniqueEndpoint;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpRemoteIpOnNonLoopback;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ibgpRemoteIpUnknown;

      private final Map<String, Map<Prefix, BgpNeighbor>> _ignoredForeignEndpoints;

      private final Map<String, Map<Prefix, BgpNeighbor>> _localIpUnknown;

      private final Map<String, Map<Prefix, BgpNeighbor>> _missingLocalIp;

      private final Map<String, Map<Prefix, BgpNeighbor>> _nonUniqueEndpoint;

      private final Map<String, Map<Prefix, BgpNeighbor>> _remoteIpUnknown;

      private BgpSessionCheckAnswerElement() {
         _allBgpNeighbors = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _broken = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpBroken = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpBroken = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpHalfOpen = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpLocalIpOnLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpLocalIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpMissingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpNonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpRemoteIpOnLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ebgpRemoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _halfOpen = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpHalfOpen = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpLocalIpOnNonLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpLocalIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpMissingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpNonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpRemoteIpOnNonLoopback = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ibgpRemoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _ignoredForeignEndpoints = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _localIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _missingLocalIp = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _nonUniqueEndpoint = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
         _remoteIpUnknown = new TreeMap<String, Map<Prefix, BgpNeighbor>>();
      }

      private void add(
            Map<String, Map<Prefix, BgpNeighbor>> neighborsByHostname,
            Configuration c, BgpNeighbor bgpNeighbor) {
         String hostname = c.getHostname();
         Map<Prefix, BgpNeighbor> neighborsByPrefix = neighborsByHostname
               .get(hostname);
         if (neighborsByPrefix == null) {
            neighborsByPrefix = new TreeMap<Prefix, BgpNeighbor>();
            neighborsByHostname.put(hostname, neighborsByPrefix);
         }
         Prefix prefix = bgpNeighbor.getPrefix();
         neighborsByPrefix.put(prefix, bgpNeighbor);
      }

      public Map<String, Map<Prefix, BgpNeighbor>> getAllBgpNeighbors() {
         return _allBgpNeighbors;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getBroken() {
         return _broken;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpBroken() {
         return _ebgpBroken;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpHalfOpen() {
         return _ebgpHalfOpen;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpLocalIpOnLoopback() {
         return _ebgpLocalIpOnLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpLocalIpUnknown() {
         return _ebgpLocalIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpMissingLocalIp() {
         return _ebgpMissingLocalIp;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpNonUniqueEndpoint() {
         return _ebgpNonUniqueEndpoint;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpRemoteIpOnLoopback() {
         return _ebgpRemoteIpOnLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getEbgpRemoteIpUnknown() {
         return _ebgpRemoteIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getHalfOpen() {
         return _halfOpen;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpBroken() {
         return _ibgpBroken;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpHalfOpen() {
         return _ibgpHalfOpen;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpLocalIpOnNonLoopback() {
         return _ibgpLocalIpOnNonLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpLocalIpUnknown() {
         return _ibgpLocalIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpMissingLocalIp() {
         return _ibgpMissingLocalIp;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpNonUniqueEndpoint() {
         return _ibgpNonUniqueEndpoint;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpRemoteIpOnNonLoopback() {
         return _ibgpRemoteIpOnNonLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIbgpRemoteIpUnknown() {
         return _ibgpRemoteIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getIgnoredForeignEndpoints() {
         return _ignoredForeignEndpoints;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getLocalIpUnknown() {
         return _localIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getMissingLocalIp() {
         return _missingLocalIp;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getNonUniqueEndpoint() {
         return _nonUniqueEndpoint;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public Map<String, Map<Prefix, BgpNeighbor>> getRemoteIpUnknown() {
         return _remoteIpUnknown;
      }
   }

   public BgpSessionCheckAnswer(Batfish batfish,
         BgpSessionCheckQuestion question) {
      setQuestion(question);
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
      setStatus(AnswerStatus.SUCCESS);
   }

}
