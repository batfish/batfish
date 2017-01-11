package org.batfish.question;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.BgpNeighbor.BgpNeighborSummary;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BgpSessionCheckQuestionPlugin extends QuestionPlugin {

   public static class BgpSessionCheckAnswerElement implements AnswerElement {

      private SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> _allBgpNeighborSummarys;

      private SortedMap<String, SortedSet<Prefix>> _broken;

      private SortedMap<String, SortedSet<Prefix>> _ebgpBroken;

      private SortedMap<String, SortedSet<Prefix>> _ebgpHalfOpen;

      private SortedMap<String, SortedSet<Prefix>> _ebgpLocalIpOnLoopback;

      private SortedMap<String, SortedSet<Prefix>> _ebgpLocalIpUnknown;

      private SortedMap<String, SortedSet<Prefix>> _ebgpMissingLocalIp;

      private SortedMap<String, SortedSet<Prefix>> _ebgpNonUniqueEndpoint;

      private SortedMap<String, SortedSet<Prefix>> _ebgpRemoteIpOnLoopback;

      private SortedMap<String, SortedSet<Prefix>> _ebgpRemoteIpUnknown;

      private SortedMap<String, SortedSet<Prefix>> _halfOpen;

      private SortedMap<String, SortedSet<Prefix>> _ibgpBroken;

      private SortedMap<String, SortedSet<Prefix>> _ibgpHalfOpen;

      private SortedMap<String, SortedSet<Prefix>> _ibgpLocalIpOnNonLoopback;

      private SortedMap<String, SortedSet<Prefix>> _ibgpLocalIpUnknown;

      private SortedMap<String, SortedSet<Prefix>> _ibgpMissingLocalIp;

      private SortedMap<String, SortedSet<Prefix>> _ibgpNonUniqueEndpoint;

      private SortedMap<String, SortedSet<Prefix>> _ibgpRemoteIpOnNonLoopback;

      private SortedMap<String, SortedSet<Prefix>> _ibgpRemoteIpUnknown;

      private SortedMap<String, SortedSet<Prefix>> _ignoredForeignEndpoints;

      private SortedMap<String, SortedSet<Prefix>> _localIpUnknown;

      private SortedMap<String, SortedSet<Prefix>> _missingLocalIp;

      private SortedMap<String, SortedSet<Prefix>> _nonUniqueEndpoint;

      private SortedMap<String, SortedSet<Prefix>> _remoteIpUnknown;

      public BgpSessionCheckAnswerElement() {
         _allBgpNeighborSummarys = new TreeMap<>();
         _broken = new TreeMap<>();
         _ebgpBroken = new TreeMap<>();
         _ibgpBroken = new TreeMap<>();
         _ebgpHalfOpen = new TreeMap<>();
         _ebgpLocalIpOnLoopback = new TreeMap<>();
         _ebgpLocalIpUnknown = new TreeMap<>();
         _ebgpMissingLocalIp = new TreeMap<>();
         _ebgpNonUniqueEndpoint = new TreeMap<>();
         _ebgpRemoteIpOnLoopback = new TreeMap<>();
         _ebgpRemoteIpUnknown = new TreeMap<>();
         _halfOpen = new TreeMap<>();
         _ibgpHalfOpen = new TreeMap<>();
         _ibgpLocalIpOnNonLoopback = new TreeMap<>();
         _ibgpLocalIpUnknown = new TreeMap<>();
         _ibgpMissingLocalIp = new TreeMap<>();
         _ibgpNonUniqueEndpoint = new TreeMap<>();
         _ibgpRemoteIpOnNonLoopback = new TreeMap<>();
         _ibgpRemoteIpUnknown = new TreeMap<>();
         _ignoredForeignEndpoints = new TreeMap<>();
         _localIpUnknown = new TreeMap<>();
         _missingLocalIp = new TreeMap<>();
         _nonUniqueEndpoint = new TreeMap<>();
         _remoteIpUnknown = new TreeMap<>();
      }

      public void add(SortedMap<String, SortedSet<Prefix>> neighborsByHostname,
            Configuration c, BgpNeighborSummary bgpNeighbor) {
         String hostname = c.getHostname();
         SortedSet<Prefix> neighbors = neighborsByHostname.get(hostname);
         if (neighbors == null) {
            neighbors = new TreeSet<>();
            neighborsByHostname.put(hostname, neighbors);
         }
         Prefix prefix = bgpNeighbor.getPrefix();
         neighbors.add(prefix);
      }

      public void addToAll(
            SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> neighborsByHostname,
            Configuration c, BgpNeighborSummary bgpNeighbor) {
         String hostname = c.getHostname();
         SortedMap<Prefix, BgpNeighborSummary> neighborsByPrefix = neighborsByHostname
               .get(hostname);
         if (neighborsByPrefix == null) {
            neighborsByPrefix = new TreeMap<>();
            neighborsByHostname.put(hostname, neighborsByPrefix);
         }
         Prefix prefix = bgpNeighbor.getPrefix();
         neighborsByPrefix.put(prefix, bgpNeighbor);
      }

      public SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> getAllBgpNeighbors() {
         return _allBgpNeighborSummarys;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getBroken() {
         return _broken;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpBroken() {
         return _ebgpBroken;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpHalfOpen() {
         return _ebgpHalfOpen;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpLocalIpOnLoopback() {
         return _ebgpLocalIpOnLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpLocalIpUnknown() {
         return _ebgpLocalIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpMissingLocalIp() {
         return _ebgpMissingLocalIp;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpNonUniqueEndpoint() {
         return _ebgpNonUniqueEndpoint;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpRemoteIpOnLoopback() {
         return _ebgpRemoteIpOnLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getEbgpRemoteIpUnknown() {
         return _ebgpRemoteIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getHalfOpen() {
         return _halfOpen;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpBroken() {
         return _ibgpBroken;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpHalfOpen() {
         return _ibgpHalfOpen;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpLocalIpOnNonLoopback() {
         return _ibgpLocalIpOnNonLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpLocalIpUnknown() {
         return _ibgpLocalIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpMissingLocalIp() {
         return _ibgpMissingLocalIp;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpNonUniqueEndpoint() {
         return _ibgpNonUniqueEndpoint;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpRemoteIpOnNonLoopback() {
         return _ibgpRemoteIpOnNonLoopback;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIbgpRemoteIpUnknown() {
         return _ibgpRemoteIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getIgnoredForeignEndpoints() {
         return _ignoredForeignEndpoints;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getLocalIpUnknown() {
         return _localIpUnknown;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getMissingLocalIp() {
         return _missingLocalIp;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getNonUniqueEndpoint() {
         return _nonUniqueEndpoint;
      }

      @JsonIdentityReference(alwaysAsId = true)
      public SortedMap<String, SortedSet<Prefix>> getRemoteIpUnknown() {
         return _remoteIpUnknown;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         // TODO: change this function to pretty print the answer
         ObjectMapper mapper = new BatfishObjectMapper();
         return mapper.writeValueAsString(this);
      }

      public void setAllBgpNeighborSummarys(
            SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> allBgpNeighborSummarys) {
         _allBgpNeighborSummarys = allBgpNeighborSummarys;
      }

      public void setBroken(SortedMap<String, SortedSet<Prefix>> broken) {
         _broken = broken;
      }

      public void setEbgpBroken(
            SortedMap<String, SortedSet<Prefix>> ebgpBroken) {
         _ebgpBroken = ebgpBroken;
      }

      public void setEbgpHalfOpen(
            SortedMap<String, SortedSet<Prefix>> ebgpHalfOpen) {
         _ebgpHalfOpen = ebgpHalfOpen;
      }

      public void setEbgpLocalIpOnLoopback(
            SortedMap<String, SortedSet<Prefix>> ebgpLocalIpOnLoopback) {
         _ebgpLocalIpOnLoopback = ebgpLocalIpOnLoopback;
      }

      public void setEbgpLocalIpUnknown(
            SortedMap<String, SortedSet<Prefix>> ebgpLocalIpUnknown) {
         _ebgpLocalIpUnknown = ebgpLocalIpUnknown;
      }

      public void setEbgpMissingLocalIp(
            SortedMap<String, SortedSet<Prefix>> ebgpMissingLocalIp) {
         _ebgpMissingLocalIp = ebgpMissingLocalIp;
      }

      public void setEbgpNonUniqueEndpoint(
            SortedMap<String, SortedSet<Prefix>> ebgpNonUniqueEndpoint) {
         _ebgpNonUniqueEndpoint = ebgpNonUniqueEndpoint;
      }

      public void setEbgpRemoteIpOnLoopback(
            SortedMap<String, SortedSet<Prefix>> ebgpRemoteIpOnLoopback) {
         _ebgpRemoteIpOnLoopback = ebgpRemoteIpOnLoopback;
      }

      public void setEbgpRemoteIpUnknown(
            SortedMap<String, SortedSet<Prefix>> ebgpRemoteIpUnknown) {
         _ebgpRemoteIpUnknown = ebgpRemoteIpUnknown;
      }

      public void setHalfOpen(SortedMap<String, SortedSet<Prefix>> halfOpen) {
         _halfOpen = halfOpen;
      }

      public void setIbgpBroken(
            SortedMap<String, SortedSet<Prefix>> ibgpBroken) {
         _ibgpBroken = ibgpBroken;
      }

      public void setIbgpHalfOpen(
            SortedMap<String, SortedSet<Prefix>> ibgpHalfOpen) {
         _ibgpHalfOpen = ibgpHalfOpen;
      }

      public void setIbgpLocalIpOnNonLoopback(
            SortedMap<String, SortedSet<Prefix>> ibgpLocalIpOnNonLoopback) {
         _ibgpLocalIpOnNonLoopback = ibgpLocalIpOnNonLoopback;
      }

      public void setIbgpLocalIpUnknown(
            SortedMap<String, SortedSet<Prefix>> ibgpLocalIpUnknown) {
         _ibgpLocalIpUnknown = ibgpLocalIpUnknown;
      }

      public void setIbgpMissingLocalIp(
            SortedMap<String, SortedSet<Prefix>> ibgpMissingLocalIp) {
         _ibgpMissingLocalIp = ibgpMissingLocalIp;
      }

      public void setIbgpNonUniqueEndpoint(
            SortedMap<String, SortedSet<Prefix>> ibgpNonUniqueEndpoint) {
         _ibgpNonUniqueEndpoint = ibgpNonUniqueEndpoint;
      }

      public void setIbgpRemoteIpOnNonLoopback(
            SortedMap<String, SortedSet<Prefix>> ibgpRemoteIpOnNonLoopback) {
         _ibgpRemoteIpOnNonLoopback = ibgpRemoteIpOnNonLoopback;
      }

      public void setIbgpRemoteIpUnknown(
            SortedMap<String, SortedSet<Prefix>> ibgpRemoteIpUnknown) {
         _ibgpRemoteIpUnknown = ibgpRemoteIpUnknown;
      }

      public void setIgnoredForeignEndpoints(
            SortedMap<String, SortedSet<Prefix>> ignoredForeignEndpoints) {
         _ignoredForeignEndpoints = ignoredForeignEndpoints;
      }

      public void setLocalIpUnknown(
            SortedMap<String, SortedSet<Prefix>> localIpUnknown) {
         _localIpUnknown = localIpUnknown;
      }

      public void setMissingLocalIp(
            SortedMap<String, SortedSet<Prefix>> missingLocalIp) {
         _missingLocalIp = missingLocalIp;
      }

      public void setNonUniqueEndpoint(
            SortedMap<String, SortedSet<Prefix>> nonUniqueEndpoint) {
         _nonUniqueEndpoint = nonUniqueEndpoint;
      }

      public void setRemoteIpUnknown(
            SortedMap<String, SortedSet<Prefix>> remoteIpUnknown) {
         _remoteIpUnknown = remoteIpUnknown;
      }
   }

   public static class BgpSessionCheckAnswerer extends Answerer {

      public BgpSessionCheckAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {

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
               .loadConfigurations();

         BgpSessionCheckAnswerElement answerElement = new BgpSessionCheckAnswerElement();
         Set<Ip> allInterfaceIps = new HashSet<>();
         Set<Ip> loopbackIps = new HashSet<>();
         Map<Ip, Set<String>> ipOwners = new HashMap<>();
         for (Configuration c : configurations.values()) {
            for (Interface i : c.getInterfaces().values()) {
               if (i.getActive()) {
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
         }
         _batfish.initRemoteBgpNeighbors(configurations, ipOwners);
         for (Configuration c : configurations.values()) {
            if (!node1Regex.matcher(c.getHostname()).matches()) {
               continue;
            }
            for (Vrf vrf : c.getVrfs().values()) {
               BgpProcess proc = vrf.getBgpProcess();

               if (proc != null) {
                  for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
                     BgpNeighborSummary bgpNeighborSummary = new BgpNeighborSummary(
                           bgpNeighbor);
                     answerElement.addToAll(answerElement.getAllBgpNeighbors(),
                           c, bgpNeighborSummary);
                     boolean foreign = bgpNeighbor.getGroup() != null
                           && question.getForeignBgpGroups()
                                 .contains(bgpNeighbor.getGroup());
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
                           answerElement.add(answerElement.getMissingLocalIp(),
                                 c, bgpNeighborSummary);
                           answerElement.add(answerElement.getEbgpBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(
                                 answerElement.getEbgpMissingLocalIp(), c,
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
                           answerElement.add(answerElement.getMissingLocalIp(),
                                 c, bgpNeighborSummary);
                           answerElement.add(answerElement.getIbgpBroken(), c,
                                 bgpNeighborSummary);
                           answerElement.add(
                                 answerElement.getIbgpMissingLocalIp(), c,
                                 bgpNeighborSummary);
                        }
                     }
                     if (foreign) {
                        answerElement.add(
                              answerElement.getIgnoredForeignEndpoints(), c,
                              bgpNeighborSummary);
                     }
                     else {
                        // not foreign
                        if (ebgp) {
                           if (localIp != null
                                 && !allInterfaceIps.contains(localIp)) {
                              answerElement.add(answerElement.getBroken(), c,
                                    bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getLocalIpUnknown(), c,
                                    bgpNeighborSummary);
                              answerElement.add(answerElement.getEbgpBroken(),
                                    c, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getEbgpLocalIpUnknown(), c,
                                    bgpNeighborSummary);
                           }
                           if (!allInterfaceIps.contains(remoteIp)) {
                              answerElement.add(answerElement.getBroken(), c,
                                    bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getRemoteIpUnknown(), c,
                                    bgpNeighborSummary);
                              answerElement.add(answerElement.getEbgpBroken(),
                                    c, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getEbgpRemoteIpUnknown(), c,
                                    bgpNeighborSummary);
                           }
                           else {
                              if (!ebgpMultihop
                                    && loopbackIps.contains(remoteIp)
                                    && node2RegexMatchesIp(remoteIp, ipOwners,
                                          node2Regex)) {
                                 answerElement.add(
                                       answerElement
                                             .getEbgpRemoteIpOnLoopback(),
                                       c, bgpNeighborSummary);
                              }
                           }
                           // check half open
                           if (localIp != null
                                 && allInterfaceIps.contains(remoteIp)
                                 && node2RegexMatchesIp(remoteIp, ipOwners,
                                       node2Regex)) {
                              if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                                 answerElement.add(answerElement.getBroken(), c,
                                       bgpNeighborSummary);
                                 answerElement.add(answerElement.getHalfOpen(),
                                       c, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getEbgpBroken(), c,
                                       bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getEbgpHalfOpen(), c,
                                       bgpNeighborSummary);
                              }
                              else if (bgpNeighbor
                                    .getCandidateRemoteBgpNeighbors()
                                    .size() != 1) {
                                 answerElement.add(
                                       answerElement.getNonUniqueEndpoint(), c,
                                       bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getEbgpNonUniqueEndpoint(),
                                       c, bgpNeighborSummary);
                              }
                           }
                        }
                        else {
                           // ibgp
                           if (localIp != null
                                 && !allInterfaceIps.contains(localIp)) {
                              answerElement.add(answerElement.getBroken(), c,
                                    bgpNeighborSummary);
                              answerElement.add(answerElement.getIbgpBroken(),
                                    c, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getIbgpLocalIpUnknown(), c,
                                    bgpNeighborSummary);
                           }
                           if (!allInterfaceIps.contains(remoteIp)) {
                              answerElement.add(answerElement.getBroken(), c,
                                    bgpNeighborSummary);
                              answerElement.add(answerElement.getIbgpBroken(),
                                    c, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getIbgpRemoteIpUnknown(), c,
                                    bgpNeighborSummary);
                           }
                           else {
                              if (!loopbackIps.contains(remoteIp)
                                    && node2RegexMatchesIp(remoteIp, ipOwners,
                                          node2Regex)) {
                                 answerElement.add(
                                       answerElement
                                             .getIbgpRemoteIpOnNonLoopback(),
                                       c, bgpNeighborSummary);
                              }
                           }
                           if (localIp != null
                                 && allInterfaceIps.contains(remoteIp)
                                 && node2RegexMatchesIp(remoteIp, ipOwners,
                                       node2Regex)) {
                              if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                                 answerElement.add(answerElement.getBroken(), c,
                                       bgpNeighborSummary);
                                 answerElement.add(answerElement.getHalfOpen(),
                                       c, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getIbgpBroken(), c,
                                       bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getIbgpHalfOpen(), c,
                                       bgpNeighborSummary);
                              }
                              else if (bgpNeighbor
                                    .getCandidateRemoteBgpNeighbors()
                                    .size() != 1) {
                                 answerElement.add(
                                       answerElement.getNonUniqueEndpoint(), c,
                                       bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getIbgpNonUniqueEndpoint(),
                                       c, bgpNeighborSummary);
                              }
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

   // <question_page_comment>
   /**
    * Checks if BGP sessions are correctly configured.
    * <p>
    * Details coming
    *
    * @type BgpSessionCheck multifile
    *
    * @param foreignBgpGroups
    *           Details coming.
    * @param node1Regex
    *           Regular expression to match the nodes names for one end of the
    *           sessions. Default is '.*' (all nodes).
    * @param node2Regex
    *           Regular expression to match the nodes names for the other end of
    *           the sessions. Default is '.*' (all nodes).
    *
    * @example bf_answer("BgpSessionCheck", node1Regex="as1.*",
    *          node2Regex="as2.*") Checks all BGP sessions between nodes that
    *          start with as1 and those that start with as2.
    */
   public static class BgpSessionCheckQuestion extends Question {

      private static final String FOREIGN_BGP_GROUPS_VAR = "foreignBgpGroups";

      private static final String NODE1_REGEX_VAR = "node1Regex";

      private static final String NODE2_REGEX_VAR = "node2Regex";

      private Set<String> _foreignBgpGroups;

      private String _node1Regex;

      private String _node2Regex;

      public BgpSessionCheckQuestion() {
         _foreignBgpGroups = new TreeSet<>();
         _node1Regex = ".*";
         _node2Regex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @JsonProperty(FOREIGN_BGP_GROUPS_VAR)
      public Set<String> getForeignBgpGroups() {
         return _foreignBgpGroups;
      }

      @Override
      public String getName() {
         return "bgpsessioncheck";
      }

      @JsonProperty(NODE1_REGEX_VAR)
      public String getNode1Regex() {
         return _node1Regex;
      }

      @JsonProperty(NODE2_REGEX_VAR)
      public String getNode2Regex() {
         return _node2Regex;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      public void setForeignBgpGroups(Set<String> foreignBgpGroups) {
         _foreignBgpGroups = foreignBgpGroups;
      }

      @Override
      public void setJsonParameters(JSONObject parameters) {
         super.setJsonParameters(parameters);

         Iterator<?> paramKeys = parameters.keys();

         while (paramKeys.hasNext()) {
            String paramKey = (String) paramKeys.next();
            if (isBaseParamKey(paramKey)) {
               continue;
            }

            try {
               switch (paramKey) {
               case FOREIGN_BGP_GROUPS_VAR:
                  setForeignBgpGroups(new ObjectMapper()
                        .<Set<String>> readValue(parameters.getString(paramKey),
                              new TypeReference<Set<String>>() {
                              }));
                  break;
               case NODE1_REGEX_VAR:
                  setNode1Regex(parameters.getString(paramKey));
                  break;
               case NODE2_REGEX_VAR:
                  setNode2Regex(parameters.getString(paramKey));
                  break;
               default:
                  throw new BatfishException("Unknown key in "
                        + getClass().getSimpleName() + ": " + paramKey);
               }
            }
            catch (JSONException | IOException e) {
               throw new BatfishException("JSONException in parameters", e);
            }
         }
      }

      public void setNode1Regex(String regex) {
         _node1Regex = regex;
      }

      public void setNode2Regex(String regex) {
         _node2Regex = regex;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new BgpSessionCheckAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new BgpSessionCheckQuestion();
   }

}
