package org.batfish.question;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BgpSessionCheckQuestionPlugin extends QuestionPlugin {

   public static class BgpSessionCheckAnswerElement implements AnswerElement {

      private static final String ALL_BGP_NEIGHBORS_VAR = "allBgpNeighbors";

      private static final String BROKEN_VAR = "broken";

      private static final String EBGP_BROKEN_VAR = "ebgpBroken";

      private static final String EBGP_HALF_OPEN_VAR = "ebgpHalfOpen";

      private static final String EBGP_LOCAL_IP_ON_LOOPBACK_VAR = "ebgpLocalIpOnLoopback";

      private static final String EBGP_LOCAL_IP_UNKNOWN_VAR = "ebgpLocalIpUnknown";

      private static final String EBGP_MISSING_LOCAL_IP_VAR = "ebgpMissingLocalIp";

      private static final String EBGP_NON_UNIQUE_ENDPOINT_VAR = "ebgpNonUniqueEndpoint";

      private static final String EBGP_REMOTE_IP_ON_LOOPBACK_VAR = "ebgpRemoteIpOnLoopback";

      private static final String EBGP_REMOTE_IP_UNKNOWN_VAR = "ebgpRemoteIpUnknown";

      private static final String HALF_OPEN_VAR = "halfOpen";

      private static final String IBGP_BROKEN_VAR = "ibgpBroken";

      private static final String IBGP_HALF_OPEN_VAR = "ibgpHalfOpen";

      private static final String IBGP_LOCAL_IP_ON_NON_LOOPBACK_VAR = "ibgpLocalIpOnNonLoopback";

      private static final String IBGP_LOCAL_IP_UNKNOWN_VAR = "ibgpLocalIpUnknown";

      private static final String IBGP_MISSING_LOCAL_IP_VAR = "ibgpMissingLocalIp";

      private static final String IBGP_NON_UNIQUE_ENDPOINT_VAR = "ibgpNonUniqueEndpoint";

      private static final String IBGP_REMOTE_IP_ON_NON_LOOPBACK_VAR = "ibgpRemoteIpOnNonLoopback";

      private static final String IBGP_REMOTE_IP_UNKNOWN_VAR = "ibgpRemoteIpUnknown";

      private static final String IGNORED_FOREIGN_ENDPOINTS_VAR = "ignoredForeignEndpoints";

      private static final String LOCAL_IP_UNKNOWN_VAR = "localIpUnkown";

      private static final String MISSING_LOCAL_IP_VAR = "missingLocalIp";

      private static final String NON_UNIQUE_ENDPOINT_VAR = "nonUniqueEndpoint";

      private static final String REMOTE_IP_UNKNOWN_VAR = "remoteIpUnknown";

      private SortedMap<String, SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>>> _allBgpNeighbors;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _broken;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpBroken;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpHalfOpen;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpLocalIpOnLoopback;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpLocalIpUnknown;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpMissingLocalIp;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpNonUniqueEndpoint;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpRemoteIpOnLoopback;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ebgpRemoteIpUnknown;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _halfOpen;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpBroken;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpHalfOpen;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpLocalIpOnNonLoopback;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpLocalIpUnknown;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpMissingLocalIp;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpNonUniqueEndpoint;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpRemoteIpOnNonLoopback;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ibgpRemoteIpUnknown;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _ignoredForeignEndpoints;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _localIpUnknown;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _missingLocalIp;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _nonUniqueEndpoint;

      private SortedMap<String, SortedMap<String, SortedSet<Prefix>>> _remoteIpUnknown;

      public BgpSessionCheckAnswerElement() {
         _allBgpNeighbors = new TreeMap<>();
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

      public void add(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> neighborsByHostname,
            String hostname, String vrf, BgpNeighborSummary bgpNeighbor) {
         SortedMap<String, SortedSet<Prefix>> neighborsByVrf = neighborsByHostname
               .get(hostname);
         if (neighborsByVrf == null) {
            neighborsByVrf = new TreeMap<>();
            neighborsByHostname.put(hostname, neighborsByVrf);
         }
         SortedSet<Prefix> neighbors = neighborsByVrf.get(vrf);
         if (neighbors == null) {
            neighbors = new TreeSet<>();
            neighborsByVrf.put(vrf, neighbors);
         }
         Prefix prefix = bgpNeighbor.getPrefix();
         neighbors.add(prefix);
      }

      public void addToAll(
            SortedMap<String, SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>>> neighborsByHostname,
            String hostname, String vrf, BgpNeighborSummary bgpNeighbor) {
         SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>> neighborsByVrf = neighborsByHostname
               .get(hostname);
         if (neighborsByVrf == null) {
            neighborsByVrf = new TreeMap<>();
            neighborsByHostname.put(hostname, neighborsByVrf);
         }
         SortedMap<Prefix, BgpNeighborSummary> neighborsByPrefix = neighborsByVrf
               .get(vrf);
         if (neighborsByPrefix == null) {
            neighborsByPrefix = new TreeMap<>();
            neighborsByVrf.put(vrf, neighborsByPrefix);
         }
         Prefix prefix = bgpNeighbor.getPrefix();
         neighborsByPrefix.put(prefix, bgpNeighbor);
      }

      @JsonProperty(ALL_BGP_NEIGHBORS_VAR)
      public SortedMap<String, SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>>> getAllBgpNeighbors() {
         return _allBgpNeighbors;
      }

      @JsonProperty(BROKEN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getBroken() {
         return _broken;
      }

      @JsonProperty(EBGP_BROKEN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpBroken() {
         return _ebgpBroken;
      }

      @JsonProperty(EBGP_HALF_OPEN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpHalfOpen() {
         return _ebgpHalfOpen;
      }

      @JsonProperty(EBGP_LOCAL_IP_ON_LOOPBACK_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpLocalIpOnLoopback() {
         return _ebgpLocalIpOnLoopback;
      }

      @JsonProperty(EBGP_LOCAL_IP_UNKNOWN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpLocalIpUnknown() {
         return _ebgpLocalIpUnknown;
      }

      @JsonProperty(EBGP_MISSING_LOCAL_IP_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpMissingLocalIp() {
         return _ebgpMissingLocalIp;
      }

      @JsonProperty(EBGP_NON_UNIQUE_ENDPOINT_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpNonUniqueEndpoint() {
         return _ebgpNonUniqueEndpoint;
      }

      @JsonProperty(EBGP_REMOTE_IP_ON_LOOPBACK_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpRemoteIpOnLoopback() {
         return _ebgpRemoteIpOnLoopback;
      }

      @JsonProperty(EBGP_REMOTE_IP_UNKNOWN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getEbgpRemoteIpUnknown() {
         return _ebgpRemoteIpUnknown;
      }

      @JsonProperty(HALF_OPEN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getHalfOpen() {
         return _halfOpen;
      }

      @JsonProperty(IBGP_BROKEN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpBroken() {
         return _ibgpBroken;
      }

      @JsonProperty(IBGP_HALF_OPEN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpHalfOpen() {
         return _ibgpHalfOpen;
      }

      @JsonProperty(IBGP_LOCAL_IP_ON_NON_LOOPBACK_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpLocalIpOnNonLoopback() {
         return _ibgpLocalIpOnNonLoopback;
      }

      @JsonProperty(IBGP_LOCAL_IP_UNKNOWN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpLocalIpUnknown() {
         return _ibgpLocalIpUnknown;
      }

      @JsonProperty(IBGP_MISSING_LOCAL_IP_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpMissingLocalIp() {
         return _ibgpMissingLocalIp;
      }

      @JsonProperty(IBGP_NON_UNIQUE_ENDPOINT_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpNonUniqueEndpoint() {
         return _ibgpNonUniqueEndpoint;
      }

      @JsonProperty(IBGP_REMOTE_IP_ON_NON_LOOPBACK_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpRemoteIpOnNonLoopback() {
         return _ibgpRemoteIpOnNonLoopback;
      }

      @JsonProperty(IBGP_REMOTE_IP_UNKNOWN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIbgpRemoteIpUnknown() {
         return _ibgpRemoteIpUnknown;
      }

      @JsonProperty(IGNORED_FOREIGN_ENDPOINTS_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getIgnoredForeignEndpoints() {
         return _ignoredForeignEndpoints;
      }

      @JsonProperty(LOCAL_IP_UNKNOWN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getLocalIpUnknown() {
         return _localIpUnknown;
      }

      @JsonProperty(MISSING_LOCAL_IP_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getMissingLocalIp() {
         return _missingLocalIp;
      }

      @JsonProperty(NON_UNIQUE_ENDPOINT_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getNonUniqueEndpoint() {
         return _nonUniqueEndpoint;
      }

      @JsonProperty(REMOTE_IP_UNKNOWN_VAR)
      public SortedMap<String, SortedMap<String, SortedSet<Prefix>>> getRemoteIpUnknown() {
         return _remoteIpUnknown;
      }

      @Override
      public String prettyPrint() {
         StringBuilder sb = new StringBuilder();
         sb.append(prettyPrintCategory(_ebgpLocalIpOnLoopback,
               EBGP_LOCAL_IP_ON_LOOPBACK_VAR));
         sb.append(prettyPrintCategory(_ebgpRemoteIpOnLoopback,
               EBGP_REMOTE_IP_ON_LOOPBACK_VAR));
         sb.append(prettyPrintCategory(_halfOpen, HALF_OPEN_VAR));
         sb.append(prettyPrintCategory(_ibgpLocalIpOnNonLoopback,
               IBGP_LOCAL_IP_ON_NON_LOOPBACK_VAR));
         sb.append(prettyPrintCategory(_ibgpRemoteIpOnNonLoopback,
               IBGP_REMOTE_IP_ON_NON_LOOPBACK_VAR));
         sb.append(prettyPrintCategory(_localIpUnknown, LOCAL_IP_UNKNOWN_VAR));
         sb.append(prettyPrintCategory(_missingLocalIp, MISSING_LOCAL_IP_VAR));
         sb.append(prettyPrintCategory(_nonUniqueEndpoint,
               NON_UNIQUE_ENDPOINT_VAR));
         sb.append(
               prettyPrintCategory(_remoteIpUnknown, REMOTE_IP_UNKNOWN_VAR));
         return sb.toString();
      }

      private CharSequence prettyPrintCategory(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> category,
            String name) {
         StringBuilder sb = new StringBuilder();
         if (!category.isEmpty()) {
            sb.append(name + ":\n");
            for (Entry<String, SortedMap<String, SortedSet<Prefix>>> e1 : category
                  .entrySet()) {
               String hostname = e1.getKey();
               sb.append("  " + hostname + ":\n");
               SortedMap<String, SortedSet<Prefix>> neighborsByHostname = e1
                     .getValue();
               for (Entry<String, SortedSet<Prefix>> e2 : neighborsByHostname
                     .entrySet()) {
                  String vrf = e2.getKey();
                  sb.append("    " + vrf + ":\n");
                  SortedSet<Prefix> neighbors = e2.getValue();
                  for (Prefix prefix : neighbors) {
                     BgpNeighborSummary summary = _allBgpNeighbors.get(hostname)
                           .get(vrf).get(prefix);
                     int localAs = summary.getLocalAs();
                     Ip localIp = summary.getLocalIp();
                     int remoteAs = summary.getRemoteAs();
                     Ip remoteIp = summary.getRemoteIp();
                     String group = summary.getGroup();
                     if (group == null) {
                        group = "";
                     }
                     String description = summary.getDescription();
                     if (description == null) {
                        description = "";
                     }
                     sb.append(String.format(
                           "      remoteIp: %-15s   remoteAs: %-5d   localIp: %-15s   localAs: %-5d   group: %s   desc: %s\n",
                           remoteIp, remoteAs, localIp, localAs, group,
                           description));
                  }
               }
            }
         }
         return sb;
      }

      @JsonProperty(ALL_BGP_NEIGHBORS_VAR)
      public void setAllBgpNeighbors(
            SortedMap<String, SortedMap<String, SortedMap<Prefix, BgpNeighborSummary>>> allBgpNeighbors) {
         _allBgpNeighbors = allBgpNeighbors;
      }

      @JsonProperty(BROKEN_VAR)
      public void setBroken(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> broken) {
         _broken = broken;
      }

      @JsonProperty(EBGP_BROKEN_VAR)
      public void setEbgpBroken(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpBroken) {
         _ebgpBroken = ebgpBroken;
      }

      @JsonProperty(EBGP_HALF_OPEN_VAR)
      public void setEbgpHalfOpen(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpHalfOpen) {
         _ebgpHalfOpen = ebgpHalfOpen;
      }

      @JsonProperty(EBGP_LOCAL_IP_ON_LOOPBACK_VAR)
      public void setEbgpLocalIpOnLoopback(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpLocalIpOnLoopback) {
         _ebgpLocalIpOnLoopback = ebgpLocalIpOnLoopback;
      }

      @JsonProperty(EBGP_LOCAL_IP_UNKNOWN_VAR)
      public void setEbgpLocalIpUnknown(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpLocalIpUnknown) {
         _ebgpLocalIpUnknown = ebgpLocalIpUnknown;
      }

      @JsonProperty(EBGP_MISSING_LOCAL_IP_VAR)
      public void setEbgpMissingLocalIp(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpMissingLocalIp) {
         _ebgpMissingLocalIp = ebgpMissingLocalIp;
      }

      @JsonProperty(EBGP_NON_UNIQUE_ENDPOINT_VAR)
      public void setEbgpNonUniqueEndpoint(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpNonUniqueEndpoint) {
         _ebgpNonUniqueEndpoint = ebgpNonUniqueEndpoint;
      }

      @JsonProperty(EBGP_REMOTE_IP_ON_LOOPBACK_VAR)
      public void setEbgpRemoteIpOnLoopback(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpRemoteIpOnLoopback) {
         _ebgpRemoteIpOnLoopback = ebgpRemoteIpOnLoopback;
      }

      @JsonProperty(EBGP_REMOTE_IP_UNKNOWN_VAR)
      public void setEbgpRemoteIpUnknown(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ebgpRemoteIpUnknown) {
         _ebgpRemoteIpUnknown = ebgpRemoteIpUnknown;
      }

      @JsonProperty(HALF_OPEN_VAR)
      public void setHalfOpen(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> halfOpen) {
         _halfOpen = halfOpen;
      }

      @JsonProperty(IBGP_BROKEN_VAR)
      public void setIbgpBroken(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpBroken) {
         _ibgpBroken = ibgpBroken;
      }

      @JsonProperty(IBGP_HALF_OPEN_VAR)
      public void setIbgpHalfOpen(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpHalfOpen) {
         _ibgpHalfOpen = ibgpHalfOpen;
      }

      @JsonProperty(IBGP_LOCAL_IP_ON_NON_LOOPBACK_VAR)
      public void setIbgpLocalIpOnNonLoopback(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpLocalIpOnNonLoopback) {
         _ibgpLocalIpOnNonLoopback = ibgpLocalIpOnNonLoopback;
      }

      @JsonProperty(IBGP_LOCAL_IP_UNKNOWN_VAR)
      public void setIbgpLocalIpUnknown(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpLocalIpUnknown) {
         _ibgpLocalIpUnknown = ibgpLocalIpUnknown;
      }

      @JsonProperty(IBGP_MISSING_LOCAL_IP_VAR)
      public void setIbgpMissingLocalIp(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpMissingLocalIp) {
         _ibgpMissingLocalIp = ibgpMissingLocalIp;
      }

      @JsonProperty(IBGP_NON_UNIQUE_ENDPOINT_VAR)
      public void setIbgpNonUniqueEndpoint(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpNonUniqueEndpoint) {
         _ibgpNonUniqueEndpoint = ibgpNonUniqueEndpoint;
      }

      @JsonProperty(IBGP_REMOTE_IP_ON_NON_LOOPBACK_VAR)
      public void setIbgpRemoteIpOnNonLoopback(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpRemoteIpOnNonLoopback) {
         _ibgpRemoteIpOnNonLoopback = ibgpRemoteIpOnNonLoopback;
      }

      @JsonProperty(IBGP_REMOTE_IP_UNKNOWN_VAR)
      public void setIbgpRemoteIpUnknown(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ibgpRemoteIpUnknown) {
         _ibgpRemoteIpUnknown = ibgpRemoteIpUnknown;
      }

      @JsonProperty(IGNORED_FOREIGN_ENDPOINTS_VAR)
      public void setIgnoredForeignEndpoints(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> ignoredForeignEndpoints) {
         _ignoredForeignEndpoints = ignoredForeignEndpoints;
      }

      @JsonProperty(LOCAL_IP_UNKNOWN_VAR)
      public void setLocalIpUnknown(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> localIpUnknown) {
         _localIpUnknown = localIpUnknown;
      }

      @JsonProperty(MISSING_LOCAL_IP_VAR)
      public void setMissingLocalIp(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> missingLocalIp) {
         _missingLocalIp = missingLocalIp;
      }

      @JsonProperty(NON_UNIQUE_ENDPOINT_VAR)
      public void setNonUniqueEndpoint(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> nonUniqueEndpoint) {
         _nonUniqueEndpoint = nonUniqueEndpoint;
      }

      @JsonProperty(REMOTE_IP_UNKNOWN_VAR)
      public void setRemoteIpUnknown(
            SortedMap<String, SortedMap<String, SortedSet<Prefix>>> remoteIpUnknown) {
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
         for (Configuration co : configurations.values()) {
            String hostname = co.getHostname();
            if (!node1Regex.matcher(co.getHostname()).matches()) {
               continue;
            }
            for (Vrf vrf : co.getVrfs().values()) {
               String vrfName = vrf.getName();
               BgpProcess proc = vrf.getBgpProcess();

               if (proc != null) {
                  for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
                     BgpNeighborSummary bgpNeighborSummary = new BgpNeighborSummary(
                           bgpNeighbor);
                     answerElement.addToAll(answerElement.getAllBgpNeighbors(),
                           hostname, vrfName, bgpNeighborSummary);
                     boolean foreign = bgpNeighbor.getGroup() != null
                           && question.getForeignBgpGroups()
                                 .contains(bgpNeighbor.getGroup());
                     boolean ebgp = !bgpNeighbor.getRemoteAs()
                           .equals(bgpNeighbor.getLocalAs());
                     boolean ebgpMultihop = bgpNeighbor.getEbgpMultihop();
                     Ip localIp = bgpNeighbor.getLocalIp();
                     Ip remoteIp = bgpNeighbor.getAddress();
                     if (bgpNeighbor.getPrefix().getPrefixLength() != 32) {
                        continue;
                     }
                     if (ebgp) {
                        if (!ebgpMultihop && loopbackIps.contains(localIp)) {
                           answerElement.add(
                                 answerElement.getEbgpLocalIpOnLoopback(),
                                 hostname, vrfName, bgpNeighborSummary);
                        }
                        if (localIp == null) {
                           answerElement.add(answerElement.getBroken(),
                                 hostname, vrfName, bgpNeighborSummary);
                           answerElement.add(answerElement.getMissingLocalIp(),
                                 hostname, vrfName, bgpNeighborSummary);
                           answerElement.add(answerElement.getEbgpBroken(),
                                 hostname, vrfName, bgpNeighborSummary);
                           answerElement.add(
                                 answerElement.getEbgpMissingLocalIp(),
                                 hostname, vrfName, bgpNeighborSummary);
                        }
                     }
                     else {
                        // ibgp
                        if (!loopbackIps.contains(localIp)) {
                           answerElement.add(
                                 answerElement.getIbgpLocalIpOnNonLoopback(),
                                 hostname, vrfName, bgpNeighborSummary);
                        }
                        if (localIp == null) {
                           answerElement.add(answerElement.getBroken(),
                                 hostname, vrfName, bgpNeighborSummary);
                           answerElement.add(answerElement.getMissingLocalIp(),
                                 hostname, vrfName, bgpNeighborSummary);
                           answerElement.add(answerElement.getIbgpBroken(),
                                 hostname, vrfName, bgpNeighborSummary);
                           answerElement.add(
                                 answerElement.getIbgpMissingLocalIp(),
                                 hostname, vrfName, bgpNeighborSummary);
                        }
                     }
                     if (foreign) {
                        answerElement.add(
                              answerElement.getIgnoredForeignEndpoints(),
                              hostname, vrfName, bgpNeighborSummary);
                     }
                     else {
                        // not foreign
                        if (ebgp) {
                           if (localIp != null
                                 && !allInterfaceIps.contains(localIp)) {
                              answerElement.add(answerElement.getBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getLocalIpUnknown(), hostname,
                                    vrfName, bgpNeighborSummary);
                              answerElement.add(answerElement.getEbgpBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getEbgpLocalIpUnknown(),
                                    hostname, vrfName, bgpNeighborSummary);
                           }
                           if (!allInterfaceIps.contains(remoteIp)) {
                              answerElement.add(answerElement.getBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getRemoteIpUnknown(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(answerElement.getEbgpBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getEbgpRemoteIpUnknown(),
                                    hostname, vrfName, bgpNeighborSummary);
                           }
                           else {
                              if (!ebgpMultihop
                                    && loopbackIps.contains(remoteIp)
                                    && node2RegexMatchesIp(remoteIp, ipOwners,
                                          node2Regex)) {
                                 answerElement.add(
                                       answerElement
                                             .getEbgpRemoteIpOnLoopback(),
                                       hostname, vrfName, bgpNeighborSummary);
                              }
                           }
                           // check half open
                           if (localIp != null
                                 && allInterfaceIps.contains(remoteIp)
                                 && node2RegexMatchesIp(remoteIp, ipOwners,
                                       node2Regex)) {
                              if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                                 answerElement.add(answerElement.getBroken(),
                                       hostname, vrfName, bgpNeighborSummary);
                                 answerElement.add(answerElement.getHalfOpen(),
                                       hostname, vrfName, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getEbgpBroken(), hostname,
                                       vrfName, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getEbgpHalfOpen(),
                                       hostname, vrfName, bgpNeighborSummary);
                              }
                              else if (bgpNeighbor
                                    .getCandidateRemoteBgpNeighbors()
                                    .size() != 1) {
                                 answerElement.add(
                                       answerElement.getNonUniqueEndpoint(),
                                       hostname, vrfName, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getEbgpNonUniqueEndpoint(),
                                       hostname, vrfName, bgpNeighborSummary);
                              }
                           }
                        }
                        else {
                           // ibgp
                           if (localIp != null
                                 && !allInterfaceIps.contains(localIp)) {
                              answerElement.add(answerElement.getBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(answerElement.getIbgpBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getIbgpLocalIpUnknown(),
                                    hostname, vrfName, bgpNeighborSummary);
                           }
                           if (!allInterfaceIps.contains(remoteIp)) {
                              answerElement.add(answerElement.getBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(answerElement.getIbgpBroken(),
                                    hostname, vrfName, bgpNeighborSummary);
                              answerElement.add(
                                    answerElement.getIbgpRemoteIpUnknown(),
                                    hostname, vrfName, bgpNeighborSummary);
                           }
                           else {
                              if (!loopbackIps.contains(remoteIp)
                                    && node2RegexMatchesIp(remoteIp, ipOwners,
                                          node2Regex)) {
                                 answerElement.add(
                                       answerElement
                                             .getIbgpRemoteIpOnNonLoopback(),
                                       hostname, vrfName, bgpNeighborSummary);
                              }
                           }
                           if (localIp != null
                                 && allInterfaceIps.contains(remoteIp)
                                 && node2RegexMatchesIp(remoteIp, ipOwners,
                                       node2Regex)) {
                              if (bgpNeighbor.getRemoteBgpNeighbor() == null) {
                                 answerElement.add(answerElement.getBroken(),
                                       hostname, vrfName, bgpNeighborSummary);
                                 answerElement.add(answerElement.getHalfOpen(),
                                       hostname, vrfName, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getIbgpBroken(), hostname,
                                       vrfName, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getIbgpHalfOpen(),
                                       hostname, vrfName, bgpNeighborSummary);
                              }
                              else if (bgpNeighbor
                                    .getCandidateRemoteBgpNeighbors()
                                    .size() != 1) {
                                 answerElement.add(
                                       answerElement.getNonUniqueEndpoint(),
                                       hostname, vrfName, bgpNeighborSummary);
                                 answerElement.add(
                                       answerElement.getIbgpNonUniqueEndpoint(),
                                       hostname, vrfName, bgpNeighborSummary);
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

      private SortedSet<String> _foreignBgpGroups;

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
      public SortedSet<String> getForeignBgpGroups() {
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

      public void setForeignBgpGroups(SortedSet<String> foreignBgpGroups) {
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
                  setForeignBgpGroups(new TreeSet<>(new ObjectMapper()
                        .<Set<String>> readValue(parameters.getString(paramKey),
                              new TypeReference<Set<String>>() {
                              })));
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
