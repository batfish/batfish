package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
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
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.OspfNeighbor;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.RoleEdge;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.VerboseEdge;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.IpEdge;
import org.batfish.datamodel.collections.VerboseBgpEdge;
import org.batfish.datamodel.collections.VerboseOspfEdge;
import org.batfish.datamodel.questions.Question;

public class NeighborsQuestionPlugin extends QuestionPlugin {

  public enum EdgeStyle {
    ROLE("role"),
    SUMMARY("summary"),
    VERBOSE("verbose");

    private static final Map<String, EdgeStyle> _map = buildMap();

    private static synchronized Map<String, EdgeStyle> buildMap() {
      Map<String, EdgeStyle> map = new HashMap<>();
      for (EdgeStyle value : EdgeStyle.values()) {
        String name = value._name;
        map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
    }

    @JsonCreator
    public static EdgeStyle fromName(String name) {
      EdgeStyle instance = _map.get(name.toLowerCase());
      if (instance == null) {
        throw new BatfishException(
            "No " + EdgeStyle.class.getSimpleName() + " with name: '" + name + "'");
      }
      return instance;
    }

    private final String _name;

    private EdgeStyle(String name) {
      _name = name;
    }

    @JsonValue
    public String edgeStyleName() {
      return _name;
    }
  }

  public static class NeighborsAnswerElement implements AnswerElement {

    private static final String EBGP_NEIGHBORS_VAR = "ebgpNeighbors";

    private static final String IBGP_NEIGHBORS_VAR = "ibgpNeighbors";

    private static final String LAN_NEIGHBORS_VAR = "lanNeighbors";

    private static final String OSPF_NEIGHBORS_VAR = "ospfNeighbors";

    private static final String ROLE_EBGP_NEIGHBORS_VAR = "roleEbgpNeighbors";

    private static final String ROLE_IBGP_NEIGHBORS_VAR = "roleIbgpNeighbors";

    private static final String ROLE_LAN_NEIGHBORS_VAR = "roleLanNeighbors";

    private static final String ROLE_OSPF_NEIGHBORS_VAR = "roleOspfNeighbors";

    private static final String VERBOSE_EBGP_NEIGHBORS_VAR = "verboseEbgpNeighbors";

    private static final String VERBOSE_IBGP_NEIGHBORS_VAR = "verboseIbgpNeighbors";

    private static final String VERBOSE_LAN_NEIGHBORS_VAR = "verboseLanNeighbors";

    private static final String VERBOSE_OSPF_NEIGHBORS_VAR = "verboseOspfNeighbors";

    private SortedSet<IpEdge> _ebgpNeighbors;

    private SortedSet<IpEdge> _ibgpNeighbors;

    private SortedSet<Edge> _lanNeighbors;

    private SortedSet<IpEdge> _ospfNeighbors;

    private SortedSet<RoleEdge> _roleEbgpNeighbors;

    private SortedSet<RoleEdge> _roleIbgpNeighbors;

    private SortedSet<RoleEdge> _roleLanNeighbors;

    private SortedSet<RoleEdge> _roleOspfNeighbors;

    private SortedSet<VerboseBgpEdge> _verboseEbgpNeighbors;

    private SortedSet<VerboseBgpEdge> _verboseIbgpNeighbors;

    private SortedSet<VerboseEdge> _verboseLanNeighbors;

    private SortedSet<VerboseOspfEdge> _verboseOspfNeighbors;

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

    @JsonProperty(ROLE_EBGP_NEIGHBORS_VAR)
    public SortedSet<RoleEdge> getRoleEbgpNeighbors() {
      return _roleEbgpNeighbors;
    }

    @JsonProperty(ROLE_IBGP_NEIGHBORS_VAR)
    public SortedSet<RoleEdge> getRoleIbgpNeighbors() {
      return _roleIbgpNeighbors;
    }

    @JsonProperty(ROLE_LAN_NEIGHBORS_VAR)
    public SortedSet<RoleEdge> getRoleLanNeighbors() {
      return _roleLanNeighbors;
    }

    @JsonProperty(ROLE_OSPF_NEIGHBORS_VAR)
    public SortedSet<RoleEdge> getRoleOspfNeighbors() {
      return _roleOspfNeighbors;
    }

