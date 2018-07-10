package org.batfish.question.bgpsessionstatus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;

public class BgpSessionStatusAnswerer extends Answerer {

  public static final String COL_CONFIGURED_STATUS = "configuredStatus";
  public static final String COL_ESTABLISHED_NEIGHBORS = "establishedNeighbors";
  public static final String COL_LOCAL_INTERFACE = "localInterface";
  public static final String COL_LOCAL_IP = "localIp";
  public static final String COL_NODE = "node";
  public static final String COL_REMOTE_NODE = "remoteNode";
  public static final String COL_REMOTE_PREFIX = "remotePrefix";
  public static final String COL_SESSION_TYPE = "sessionType";
  public static final String COL_VRF_NAME = "vrfName";

  /** Answerer for the BGP Session status question (new version). */
  public BgpSessionStatusAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    BgpSessionStatusQuestion question = (BgpSessionStatusQuestion) _question;
    Multiset<BgpSessionInfo> sessions = rawAnswer(question);
    TableAnswerElement answer =
        new TableAnswerElement(BgpSessionStatusAnswerer.createMetadata(question));
    answer.postProcessAnswer(
        question,
        sessions
            .stream()
            .map(BgpSessionStatusAnswerer::toRow)
            .collect(Collectors.toCollection(HashMultiset::create)));
    return answer;
  }

  private static boolean node2RegexMatchesIp(
      Ip ip, Map<Ip, Set<String>> ipOwners, Set<String> includeNodes2) {
    Set<String> owners = ipOwners.get(ip);
    if (owners == null) {
      throw new BatfishException("Expected at least one owner of ip: " + ip);
    }
    return !Sets.intersection(includeNodes2, owners).isEmpty();
  }

  /**
   * Return the answer for {@link BgpSessionStatusQuestion} -- a set of BGP sessions and their
   * status.
   */
  public Multiset<BgpSessionInfo> rawAnswer(BgpSessionStatusQuestion question) {
    Multiset<BgpSessionInfo> sessions = HashMultiset.create();
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes1 = question.getNode1Regex().getMatchingNodes(_batfish);
    Set<String> includeNodes2 = question.getNode2Regex().getMatchingNodes(_batfish);

    Map<Ip, Set<String>> ipOwners = CommonUtil.computeIpNodeOwners(configurations, true);
    Set<Ip> allInterfaceIps = ipOwners.keySet();

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology =
        CommonUtil.initBgpTopology(configurations, ipOwners, true);

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> establishedBgpTopology;
    if (question.getIncludeEstablishedCount()) {
      DataPlane dp = _batfish.loadDataPlane();
      establishedBgpTopology =
          CommonUtil.initBgpTopology(
              configurations,
              ipOwners,
              false,
              true,
              _batfish.getDataPlanePlugin().getTracerouteEngine(),
              dp);
    } else {
      establishedBgpTopology = null;
    }

    sessions.addAll(
        configuredBgpTopology
            .nodes()
            .stream()
            .map(
                neighbor ->
                    getBgpSessionInfo(
                        question,
                        configurations,
                        includeNodes1,
                        includeNodes2,
                        ipOwners,
                        allInterfaceIps,
                        configuredBgpTopology,
                        establishedBgpTopology,
                        neighbor))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableList.toImmutableList()));

    return sessions;
  }

  private static Optional<BgpSessionInfo> getBgpSessionInfo(
      BgpSessionStatusQuestion question,
      Map<String, Configuration> configurations,
      Set<String> includeNodes1,
      Set<String> includeNodes2,
      Map<Ip, Set<String>> ipOwners,
      Set<Ip> allInterfaceIps,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> establishedBgpTopology,
      BgpPeerConfigId bgpPeerConfigId) {
    String hostname = bgpPeerConfigId.getHostname();
    String vrfName = bgpPeerConfigId.getVrfName();
    // Only match nodes we care about
    if (!includeNodes1.contains(hostname)) {
      return Optional.empty();
    }

    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    BgpPeerConfig bgpPeerConfig = networkConfigurations.getBgpPeerConfig(bgpPeerConfigId);
    if (bgpPeerConfig == null) {
      return Optional.empty();
    }
    // Only match groups we care about
    if (bgpPeerConfig.getGroup() != null
        && question.matchesForeignGroup(bgpPeerConfig.getGroup())) {
      return Optional.empty();
    }

    // Setup session info.
    SessionType sessionType =
        bgpPeerConfig instanceof BgpActivePeerConfig
            ? BgpSessionProperties.getSessionType((BgpActivePeerConfig) bgpPeerConfig)
            : SessionType.UNSET;

    // Skip session types we don't care about
    if (!question.matchesType(sessionType)) {
      return Optional.empty();
    }

    BgpSessionInfo.Builder bsiBuilder =
        BgpSessionInfo.builder(
            hostname, vrfName, bgpPeerConfigId.getRemotePeerPrefix(), sessionType);

    SessionStatus configuredStatus;
    if (bgpPeerConfig instanceof BgpPassivePeerConfig) {
      configuredStatus = SessionStatus.DYNAMIC_LISTEN;
    } else if (bgpPeerConfig instanceof BgpActivePeerConfig) {
      configuredStatus = getLocallyBrokenStatus((BgpActivePeerConfig) bgpPeerConfig, sessionType);
    } else {
      throw new BatfishException("Unsupported type of BGP peer config (not active or passive)");
    }

    if (configuredStatus == null) {
      /*
       * Nothing blatantly broken so far on the local side, keep checking.
       * Also at this point we know this is not a Dynamic bgp neighbor
       */
      Ip localIp = bgpPeerConfig.getLocalIp();
      bsiBuilder.withLocalIp(localIp);
      Optional<Interface> iface =
          CommonUtil.getActiveInterfaceWithIp(localIp, configurations.get(hostname));
      bsiBuilder.withLocalInterface(
          iface
              .map(anInterface -> new NodeInterfacePair(hostname, anInterface.getName()))
              .orElse(null));

      BgpActivePeerConfig p2pBgpPeerConfig = (BgpActivePeerConfig) bgpPeerConfig;
      Ip remoteIp = p2pBgpPeerConfig.getPeerAddress();

      if (!allInterfaceIps.contains(localIp)) {
        configuredStatus = SessionStatus.INVALID_LOCAL_IP;
      } else if (remoteIp == null || !allInterfaceIps.contains(remoteIp)) {
        configuredStatus = SessionStatus.UNKNOWN_REMOTE;
      } else {
        if (!node2RegexMatchesIp(remoteIp, ipOwners, includeNodes2)) {
          return Optional.empty();
        }
        if (configuredBgpTopology.adjacentNodes(bgpPeerConfigId).isEmpty()) {
          configuredStatus = SessionStatus.HALF_OPEN;
          // degree > 2 because of directed edges. 1 edge in, 1 edge out == single connection
        } else if (configuredBgpTopology.degree(bgpPeerConfigId) > 2) {
          configuredStatus = SessionStatus.MULTIPLE_REMOTES;
        } else {
          BgpPeerConfigId remoteNeighbor =
              configuredBgpTopology.adjacentNodes(bgpPeerConfigId).iterator().next();
          bsiBuilder.withRemoteNode(remoteNeighbor.getHostname());
          configuredStatus = SessionStatus.UNIQUE_MATCH;
        }
      }
    }
    if (!question.matchesStatus(configuredStatus)) {
      return Optional.empty();
    }

    bsiBuilder.withConfiguredStatus(configuredStatus);

    bsiBuilder.withEstablishedNeighbors(
        establishedBgpTopology != null && establishedBgpTopology.nodes().contains(bgpPeerConfigId)
            ? establishedBgpTopology.inDegree(bgpPeerConfigId)
            : -1);
    return Optional.of(bsiBuilder.build());
  }

  @Nullable
  @VisibleForTesting
  static SessionStatus getLocallyBrokenStatus(
      BgpActivePeerConfig neighbor, SessionType sessionType) {
    if (neighbor.getLocalIp() == null) {
      if (sessionType == BgpSessionProperties.SessionType.EBGP_MULTIHOP
          || sessionType == BgpSessionProperties.SessionType.IBGP) {
        return SessionStatus.LOCAL_IP_UNKNOWN_STATICALLY;
      } else {
        return SessionStatus.NO_LOCAL_IP;
      }
    } else if (neighbor.getRemoteAs() == null) {
      return SessionStatus.NO_REMOTE_AS;
    }
    return null;
  }

  public static TableMetadata createMetadata(Question question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(
                COL_NODE, Schema.NODE, "The node where this session is configured", true, false),
            new ColumnMetadata(
                COL_LOCAL_IP, Schema.IP, "The local IP of the session", false, false),
            new ColumnMetadata(
                COL_VRF_NAME,
                Schema.STRING,
                "The VRF in which this session is configured",
                true,
                false),
            new ColumnMetadata(
                COL_LOCAL_INTERFACE,
                Schema.INTERFACE,
                "Local interface of the session",
                false,
                true),
            new ColumnMetadata(
                COL_REMOTE_NODE, Schema.NODE, "Remote node for this session", false, false),
            new ColumnMetadata(
                COL_REMOTE_PREFIX, Schema.PREFIX, "Remote prefix for this session", true, false),
            new ColumnMetadata(
                COL_SESSION_TYPE, Schema.STRING, "The type of this session", false, false),
            new ColumnMetadata(
                COL_CONFIGURED_STATUS, Schema.STRING, "Configured status", false, true),
            new ColumnMetadata(
                COL_ESTABLISHED_NEIGHBORS,
                Schema.INTEGER,
                "Number of neighbors with whom BGP session was established",
                false,
                true));

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "On ${%s} session ${%s}:${%s} has configured status ${%s}.",
              COL_NODE, COL_VRF_NAME, COL_REMOTE_PREFIX, COL_CONFIGURED_STATUS));
    }
    return new TableMetadata(columnMetadata, dhints);
  }

  /**
   * Creates a {@link Row} object from the corresponding {@link BgpSessionInfo} object.
   *
   * @param info The input object
   * @return The output row
   */
  public static Row toRow(@Nonnull BgpSessionInfo info) {
    RowBuilder row = Row.builder();
    row.put(COL_CONFIGURED_STATUS, info.getConfiguredStatus())
        .put(COL_ESTABLISHED_NEIGHBORS, info.getEstablishedNeighbors())
        .put(COL_LOCAL_INTERFACE, info.getLocalInterface())
        .put(COL_LOCAL_IP, info.getLocalIp())
        .put(COL_NODE, new Node(info.getNodeName()))
        .put(COL_REMOTE_NODE, info.getRemoteNode() == null ? null : new Node(info.getRemoteNode()))
        .put(COL_REMOTE_PREFIX, info.getRemotePrefix())
        .put(COL_SESSION_TYPE, info.getSessionType())
        .put(COL_VRF_NAME, info.getVrfName());
    return row.build();
  }
}
