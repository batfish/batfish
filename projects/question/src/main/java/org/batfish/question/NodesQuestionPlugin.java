package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NodeType;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class NodesQuestionPlugin extends QuestionPlugin {

  public static class NodesAnswerElement extends AnswerElement {

    public static class NodeSummary {

      private SortedSet<String> _asPathAccessLists;

      private SortedSet<String> _communityLists;

      private ConfigurationFormat _configurationFormat;

      private SortedSet<String> _ikePhase1Keys;

      private SortedSet<String> _ikePhase1Policies;

      private SortedSet<String> _ikePhase1Proposals;

      private SortedSet<String> _interfaces;

      private SortedSet<String> _ipAccessLists;

      private SortedSet<String> _ipsecPeerConfigs;

      private SortedSet<String> _ipsecPhase2Policies;

      private SortedSet<String> _ipsecPhase2Proposals;

      private SortedSet<String> _routeFilterLists;

      private SortedSet<String> _routingPolicies;

      private EnumSet<RoutingProtocol> _routingProtocols;

      private SortedSet<String> _zones;

      public NodeSummary() {}

      public NodeSummary(Configuration node) {
        if (!node.getAsPathAccessLists().isEmpty()) {
          _asPathAccessLists = node.getAsPathAccessLists().navigableKeySet();
        }
        if (!node.getCommunityLists().isEmpty()) {
          _communityLists = node.getCommunityLists().navigableKeySet();
        }
        _configurationFormat = node.getConfigurationFormat();
        if (!node.getAllInterfaces().isEmpty()) {
          _interfaces = node.getAllInterfaces().navigableKeySet();
        }
        if (!node.getIkePhase1Keys().isEmpty()) {
          _ikePhase1Keys = node.getIkePhase1Keys().navigableKeySet();
        }
        if (!node.getIkePhase1Policies().isEmpty()) {
          _ikePhase1Policies = node.getIkePhase1Policies().navigableKeySet();
        }
        if (!node.getIkePhase1Proposals().isEmpty()) {
          _ikePhase1Proposals = node.getIkePhase1Proposals().navigableKeySet();
        }
        if (!node.getIpAccessLists().isEmpty()) {
          _ipAccessLists = node.getIpAccessLists().navigableKeySet();
        }
        if (!node.getIpsecPeerConfigs().isEmpty()) {
          _ipsecPeerConfigs = node.getIpsecPeerConfigs().navigableKeySet();
        }
        if (!node.getIpsecPhase2Policies().isEmpty()) {
          _ipsecPhase2Policies = node.getIpsecPhase2Policies().navigableKeySet();
        }
        if (!node.getIpsecPhase2Proposals().isEmpty()) {
          _ipsecPhase2Proposals = node.getIpsecPhase2Proposals().navigableKeySet();
        }
        if (!node.getRoutingPolicies().isEmpty()) {
          _routingPolicies = node.getRoutingPolicies().navigableKeySet();
        }
        if (!node.getRouteFilterLists().isEmpty()) {
          _routeFilterLists = node.getRouteFilterLists().navigableKeySet();
        }
        _routingProtocols = EnumSet.noneOf(RoutingProtocol.class);
        for (Vrf vrf : node.getVrfs().values()) {
          if (vrf.getBgpProcess() != null) {
            _routingProtocols.add(RoutingProtocol.BGP);
            break;
          }
        }
        for (Vrf vrf : node.getVrfs().values()) {
          if (vrf.getOspfProcess() != null) {
            _routingProtocols.add(RoutingProtocol.OSPF);
            break;
          }
        }
        for (Vrf vrf : node.getVrfs().values()) {
          if (!vrf.getEigrpProcesses().isEmpty()) {
            _routingProtocols.add(RoutingProtocol.EIGRP);
            break;
          }
        }
        for (Vrf vrf : node.getVrfs().values()) {
          if (vrf.getIsisProcess() != null) {
            _routingProtocols.add(RoutingProtocol.ISIS_ANY);
            break;
          }
        }
        for (Vrf vrf : node.getVrfs().values()) {
          if (!vrf.getStaticRoutes().isEmpty()) {
            _routingProtocols.add(RoutingProtocol.STATIC);
            break;
          }
        }
        for (Vrf vrf : node.getVrfs().values()) {
          if (!vrf.getGeneratedRoutes().isEmpty()) {
            _routingProtocols.add(RoutingProtocol.AGGREGATE);
            break;
          }
        }
        if (!node.getZones().isEmpty()) {
          _zones = node.getZones().navigableKeySet();
        }
      }

      public SortedSet<String> getAsPathAccessLists() {
        return _asPathAccessLists;
      }

      public SortedSet<String> getCommunityLists() {
        return _communityLists;
      }

      public ConfigurationFormat getConfigurationFormat() {
        return _configurationFormat;
      }

      public SortedSet<String> getIkePhase1Keys() {
        return _ikePhase1Keys;
      }

      public SortedSet<String> getIkePhase1Policies() {
        return _ikePhase1Policies;
      }

      public SortedSet<String> getIkePhase1Proposals() {
        return _ikePhase1Proposals;
      }

      public SortedSet<String> getInterfaces() {
        return _interfaces;
      }

      public SortedSet<String> getIpAccessLists() {
        return _ipAccessLists;
      }

      public SortedSet<String> getIpsecPeerConfigs() {
        return _ipsecPeerConfigs;
      }

      public SortedSet<String> getIpsecPhase2Policies() {
        return _ipsecPhase2Policies;
      }

      public SortedSet<String> get_ipsecPhase2Proposals() {
        return _ipsecPhase2Proposals;
      }

      public SortedSet<String> getPolicySortedMaps() {
        return _routingPolicies;
      }

      public SortedSet<String> getRouteFilterLists() {
        return _routeFilterLists;
      }

      public EnumSet<RoutingProtocol> getRoutingProtocols() {
        return _routingProtocols;
      }

      public SortedSet<String> getZones() {
        return _zones;
      }

      public void setAsPathAccessLists(SortedSet<String> asPathAccessLists) {
        _asPathAccessLists = asPathAccessLists;
      }

      public void setCommunityLists(SortedSet<String> communityLists) {
        _communityLists = communityLists;
      }

      public void setConfigurationFormat(ConfigurationFormat configurationFormat) {
        _configurationFormat = configurationFormat;
      }

      public void setIkePhase1Keys(SortedSet<String> ikePhase1Keys) {
        _ikePhase1Keys = ikePhase1Keys;
      }

      public void setIkePhase1Policies(SortedSet<String> ikePhase1Policies) {
        _ikePhase1Policies = ikePhase1Policies;
      }

      public void setIkePhase1Proposals(SortedSet<String> ikePhase1Proposals) {
        _ikePhase1Proposals = ikePhase1Proposals;
      }

      public void setInterfaces(SortedSet<String> interfaces) {
        _interfaces = interfaces;
      }

      public void setIpAccessLists(SortedSet<String> ipAccessLists) {
        _ipAccessLists = ipAccessLists;
      }

      public void setIpsecPeerConfigs(SortedSet<String> ipsecPeerConfigs) {
        _ipsecPeerConfigs = ipsecPeerConfigs;
      }

      public void setIpsecPhase2Policies(SortedSet<String> ipsecPhase2Policies) {
        _ipsecPhase2Policies = ipsecPhase2Policies;
      }

      public void setIpsecPhase2Proposals(SortedSet<String> ipsecPhase2Proposals) {
        _ipsecPhase2Proposals = ipsecPhase2Proposals;
      }

      public void setPolicySortedMaps(SortedSet<String> policySortedMaps) {
        _routingPolicies = policySortedMaps;
      }

      public void setRouteFilterLists(SortedSet<String> routeFilterLists) {
        _routeFilterLists = routeFilterLists;
      }

      public void setRoutingProtocols(EnumSet<RoutingProtocol> routingProtocols) {
        _routingProtocols = routingProtocols;
      }

      public void setZones(SortedSet<String> zones) {
        _zones = zones;
      }
    }

    private static final String PROP_NODES_SUMMARY = "nodesSummary";

    private static final String PROP_NODES = "nodes";

    private final SortedMap<String, Configuration> _nodes;

    private final SortedMap<String, NodeSummary> _nodesSummary;

    public NodesAnswerElement(SortedMap<String, Configuration> nodes, boolean summary) {

      if (summary) {
        _nodesSummary = new TreeMap<>();
        for (Entry<String, Configuration> e : nodes.entrySet()) {
          String hostname = e.getKey();
          Configuration node = e.getValue();
          _nodesSummary.put(hostname, new NodeSummary(node));
        }
        _nodes = null;
      } else {
        _nodes = nodes;
        _nodesSummary = null;
      }
    }

    @JsonCreator
    public NodesAnswerElement(
        @JsonProperty(PROP_NODES) SortedMap<String, Configuration> nodes,
        @JsonProperty(PROP_NODES_SUMMARY) SortedMap<String, NodeSummary> nodesSummary) {
      _nodes = nodes;
      _nodesSummary = nodesSummary;
    }

    @JsonProperty(PROP_NODES)
    public SortedMap<String, Configuration> getAnswer() {
      return _nodes;
    }

    @JsonProperty(PROP_NODES_SUMMARY)
    public SortedMap<String, NodeSummary> getNodesSummary() {
      return _nodesSummary;
    }
  }

  public static class NodesAnswerer extends Answerer {

    public NodesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public NodesAnswerElement answer() {
      NodesQuestion question = (NodesQuestion) _question;

      Map<String, Configuration> configurations = _batfish.loadConfigurations();

      // initRemoteBgpNeighbors(_batfish, configurations);

      SortedMap<String, Configuration> answerNodes = new TreeMap<>();
      Set<String> nodes = question.getNodes().getMatchingNodes(_batfish);
      for (String node : configurations.keySet()) {
        if (nodes.contains(node)) {
          answerNodes.put(node, configurations.get(node));
        }
      }

      return new NodesAnswerElement(answerNodes, question.getSummary());
    }

    @Override
    public AnswerElement answerDiff() {
      throw new BatfishException("Nodes question in differential mode no longer supported.");
    }
  }

  /**
   * Outputs the configuration of nodes in the network.
   *
   * <p>This question may be used to extract the configuration of the node in the Batfish datamodel
   * or a summary of it.
   *
   * <p>Generally prefer properties questions like {@link
   * org.batfish.question.nodeproperties.NodePropertiesQuestion}.
   */
  public static class NodesQuestion extends Question {

    private static final String PROP_NODES = "nodes";

    private static final String PROP_NODE_TYPES = "nodeTypes";

    private static final String PROP_SUMMARY = "summary";

    private NodesSpecifier _nodes;

    private SortedSet<NodeType> _nodeTypes;

    private boolean _summary;

    public NodesQuestion() {
      _nodeTypes = new TreeSet<>();
      _nodes = NodesSpecifier.ALL;
      _summary = true;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "nodes";
    }

    @JsonProperty(PROP_NODES)
    public NodesSpecifier getNodes() {
      return _nodes;
    }

    @JsonProperty(PROP_NODE_TYPES)
    public SortedSet<NodeType> getNodeTypes() {
      return _nodeTypes;
    }

    @JsonProperty(PROP_SUMMARY)
    public boolean getSummary() {
      return _summary;
    }

    @JsonProperty(PROP_NODES)
    public void setNodes(NodesSpecifier regex) {
      _nodes = regex;
    }

    @JsonProperty(PROP_NODE_TYPES)
    public void setNodeTypes(SortedSet<NodeType> nodeTypes) {
      _nodeTypes = nodeTypes;
    }

    @JsonProperty(PROP_SUMMARY)
    public void setSummary(boolean summary) {
      _summary = summary;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new NodesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new NodesQuestion();
  }
}
