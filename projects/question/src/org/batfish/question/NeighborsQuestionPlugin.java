package org.batfish.question;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.VerboseEdge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.OspfNeighbor;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.IpEdge;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NeighborsQuestionPlugin extends QuestionPlugin {

   public static class NeighborsAnswerElement implements AnswerElement {

      private static final String EBGP_NEIGHBORS_VAR = "ebgpNeighbors";

      private static final String IBGP_NEIGHBORS_VAR = "ibgpNeighbors";

      private final static String LAN_NEIGHBORS_VAR = "lanNeighbors";

      private final static String OSPF_NEIGHBORS_VAR = "ospfNeighbors";

      private final static String VERBOSE_LAN_NEIGHBORS_VAR = "verboseLanNeighbors";

      private SortedSet<IpEdge> _ebgpNeighbors;

      private SortedSet<IpEdge> _ibgpNeighbors;

      private SortedSet<Edge> _lanNeighbors;

      private SortedSet<IpEdge> _ospfNeighbors;

      private SortedSet<VerboseEdge> _verboseLanNeighbors;

      public void addLanEdge(Edge edge) {
         _lanNeighbors.add(edge);
      }

      @JsonProperty(EBGP_NEIGHBORS_VAR)
      public SortedSet<IpEdge> getEbgpNeighbors() {
         return _ebgpNeighbors;
      }

      @JsonProperty(IBGP_NEIGHBORS_VAR)
      public SortedSet<IpEdge> getIbgpNeighbors() {
         return _ibgpNeighbors;
      }

      @JsonProperty(LAN_NEIGHBORS_VAR)
      public SortedSet<Edge> getLanNeighbors() {
         return _lanNeighbors;
      }

      @JsonProperty(OSPF_NEIGHBORS_VAR)
      public SortedSet<IpEdge> getOspfNeighbors() {
         return _ospfNeighbors;
      }

      @JsonProperty(VERBOSE_LAN_NEIGHBORS_VAR)
      public SortedSet<VerboseEdge> getVerboseLanNeighbors() {
         return _verboseLanNeighbors;
      }

      public void initEbgpNeighbors() {
         _ebgpNeighbors = new TreeSet<>();
      }

      public void initIbgpNeighbors() {
         _ibgpNeighbors = new TreeSet<>();
      }

      public void initLanNeighbors() {
         _lanNeighbors = new TreeSet<>();
      }

      public void initOspfNeighbors() {
         _ospfNeighbors = new TreeSet<>();
      }

      public void initVerboseLanNeighbors() {
         _verboseLanNeighbors = new TreeSet<>();
      }

      @Override
      public String prettyPrint() {
         StringBuilder sb = new StringBuilder("Results for neighbors\n");

         if (_lanNeighbors != null) {
            sb.append("  LAN neighbors\n");
            for (Edge edge : _lanNeighbors) {
               sb.append("    " + edge.toString() + "\n");
            }
         }

         if (_verboseLanNeighbors != null) {
            sb.append("  LAN neighbors\n");
            for (VerboseEdge edge : _verboseLanNeighbors) {
               sb.append("    " + edge.toString() + "\n");
            }
         }

         if (_ebgpNeighbors != null) {
            sb.append("  eBGP Neighbors\n");
            for (IpEdge ipEdge : _ebgpNeighbors) {
               sb.append("    " + ipEdge.toString() + "\n");
            }
         }

         if (_ibgpNeighbors != null) {
            sb.append("  iBGP Neighbors\n");
            for (IpEdge ipEdge : _ibgpNeighbors) {
               sb.append("    " + ipEdge.toString() + "\n");
            }
         }

         if (_ospfNeighbors != null) {
            sb.append("  OSPF Neighbors\n");
            for (IpEdge ipEdge : _ospfNeighbors) {
               sb.append("    " + ipEdge.toString() + "\n");
            }
         }

         return sb.toString();
      }

      @JsonProperty(EBGP_NEIGHBORS_VAR)
      public void setEbgpNeighbors(SortedSet<IpEdge> ebgpNeighbors) {
         _ebgpNeighbors = ebgpNeighbors;
      }

      @JsonProperty(IBGP_NEIGHBORS_VAR)
      public void setIbgpNeighbors(SortedSet<IpEdge> ibgpNeighbors) {
         _ibgpNeighbors = ibgpNeighbors;
      }

      @JsonProperty(LAN_NEIGHBORS_VAR)
      public void setLanNeighbors(SortedSet<Edge> lanNeighbors) {
         _lanNeighbors = lanNeighbors;
      }

      @JsonProperty(OSPF_NEIGHBORS_VAR)
      public void setOspfNeighbors(SortedSet<IpEdge> ospfNeighbors) {
         _ospfNeighbors = ospfNeighbors;
      }

      @JsonProperty(VERBOSE_LAN_NEIGHBORS_VAR)
      public void setVerboseLanNeighbors(
            SortedSet<VerboseEdge> verboseLanNeighbors) {
         _verboseLanNeighbors = verboseLanNeighbors;
      }

   }

   public static class NeighborsAnswerer extends Answerer {

      private boolean _remoteBgpNeighborsInitialized;

      private boolean _remoteOspfNeighborsInitialized;

      Topology _topology;

      public NeighborsAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {
         NeighborsQuestion question = (NeighborsQuestion) _question;
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

         NeighborsAnswerElement answerElement = new NeighborsAnswerElement();

         Map<String, Configuration> configurations = _batfish
               .loadConfigurations();

         // for (NeighborType nType : question.getNeighborTypes()) {
         // switch (nType) {
         // case EBGP:
         // answerElement.initEbgpNeighbors();
         // initRemoteBgpNeighbors(_batfish, configurations);
         // break;
         // case IBGP:
         // answerElement.initIbgpNeighbors();
         // initRemoteBgpNeighbors(_batfish, configurations);
         // break;
         // case LAN:
         // answerElement.initLanNeighbors();
         // break;
         // default:
         // throw new BatfishException("Unsupported NeighborType: "
         // + nType.toString());
         //
         // }
         // }

         if (question.getNeighborTypes().contains(NeighborType.OSPF)) {
            answerElement.initOspfNeighbors();
            initTopology(configurations);
            initRemoteOspfNeighbors(_batfish, configurations, _topology);
            for (Configuration c : configurations.values()) {
               String hostname = c.getHostname();
               for (Vrf vrf : c.getVrfs().values()) {
                  OspfProcess proc = vrf.getOspfProcess();
                  if (proc != null) {
                     for (OspfNeighbor ospfNeighbor : proc.getOspfNeighbors()
                           .values()) {
                        OspfNeighbor remoteOspfNeighbor = ospfNeighbor
                              .getRemoteOspfNeighbor();
                        if (remoteOspfNeighbor != null) {
                           Configuration remoteHost = remoteOspfNeighbor
                                 .getOwner();
                           String remoteHostname = remoteHost.getHostname();
                           Matcher node1Matcher = node1Regex.matcher(hostname);
                           Matcher node2Matcher = node2Regex
                                 .matcher(remoteHostname);
                           if (node1Matcher.matches()
                                 && node2Matcher.matches()) {
                              Ip localIp = ospfNeighbor.getLocalIp();
                              Ip remoteIp = remoteOspfNeighbor.getLocalIp();
                              answerElement.getOspfNeighbors()
                                    .add(new IpEdge(hostname, localIp,
                                          remoteHostname, remoteIp));
                           }
                        }
                     }
                  }
               }
            }
         }

         if (question.getNeighborTypes().contains(NeighborType.EBGP)) {
            answerElement.initEbgpNeighbors();
            initRemoteBgpNeighbors(_batfish, configurations);
            for (Configuration c : configurations.values()) {
               String hostname = c.getHostname();
               for (Vrf vrf : c.getVrfs().values()) {
                  BgpProcess proc = vrf.getBgpProcess();
                  if (proc != null) {
                     for (BgpNeighbor bgpNeighbor : proc.getNeighbors()
                           .values()) {
                        BgpNeighbor remoteBgpNeighbor = bgpNeighbor
                              .getRemoteBgpNeighbor();
                        if (remoteBgpNeighbor != null) {
                           boolean ebgp = !bgpNeighbor.getRemoteAs()
                                 .equals(bgpNeighbor.getLocalAs());
                           if (ebgp) {
                              Configuration remoteHost = remoteBgpNeighbor
                                    .getOwner();
                              String remoteHostname = remoteHost.getHostname();
                              Matcher node1Matcher = node1Regex
                                    .matcher(hostname);
                              Matcher node2Matcher = node2Regex
                                    .matcher(remoteHostname);
                              if (node1Matcher.matches()
                                    && node2Matcher.matches()) {
                                 Ip localIp = bgpNeighbor.getLocalIp();
                                 Ip remoteIp = remoteBgpNeighbor.getLocalIp();
                                 answerElement.getEbgpNeighbors()
                                       .add(new IpEdge(hostname, localIp,
                                             remoteHostname, remoteIp));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (question.getNeighborTypes().contains(NeighborType.IBGP)) {
            answerElement.initIbgpNeighbors();
            initRemoteBgpNeighbors(_batfish, configurations);
            for (Configuration c : configurations.values()) {
               String hostname = c.getHostname();
               for (Vrf vrf : c.getVrfs().values()) {
                  BgpProcess proc = vrf.getBgpProcess();
                  if (proc != null) {
                     for (BgpNeighbor bgpNeighbor : proc.getNeighbors()
                           .values()) {
                        BgpNeighbor remoteBgpNeighbor = bgpNeighbor
                              .getRemoteBgpNeighbor();
                        if (remoteBgpNeighbor != null) {
                           boolean ibgp = bgpNeighbor.getRemoteAs()
                                 .equals(bgpNeighbor.getLocalAs());
                           if (ibgp) {
                              Configuration remoteHost = remoteBgpNeighbor
                                    .getOwner();
                              String remoteHostname = remoteHost.getHostname();
                              Matcher node1Matcher = node1Regex
                                    .matcher(hostname);
                              Matcher node2Matcher = node2Regex
                                    .matcher(remoteHostname);
                              if (node1Matcher.matches()
                                    && node2Matcher.matches()) {
                                 Ip localIp = bgpNeighbor.getLocalIp();
                                 Ip remoteIp = remoteBgpNeighbor.getLocalIp();
                                 answerElement.getIbgpNeighbors()
                                       .add(new IpEdge(hostname, localIp,
                                             remoteHostname, remoteIp));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (question.getNeighborTypes().isEmpty()
               || question.getNeighborTypes().contains(NeighborType.LAN)) {
            initTopology(configurations);
            SortedSet<Edge> matchingEdges = new TreeSet<>();
            for (Edge edge : _topology.getEdges()) {
               Matcher node1Matcher = node1Regex.matcher(edge.getNode1());
               Matcher node2Matcher = node2Regex.matcher(edge.getNode2());
               if (node1Matcher.matches() && node2Matcher.matches()) {
                  matchingEdges.add(edge);
               }
            }
            if (!question.getVerbose()) {
               answerElement.setLanNeighbors(matchingEdges);
            }
            else {
               SortedSet<VerboseEdge> vMatchingEdges = new TreeSet<>();
               for (Edge edge : matchingEdges) {
                  Configuration n1 = configurations.get(edge.getNode1());
                  Interface i1 = n1.getInterfaces().get(edge.getInt1());
                  Configuration n2 = configurations.get(edge.getNode2());
                  Interface i2 = n2.getInterfaces().get(edge.getInt2());
                  vMatchingEdges.add(new VerboseEdge(n1, i1, n2, i2, edge));
               }
               answerElement.setVerboseLanNeighbors(vMatchingEdges);
            }
         }

         return answerElement;
      }

      private void initRemoteBgpNeighbors(IBatfish batfish,
            Map<String, Configuration> configurations) {
         if (!_remoteBgpNeighborsInitialized) {
            Map<Ip, Set<String>> ipOwners = _batfish
                  .computeIpOwners(configurations, true);
            batfish.initRemoteBgpNeighbors(configurations, ipOwners);
            _remoteBgpNeighborsInitialized = true;
         }
      }

      private void initRemoteOspfNeighbors(IBatfish batfish,
            Map<String, Configuration> configurations, Topology topology) {
         if (!_remoteOspfNeighborsInitialized) {
            Map<Ip, Set<String>> ipOwners = _batfish
                  .computeIpOwners(configurations, true);
            batfish.initRemoteOspfNeighbors(configurations, ipOwners, topology);
            _remoteOspfNeighborsInitialized = true;
         }
      }

      private void initTopology(Map<String, Configuration> configurations) {
         if (_topology == null) {
            _topology = _batfish.computeTopology(configurations);
         }
      }

   }

   // <question_page_comment>
   /**
    * Lists neighbor relationships in the testrig.
    * <p>
    * Details coming
    *
    * @type Neighbors multifile
    *
    * @param neighborType
    *           The type(s) of neighbor relationships to focus on among (eBGP,
    *           iBGP, IP). Default is IP.
    * @param node1Regex
    *           Regular expression to match the nodes names for one end of pair.
    *           Default is '.*' (all nodes).
    * @param node2Regex
    *           Regular expression to match the nodes names for the other end of
    *           the pair. Default is '.*' (all nodes).
    * @param verbose
    *           Boolean indicating whether full information about the
    *           nodes/interfaces that make up each neighbor pair is requested.
    *           Default is false, indicating that only the names of
    *           nodes/interfaces is returned.
    *
    * @example bf_answer("Neighbors", neighborType=["ebgp", "ibgp"]
    *          node1Regex="as1.*", node2Regex="as2.*") Shows all eBGP and iBGP
    *          neighbor relationships between nodes that start with as1 and
    *          those that start with as2.
    *
    */
   public static class NeighborsQuestion extends Question {

      private static final String NEIGHBOR_TYPES_VAR = "neighborTypes";

      private static final String NODE1_REGEX_VAR = "node1Regex";

      private static final String NODE2_REGEX_VAR = "node2Regex";

      private static final String VERBOSE_VAR = "verbose";

      private SortedSet<NeighborType> _neighborTypes;

      private String _node1Regex;

      private String _node2Regex;

      private boolean _verbose;

      public NeighborsQuestion() {
         _node1Regex = ".*";
         _node2Regex = ".*";
         _neighborTypes = new TreeSet<>();
         _verbose = false;
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "neighbors";
      }

      @JsonProperty(NEIGHBOR_TYPES_VAR)
      public SortedSet<NeighborType> getNeighborTypes() {
         return _neighborTypes;
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

      @JsonProperty(VERBOSE_VAR)
      public boolean getVerbose() {
         return _verbose;
      }

      @Override
      public String prettyPrint() {
         try {
            String retString = String.format(
                  "neighbors %s%s=%s | %s=%s | %s=%s | %s=%b",
                  prettyPrintBase(), NODE1_REGEX_VAR, _node1Regex,
                  NODE2_REGEX_VAR, _node2Regex, NEIGHBOR_TYPES_VAR,
                  _neighborTypes.toString(), VERBOSE_VAR, _verbose);
            return retString;
         }
         catch (Exception e) {
            try {
               return "Pretty printing failed. Printing Json\n"
                     + toJsonString();
            }
            catch (BatfishException e1) {
               throw new BatfishException(
                     "Both pretty and json printing failed\n");
            }
         }

      }

      @JsonProperty(NEIGHBOR_TYPES_VAR)
      public void setNeighborTypes(SortedSet<NeighborType> neighborTypes) {
         _neighborTypes = neighborTypes;
      }

      @JsonProperty(NODE1_REGEX_VAR)
      public void setNode1Regex(String regex) {
         _node1Regex = regex;
      }

      @JsonProperty(NODE2_REGEX_VAR)
      public void setNode2Regex(String regex) {
         _node2Regex = regex;
      }

      @JsonProperty(VERBOSE_VAR)
      public void setVerbose(boolean verbose) {
         _verbose = verbose;
      }
   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new NeighborsAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new NeighborsQuestion();
   }

}