    @JsonProperty(VERBOSE_EBGP_NEIGHBORS_VAR)
    public SortedSet<VerboseBgpEdge> getVerboseEbgpNeighbors() {
      return _verboseEbgpNeighbors;
    }

    @JsonProperty(VERBOSE_IBGP_NEIGHBORS_VAR)
    public SortedSet<VerboseBgpEdge> getVerboseIbgpNeighbors() {
      return _verboseIbgpNeighbors;
    }

    @JsonProperty(VERBOSE_LAN_NEIGHBORS_VAR)
    public SortedSet<VerboseEdge> getVerboseLanNeighbors() {
      return _verboseLanNeighbors;
    }

    @JsonProperty(VERBOSE_OSPF_NEIGHBORS_VAR)
    public SortedSet<VerboseOspfEdge> getVerboseOspfNeighbors() {
      return _verboseOspfNeighbors;
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

      if (_roleLanNeighbors != null) {
        sb.append("  LAN neighbors\n");
        for (RoleEdge edge : _roleLanNeighbors) {
          sb.append("    " + edge.toString() + "\n");
        }
      }

      if (_ebgpNeighbors != null) {
        sb.append("  eBGP Neighbors\n");
        for (IpEdge ipEdge : _ebgpNeighbors) {
          sb.append("    " + ipEdge.toString() + "\n");
        }
      }

      if (_verboseEbgpNeighbors != null) {
        sb.append("  eBGP neighbors\n");
        for (VerboseBgpEdge edge : _verboseEbgpNeighbors) {
          sb.append("    " + edge.toString() + "\n");
        }
      }

      if (_roleEbgpNeighbors != null) {
        sb.append("  eBGP neighbors\n");
        for (RoleEdge edge : _roleEbgpNeighbors) {
          sb.append("    " + edge.toString() + "\n");
        }
      }

      if (_ibgpNeighbors != null) {
        sb.append("  iBGP Neighbors\n");
        for (IpEdge ipEdge : _ibgpNeighbors) {
          sb.append("    " + ipEdge.toString() + "\n");
        }
      }

      if (_verboseIbgpNeighbors != null) {
        sb.append("  iBGP neighbors\n");
        for (VerboseBgpEdge edge : _verboseIbgpNeighbors) {
          sb.append("    " + edge.toString() + "\n");
        }
      }

      if (_roleIbgpNeighbors != null) {
        sb.append("  iBGP neighbors\n");
        for (RoleEdge edge : _roleIbgpNeighbors) {
          sb.append("    " + edge.toString() + "\n");
        }
      }

      if (_ospfNeighbors != null) {
        sb.append("  OSPF Neighbors\n");
        for (IpEdge ipEdge : _ospfNeighbors) {
          sb.append("    " + ipEdge.toString() + "\n");
        }
      }

      if (_verboseOspfNeighbors != null) {
        sb.append("  OSPF neighbors\n");
        for (VerboseOspfEdge edge : _verboseOspfNeighbors) {
          sb.append("    " + edge.toString() + "\n");
        }
      }

      if (_roleOspfNeighbors != null) {
        sb.append("  OSPF neighbors\n");
        for (RoleEdge edge : _roleOspfNeighbors) {
          sb.append("    " + edge.toString() + "\n");
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

    @JsonProperty(ROLE_EBGP_NEIGHBORS_VAR)
    public void setRoleEbgpNeighbors(SortedSet<RoleEdge> roleEbgpNeighbors) {
      _roleEbgpNeighbors = roleEbgpNeighbors;
    }

    @JsonProperty(ROLE_IBGP_NEIGHBORS_VAR)
    public void setRoleIbgpNeighbors(SortedSet<RoleEdge> roleIbgpNeighbors) {
      _roleIbgpNeighbors = roleIbgpNeighbors;
    }

    @JsonProperty(ROLE_LAN_NEIGHBORS_VAR)
    public void setRoleLanNeighbors(SortedSet<RoleEdge> roleLanNeighbors) {
      _roleLanNeighbors = roleLanNeighbors;
    }

    @JsonProperty(ROLE_OSPF_NEIGHBORS_VAR)
    public void setRoleOspfNeighbors(SortedSet<RoleEdge> roleOspfNeighbors) {
      _roleOspfNeighbors = roleOspfNeighbors;
    }

    @JsonProperty(VERBOSE_EBGP_NEIGHBORS_VAR)
    public void setVerboseEbgpNeighbors(SortedSet<VerboseBgpEdge> verboseEbgpNeighbors) {
      _verboseEbgpNeighbors = verboseEbgpNeighbors;
    }

    @JsonProperty(VERBOSE_IBGP_NEIGHBORS_VAR)
    public void setVerboseIbgpNeighbors(SortedSet<VerboseBgpEdge> verboseIbgpNeighbors) {
      _verboseIbgpNeighbors = verboseIbgpNeighbors;
    }

    @JsonProperty(VERBOSE_LAN_NEIGHBORS_VAR)
    public void setVerboseLanNeighbors(SortedSet<VerboseEdge> verboseLanNeighbors) {
      _verboseLanNeighbors = verboseLanNeighbors;
    }

    @JsonProperty(VERBOSE_OSPF_NEIGHBORS_VAR)
    public void setVerboseOspfNeighbors(SortedSet<VerboseOspfEdge> verboseOspfNeighbors) {
      _verboseOspfNeighbors = verboseOspfNeighbors;
    }
  }

  public static class NeighborsAnswerer extends Answerer {

    private boolean _remoteBgpNeighborsInitialized;

    private boolean _remoteOspfNeighborsInitialized;

    Topology _topology;

    private SortedMap<String, SortedSet<String>> _nodeRolesMap;

    public NeighborsAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public NeighborsAnswerElement answer() {
      NeighborsQuestion question = (NeighborsQuestion) _question;
      Pattern node1Regex;
      Pattern node2Regex;

      try {
        node1Regex = Pattern.compile(question.getNode1Regex());
        node2Regex = Pattern.compile(question.getNode2Regex());
      } catch (PatternSyntaxException e) {
        throw new BatfishException(
            String.format(
                "One of the supplied regexes (%s  OR  %s) is not a valid java regex.",
                question.getNode1Regex(), question.getNode2Regex()),
            e);
      }

      NeighborsAnswerElement answerElement = new NeighborsAnswerElement();

      Map<String, Configuration> configurations = _batfish.loadConfigurations();

      if (question.getStyle() == EdgeStyle.ROLE) {
        NodeRoleSpecifier roleSpecifier = question.getRoleSpecifier();
        if (roleSpecifier != null) {
          _nodeRolesMap = roleSpecifier.createNodeRolesMap(configurations.keySet());
        } else {
          _nodeRolesMap = new TreeMap<>();
          for (Map.Entry<String, Configuration> entry : configurations.entrySet()) {
            _nodeRolesMap.put(entry.getKey(), entry.getValue().getRoles());
          }
        }
      }

      if (question.getNeighborTypes().contains(NeighborType.OSPF)) {
        SortedSet<VerboseOspfEdge> vedges = new TreeSet<>();
        initTopology(configurations);
        initRemoteOspfNeighbors(_batfish, configurations, _topology);
        for (Configuration c : configurations.values()) {
          String hostname = c.getHostname();
          for (Vrf vrf : c.getVrfs().values()) {
            OspfProcess proc = vrf.getOspfProcess();
            if (proc != null) {
              for (OspfNeighbor ospfNeighbor : proc.getOspfNeighbors().values()) {
                OspfNeighbor remoteOspfNeighbor = ospfNeighbor.getRemoteOspfNeighbor();
                if (remoteOspfNeighbor != null) {
                  Configuration remoteHost = remoteOspfNeighbor.getOwner();
                  String remoteHostname = remoteHost.getHostname();
                  Matcher node1Matcher = node1Regex.matcher(hostname);
                  Matcher node2Matcher = node2Regex.matcher(remoteHostname);
                  if (node1Matcher.matches() && node2Matcher.matches()) {
                    Ip localIp = ospfNeighbor.getLocalIp();
                    Ip remoteIp = remoteOspfNeighbor.getLocalIp();
                    IpEdge edge = new IpEdge(hostname, localIp, remoteHostname, remoteIp);
                    vedges.add(
                        new VerboseOspfEdge(c, ospfNeighbor, remoteHost, remoteOspfNeighbor, edge));
                  }
                }
              }
            }
          }
        }

        switch (question.getStyle()) {
          case SUMMARY:
            answerElement.initOspfNeighbors();
            for (VerboseOspfEdge vedge : vedges) {
              answerElement.getOspfNeighbors().add(vedge.getEdgeSummary());
            }
            break;
          case VERBOSE:
            answerElement.setVerboseOspfNeighbors(vedges);
            break;
          case ROLE:
            SortedSet<RoleEdge> redges = new TreeSet<>();
            for (VerboseOspfEdge vedge : vedges) {
              SortedSet<String> roles1 =
                  _nodeRolesMap.getOrDefault(vedge.getNode1().getName(), new TreeSet<>());
              SortedSet<String> roles2 =
                  _nodeRolesMap.getOrDefault(vedge.getNode2().getName(), new TreeSet<>());
              for (String r1 : roles1) {
                for (String r2 : roles2) {
                  redges.add(new RoleEdge(r1, r2));
                }
              }
            }
            answerElement.setRoleOspfNeighbors(redges);
            break;
          default:
            throw new BatfishException(
                "Unsupported " + EdgeStyle.class.getCanonicalName() + ": " + question.getStyle());
        }
      }

      if (question.getNeighborTypes().contains(NeighborType.EBGP)) {
        initRemoteBgpNeighbors(_batfish, configurations);
        SortedSet<VerboseBgpEdge> vedges = new TreeSet<>();
        for (Configuration c : configurations.values()) {
          String hostname = c.getHostname();
          for (Vrf vrf : c.getVrfs().values()) {
            BgpProcess proc = vrf.getBgpProcess();
            if (proc != null) {
              for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
                BgpNeighbor remoteBgpNeighbor = bgpNeighbor.getRemoteBgpNeighbor();
                if (remoteBgpNeighbor != null) {
                  boolean ebgp = !bgpNeighbor.getRemoteAs().equals(bgpNeighbor.getLocalAs());
                  if (ebgp) {
                    Configuration remoteHost = remoteBgpNeighbor.getOwner();
                    String remoteHostname = remoteHost.getHostname();
                    Matcher node1Matcher = node1Regex.matcher(hostname);
                    Matcher node2Matcher = node2Regex.matcher(remoteHostname);
                    if (node1Matcher.matches() && node2Matcher.matches()) {
                      Ip localIp = bgpNeighbor.getLocalIp();
                      Ip remoteIp = remoteBgpNeighbor.getLocalIp();
                      IpEdge edge = new IpEdge(hostname, localIp, remoteHostname, remoteIp);
                      vedges.add(
                          new VerboseBgpEdge(c, bgpNeighbor, remoteHost, remoteBgpNeighbor, edge));
                    }
                  }
                }
              }
            }
          }
        }

        switch (question.getStyle()) {
          case SUMMARY:
            answerElement.initEbgpNeighbors();
            for (VerboseBgpEdge vedge : vedges) {
              answerElement.getEbgpNeighbors().add(vedge.getEdgeSummary());
            }
            break;
          case VERBOSE:
            answerElement.setVerboseEbgpNeighbors(vedges);
            break;
          case ROLE:
            answerElement.setRoleEbgpNeighbors(verboseToRoleEdges(vedges));
            break;
          default:
            throw new BatfishException(
                "Unsupported " + EdgeStyle.class.getCanonicalName() + ": " + question.getStyle());
        }
      }

      if (question.getNeighborTypes().contains(NeighborType.IBGP)) {
        SortedSet<VerboseBgpEdge> vedges = new TreeSet<>();
        initRemoteBgpNeighbors(_batfish, configurations);
        for (Configuration c : configurations.values()) {
          String hostname = c.getHostname();
          for (Vrf vrf : c.getVrfs().values()) {
            BgpProcess proc = vrf.getBgpProcess();
            if (proc != null) {
              for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
                BgpNeighbor remoteBgpNeighbor = bgpNeighbor.getRemoteBgpNeighbor();
                if (remoteBgpNeighbor != null) {
                  boolean ibgp = bgpNeighbor.getRemoteAs().equals(bgpNeighbor.getLocalAs());
                  if (ibgp) {
                    Configuration remoteHost = remoteBgpNeighbor.getOwner();
                    String remoteHostname = remoteHost.getHostname();
                    Matcher node1Matcher = node1Regex.matcher(hostname);
                    Matcher node2Matcher = node2Regex.matcher(remoteHostname);
                    if (node1Matcher.matches() && node2Matcher.matches()) {
                      Ip localIp = bgpNeighbor.getLocalIp();
                      Ip remoteIp = remoteBgpNeighbor.getLocalIp();
                      IpEdge edge = new IpEdge(hostname, localIp, remoteHostname, remoteIp);
                      vedges.add(
                          new VerboseBgpEdge(c, bgpNeighbor, remoteHost, remoteBgpNeighbor, edge));
                    }
                  }
                }
              }
            }
          }
        }
        switch (question.getStyle()) {
          case SUMMARY:
            answerElement.initIbgpNeighbors();
            for (VerboseBgpEdge vedge : vedges) {
              answerElement.getIbgpNeighbors().add(vedge.getEdgeSummary());
            }
            break;
          case VERBOSE:
            answerElement.setVerboseIbgpNeighbors(vedges);
            break;
          case ROLE:
            answerElement.setRoleIbgpNeighbors(verboseToRoleEdges(vedges));
            break;
          default:
            throw new BatfishException(
                "Unsupported " + EdgeStyle.class.getCanonicalName() + ": " + question.getStyle());
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
        switch (question.getStyle()) {
          case SUMMARY:
            answerElement.setLanNeighbors(matchingEdges);
            break;
          case VERBOSE:
            SortedSet<VerboseEdge> vMatchingEdges = new TreeSet<>();
            for (Edge edge : matchingEdges) {
              Configuration n1 = configurations.get(edge.getNode1());
              Interface i1 = n1.getInterfaces().get(edge.getInt1());
              Configuration n2 = configurations.get(edge.getNode2());
              Interface i2 = n2.getInterfaces().get(edge.getInt2());
              vMatchingEdges.add(new VerboseEdge(n1, i1, n2, i2, edge));
            }
            answerElement.setVerboseLanNeighbors(vMatchingEdges);
            break;
          case ROLE:
            SortedSet<RoleEdge> rMatchingEdges = new TreeSet<>();
            for (Edge edge : matchingEdges) {
              SortedSet<String> roles1 =
                  _nodeRolesMap.getOrDefault(edge.getNode1(), new TreeSet<>());
              SortedSet<String> roles2 =
                  _nodeRolesMap.getOrDefault(edge.getNode2(), new TreeSet<>());
              for (String r1 : roles1) {
                for (String r2 : roles2) {
                  rMatchingEdges.add(new RoleEdge(r1, r2));
                }
              }
            }
            answerElement.setRoleLanNeighbors(rMatchingEdges);
            break;
          default:
            throw new BatfishException(
                "Unsupported " + EdgeStyle.class.getCanonicalName() + ": " + question.getStyle());
        }
      }

      return answerElement;
    }

    private void initRemoteBgpNeighbors(
        IBatfish batfish, Map<String, Configuration> configurations) {
      if (!_remoteBgpNeighborsInitialized) {
        Map<Ip, Set<String>> ipOwners = _batfish.computeIpOwners(configurations, true);
        batfish.initRemoteBgpNeighbors(configurations, ipOwners);
        _remoteBgpNeighborsInitialized = true;
      }
    }

    private void initRemoteOspfNeighbors(
        IBatfish batfish, Map<String, Configuration> configurations, Topology topology) {
      if (!_remoteOspfNeighborsInitialized) {
        Map<Ip, Set<String>> ipOwners = _batfish.computeIpOwners(configurations, true);
        batfish.initRemoteOspfNeighbors(configurations, ipOwners, topology);
        _remoteOspfNeighborsInitialized = true;
      }
    }

    private void initTopology(Map<String, Configuration> configurations) {
      if (_topology == null) {
        _topology = _batfish.computeTopology(configurations);
      }
    }

    private SortedSet<RoleEdge> verboseToRoleEdges(SortedSet<VerboseBgpEdge> vedges) {
      SortedSet<RoleEdge> redges = new TreeSet<>();
      for (VerboseBgpEdge vedge : vedges) {
        SortedSet<String> roles1 =
            _nodeRolesMap.getOrDefault(vedge.getNode1().getName(), new TreeSet<>());
        SortedSet<String> roles2 =
            _nodeRolesMap.getOrDefault(vedge.getNode2().getName(), new TreeSet<>());
        for (String r1 : roles1) {
          for (String r2 : roles2) {
            redges.add(new RoleEdge(r1, r2));
          }
        }
      }
      return redges;
    }
  }

  // <question_page_comment>

  /**
   * Lists neighbor relationships in the testrig.
   *
   * <p>Details coming
   *
   * @type Neighbors multifile
   * @param neighborType The type(s) of neighbor relationships to focus on among (eBGP, iBGP, IP).
   *     Default is IP.
   * @param node1Regex Regular expression to match the nodes names for one end of pair. Default is
   *     '.*' (all nodes).
   * @param node2Regex Regular expression to match the nodes names for the other end of the pair.
   *     Default is '.*' (all nodes).
   * @param style String indicating the style of information requested about each edge: "summary" is
   *     the default and returns only the names of nodes/interfaces in the edge; "verbose" provides
   *     full configuration information about those nodes/interfaces; "role" abstracts edges to the
   *     role level.
   * @param roleSpecifier NodeRoleSpecifier that assigns roles to nodes.  This is an optional
   *     variable that is only used if the style is set to "role".  If no roleSpecifier is provided
   *     then by default the roles originally assigned to nodes when the configurations were loaded
   *     are used.
   * @example bf_answer("Neighbors", neighborType=["ebgp", "ibgp"] node1Regex="as1.*",
   *     node2Regex="as2.*") Shows all eBGP and iBGP neighbor relationships between nodes that start
   *     with as1 and those that start with as2.
   */
  public static class NeighborsQuestion extends Question {

    private static final String NEIGHBOR_TYPES_VAR = "neighborTypes";

    private static final String NODE1_REGEX_VAR = "node1Regex";

    private static final String NODE2_REGEX_VAR = "node2Regex";

    private static final String STYLE_VAR = "style";

    private static final String ROLE_SPECIFIER_VAR = "roleSpecifier";

    private SortedSet<NeighborType> _neighborTypes;

    private String _node1Regex;

    private String _node2Regex;

    private EdgeStyle _style;

    private NodeRoleSpecifier _roleSpecifier;

    public NeighborsQuestion() {
      _node1Regex = ".*";
      _node2Regex = ".*";
      _neighborTypes = new TreeSet<>();
      _style = EdgeStyle.SUMMARY;
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

    @JsonProperty(STYLE_VAR)
    public EdgeStyle getStyle() {
      return _style;
    }

    @JsonProperty(ROLE_SPECIFIER_VAR)
    public NodeRoleSpecifier getRoleSpecifier() {
      return _roleSpecifier;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @Override
    public String prettyPrint() {
      try {
        String retString =
            String.format(
                "neighbors %s%s=%s | %s=%s | %s=%s | %s=%b",
                prettyPrintBase(),
                NODE1_REGEX_VAR,
                _node1Regex,
                NODE2_REGEX_VAR,
                _node2Regex,
                NEIGHBOR_TYPES_VAR,
                _neighborTypes.toString(),
                STYLE_VAR,
                _style);
        return retString;
      } catch (Exception e) {
        try {
          return "Pretty printing failed. Printing Json\n" + toJsonString();
        } catch (BatfishException e1) {
          throw new BatfishException("Both pretty and json printing failed\n");
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

    @JsonProperty(STYLE_VAR)
    public void setStyle(EdgeStyle style) {
      _style = style;
    }

    @JsonProperty(ROLE_SPECIFIER_VAR)
    public void setRoleSpecifier(NodeRoleSpecifier roleSpecifier) {
      _roleSpecifier = roleSpecifier;
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
