package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import com.google.common.graph.ValueGraph;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.RipNeighbor;
import org.batfish.datamodel.RipProcess;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.VerboseEdge;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.IpEdge;
import org.batfish.datamodel.collections.VerboseBgpEdge;
import org.batfish.datamodel.collections.VerboseEigrpEdge;
import org.batfish.datamodel.collections.VerboseOspfEdge;
import org.batfish.datamodel.collections.VerboseRipEdge;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterface;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.ospf.OspfNeighbor;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class VIModelQuestionPlugin extends QuestionPlugin {

  private static final Comparator<VerboseBgpEdge> VERBOSE_BGP_EDGE_COMPARATOR =
      Comparator.nullsFirst(
          Comparator.comparing(VerboseBgpEdge::getEdgeSummary)
              .thenComparing(VerboseBgpEdge::getSession1Id)
              .thenComparing(VerboseBgpEdge::getSession2Id));

  public static class VIModelAnswerElement extends AnswerElement {
    private static final String PROP_NODES = "nodes";

    private static final String PROP_BGP_EDGES = "bgpEdges";

    private static final String PROP_LAYER1_EDGES = "layer1Edges";

    private static final String PROP_LAYER2_EDGES = "layer2Edges";

    private static final String PROP_LAYER3_EDGES = "layer3Edges";

    private static final String PROP_EIGRP_EDGES = "eigrpEdges";

    private static final String PROP_OSPF_EDGES = "ospfEdges";

    private static final String PROP_RIP_EDGES = "ripEdges";

    private final SortedMap<String, Configuration> _nodes;

    private SortedSet<VerboseBgpEdge> _bgpEdges;

    private SortedSet<VerboseEigrpEdge> _eigrpEdges;

    private Layer1Topology _layer1Edges;

    private Layer2Topology _layer2Edges;

    private SortedSet<VerboseEdge> _layer3Edges;

    private SortedSet<VerboseOspfEdge> _ospfEdges;

    private SortedSet<VerboseRipEdge> _ripEdges;

    @JsonCreator
    public VIModelAnswerElement(
        @JsonProperty(PROP_NODES) SortedMap<String, Configuration> nodes,
        @JsonProperty(PROP_BGP_EDGES) Set<VerboseBgpEdge> bgpEdges,
        @JsonProperty(PROP_EIGRP_EDGES) SortedSet<VerboseEigrpEdge> eigrpEdges,
        @JsonProperty(PROP_LAYER1_EDGES) Layer1Topology layer1Edges,
        @JsonProperty(PROP_LAYER2_EDGES) Layer2Topology layer2Edges,
        @JsonProperty(PROP_LAYER3_EDGES) SortedSet<VerboseEdge> layer3Edges,
        @JsonProperty(PROP_OSPF_EDGES) SortedSet<VerboseOspfEdge> ospfEdges,
        @JsonProperty(PROP_RIP_EDGES) SortedSet<VerboseRipEdge> ripEdges) {
      _nodes = nodes;
      _bgpEdges = new TreeSet<>(VERBOSE_BGP_EDGE_COMPARATOR);
      _bgpEdges.addAll(bgpEdges);
      _eigrpEdges = eigrpEdges;
      _layer1Edges = layer1Edges;
      _layer2Edges = layer2Edges;
      _layer3Edges = layer3Edges;
      _ospfEdges = ospfEdges;
      _ripEdges = ripEdges;
    }

    @JsonProperty(PROP_NODES)
    public SortedMap<String, Configuration> getNodes() {
      return _nodes;
    }

    @JsonProperty(PROP_BGP_EDGES)
    public SortedSet<VerboseBgpEdge> getBgpEdges() {
      return _bgpEdges;
    }

    @JsonProperty(PROP_EIGRP_EDGES)
    public SortedSet<VerboseEigrpEdge> getEigrpEdges() {
      return _eigrpEdges;
    }

    @JsonProperty(PROP_LAYER1_EDGES)
    public Layer1Topology getLayer1Edges() {
      return _layer1Edges;
    }

    @JsonProperty(PROP_LAYER2_EDGES)
    public Layer2Topology getLayer2Edges() {
      return _layer2Edges;
    }

    @JsonProperty(PROP_LAYER3_EDGES)
    public SortedSet<VerboseEdge> getLayer3Edges() {
      return _layer3Edges;
    }

    @JsonProperty(PROP_OSPF_EDGES)
    public SortedSet<VerboseOspfEdge> getOspfEdges() {
      return _ospfEdges;
    }

    @JsonProperty(PROP_RIP_EDGES)
    public SortedSet<VerboseRipEdge> getRipEdges() {
      return _ripEdges;
    }
  }

  public static class VIModelAnswerer extends Answerer {

    public VIModelAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public VIModelAnswerElement answer() {
      SortedMap<String, Configuration> configs = _batfish.loadConfigurations();
      Topology topology = _batfish.getEnvironmentTopology();
      Map<Ip, Set<String>> ipOwners = CommonUtil.computeIpNodeOwners(configs, true);
      CommonUtil.initRemoteOspfNeighbors(configs, ipOwners, topology);
      _batfish.initRemoteRipNeighbors(configs, ipOwners, topology);

      return new VIModelAnswerElement(
          configs,
          getBgpEdges(configs, ipOwners),
          getEigrpEdges(configs, topology),
          _batfish.getLayer1Topology(),
          _batfish.getLayer2Topology(),
          getLayer3Edges(configs, topology),
          getOspfEdges(configs),
          getRipEdges(configs));
    }

    private static SortedSet<VerboseBgpEdge> getBgpEdges(
        Map<String, Configuration> configs, Map<Ip, Set<String>> ipOwners) {
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
          CommonUtil.initBgpTopology(configs, ipOwners, false, false, null, null);
      SortedSet<VerboseBgpEdge> bgpEdges = new TreeSet<>(VERBOSE_BGP_EDGE_COMPARATOR);
      for (EndpointPair<BgpPeerConfigId> session : bgpTopology.edges()) {
        BgpPeerConfigId bgpPeerConfigId = session.source();
        BgpPeerConfigId remoteBgpPeerConfigId = session.target();
        NetworkConfigurations nc = NetworkConfigurations.of(configs);
        String hostname = bgpPeerConfigId.getHostname();
        String remoteHostname = remoteBgpPeerConfigId.getHostname();
        BgpPeerConfig bgpPeerConfig = nc.getBgpPeerConfig(bgpPeerConfigId);
        BgpPeerConfig remoteBgpPeerConfig = nc.getBgpPeerConfig(remoteBgpPeerConfigId);

        if (bgpPeerConfig != null && remoteBgpPeerConfig != null) {
          Ip localIp = bgpPeerConfig.getLocalIp();
          Ip remoteIp = remoteBgpPeerConfig.getLocalIp();
          IpEdge ipEdge = new IpEdge(hostname, localIp, remoteHostname, remoteIp);
          bgpEdges.add(
              new VerboseBgpEdge(
                  bgpPeerConfig,
                  remoteBgpPeerConfig,
                  bgpPeerConfigId,
                  remoteBgpPeerConfigId,
                  ipEdge));
        }
      }
      return bgpEdges;
    }

    private static SortedSet<VerboseEigrpEdge> getEigrpEdges(
        Map<String, Configuration> configs, Topology topology) {
      Network<EigrpInterface, EigrpEdge> eigrpTopology =
          EigrpTopology.initEigrpTopology(configs, topology);
      NetworkConfigurations nc = NetworkConfigurations.of(configs);
      SortedSet<VerboseEigrpEdge> eigrpEdges = new TreeSet<>();
      for (Configuration c : configs.values()) {
        String hostname = c.getHostname();
        for (Vrf vrf : c.getVrfs().values()) {
          eigrpEdges.addAll(
              vrf.getInterfaceNames()
                  .stream()
                  .map(ifaceName -> new EigrpInterface(hostname, ifaceName, vrf.getName()))
                  .filter(eigrpTopology.nodes()::contains)
                  .flatMap(n -> eigrpTopology.inEdges(n).stream())
                  .map(edge -> new VerboseEigrpEdge(edge, edge.toIpEdge(nc)))
                  .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
        }
      }
      return eigrpEdges;
    }

    private static SortedSet<VerboseEdge> getLayer3Edges(
        Map<String, Configuration> configs, Topology topology) {
      SortedSet<VerboseEdge> layer3Edges = new TreeSet<>();
      for (Edge edge : topology.getEdges()) {
        Configuration n1 = configs.get(edge.getNode1());
        Interface i1 = n1.getAllInterfaces().get(edge.getInt1());
        Configuration n2 = configs.get(edge.getNode2());
        Interface i2 = n2.getAllInterfaces().get(edge.getInt2());
        layer3Edges.add(new VerboseEdge(i1, i2, edge));
      }
      return layer3Edges;
    }

    private static SortedSet<VerboseOspfEdge> getOspfEdges(Map<String, Configuration> configs) {
      SortedSet<VerboseOspfEdge> ospfEdges = new TreeSet<>();
      for (Configuration c : configs.values()) {
        String hostname = c.getHostname();
        for (Vrf vrf : c.getVrfs().values()) {
          OspfProcess proc = vrf.getOspfProcess();
          if (proc != null) {
            for (OspfNeighbor ospfNeighbor : proc.getOspfNeighbors().values()) {
              OspfNeighbor remoteOspfNeighbor = ospfNeighbor.getRemoteOspfNeighbor();
              if (remoteOspfNeighbor != null) {
                Configuration remoteHost = remoteOspfNeighbor.getOwner();
                String remoteHostname = remoteHost.getHostname();
                Ip localIp = ospfNeighbor.getLocalIp();
                Ip remoteIp = remoteOspfNeighbor.getLocalIp();
                IpEdge edge = new IpEdge(hostname, localIp, remoteHostname, remoteIp);
                ospfEdges.add(new VerboseOspfEdge(ospfNeighbor, remoteOspfNeighbor, edge));
              }
            }
          }
        }
      }
      return ospfEdges;
    }

    private static SortedSet<VerboseRipEdge> getRipEdges(Map<String, Configuration> configs) {
      SortedSet<VerboseRipEdge> ripEdges = new TreeSet<>();
      for (Configuration c : configs.values()) {
        String hostname = c.getHostname();
        for (Vrf vrf : c.getVrfs().values()) {
          RipProcess proc = vrf.getRipProcess();
          if (proc != null) {
            for (RipNeighbor ripNeighbor : proc.getRipNeighbors().values()) {
              RipNeighbor remoteRipNeighbor = ripNeighbor.getRemoteRipNeighbor();
              if (remoteRipNeighbor != null) {
                Configuration remoteHost = remoteRipNeighbor.getOwner();
                String remoteHostname = remoteHost.getHostname();
                Ip localIp = ripNeighbor.getLocalIp();
                Ip remoteIp = remoteRipNeighbor.getLocalIp();
                IpEdge edge = new IpEdge(hostname, localIp, remoteHostname, remoteIp);
                ripEdges.add(new VerboseRipEdge(ripNeighbor, remoteRipNeighbor, edge));
              }
            }
          }
        }
      }
      return ripEdges;
    }
  }

  public static class VIModelQuestion extends Question {
    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "viModel";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new VIModelAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new VIModelQuestion();
  }
}
