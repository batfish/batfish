package org.batfish.question.bgpsessionstatus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.specifier.AllNodesNodeSpecifier;

/** Captures the configuration state of a BGP session. */
public abstract class BgpSessionAnswerer extends Answerer {

  public static final String COL_LOCAL_INTERFACE = "Local_Interface";
  public static final String COL_LOCAL_AS = "Local_AS";
  public static final String COL_LOCAL_IP = "Local_IP";
  public static final String COL_NODE = "Node";
  public static final String COL_REMOTE_AS = "Remote_AS";
  public static final String COL_REMOTE_NODE = "Remote_Node";
  public static final String COL_REMOTE_IP = "Remote_IP";
  public static final String COL_SESSION_TYPE = "Session_Type";
  public static final String COL_VRF = "VRF";

  public BgpSessionAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  static @Nonnull BgpPeerConfig getBgpPeerConfig(
      Map<String, Configuration> configurations, BgpPeerConfigId id) {
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    return networkConfigurations.getBgpPeerConfig(id);
  }

  static @Nullable NodeInterfacePair getInterface(Configuration config, Ip localIp) {
    return config.getAllInterfaces().values().stream()
        .filter(
            iface1 ->
                iface1.getActive()
                    && iface1.getAllAddresses().stream()
                        .anyMatch(ifAddr -> Objects.equals(ifAddr.getIp(), localIp)))
        .findAny()
        .map(iface -> new NodeInterfacePair(config.getHostname(), iface.getName()))
        .orElse(null);
  }

  static @Nonnull ConfiguredSessionStatus getConfiguredStatus(
      BgpPeerConfigId bgpPeerConfigId,
      BgpActivePeerConfig activePeerConfig,
      SessionType sessionType,
      Set<Ip> allInterfaceIps,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology) {
    ConfiguredSessionStatus brokenStatus = getLocallyBrokenStatus(activePeerConfig, sessionType);
    if (brokenStatus != null) {
      return brokenStatus;
    }
    // Nothing blatantly broken so far on the local side, keep checking.
    Ip localIp = activePeerConfig.getLocalIp();
    Ip remoteIp = activePeerConfig.getPeerAddress();

    if (!allInterfaceIps.contains(localIp)) {
      return ConfiguredSessionStatus.INVALID_LOCAL_IP;
    } else if (!allInterfaceIps.contains(remoteIp)) {
      return ConfiguredSessionStatus.UNKNOWN_REMOTE;
    } else if (configuredBgpTopology.adjacentNodes(bgpPeerConfigId).isEmpty()) {
      return ConfiguredSessionStatus.HALF_OPEN;
    } else if (configuredBgpTopology.outDegree(bgpPeerConfigId) > 1) {
      return ConfiguredSessionStatus.MULTIPLE_REMOTES;
    }
    return ConfiguredSessionStatus.UNIQUE_MATCH;
  }

  static ConfiguredSessionStatus getLocallyBrokenStatus(BgpPassivePeerConfig passivePeerConfig) {
    if (passivePeerConfig.getLocalAs() == null) {
      return ConfiguredSessionStatus.NO_LOCAL_AS;
    } else if (passivePeerConfig.getPeerPrefix() == null) {
      return ConfiguredSessionStatus.NO_REMOTE_PREFIX;
    } else if (passivePeerConfig.getRemoteAsns().isEmpty()) {
      return ConfiguredSessionStatus.NO_REMOTE_AS;
    }
    return null;
  }

  @Nullable
  @VisibleForTesting
  static ConfiguredSessionStatus getLocallyBrokenStatus(
      BgpActivePeerConfig neighbor, SessionType sessionType) {
    if (neighbor.getLocalIp() == null) {
      if (sessionType == BgpSessionProperties.SessionType.EBGP_MULTIHOP
          || sessionType == BgpSessionProperties.SessionType.IBGP) {
        return ConfiguredSessionStatus.LOCAL_IP_UNKNOWN_STATICALLY;
      } else {
        return ConfiguredSessionStatus.NO_LOCAL_IP;
      }
    } else if (neighbor.getLocalAs() == null) {
      return ConfiguredSessionStatus.NO_LOCAL_AS;
    } else if (neighbor.getPeerAddress() == null) {
      return ConfiguredSessionStatus.NO_REMOTE_IP;
    } else if (neighbor.getRemoteAsns().isEmpty()) {
      return ConfiguredSessionStatus.NO_REMOTE_AS;
    }
    return null;
  }

  /**
   * Returns true if local node, remote node, and session type in row match the question's filters
   */
  protected static boolean matchesNodesAndType(
      Row row, Set<String> nodes, Set<String> remoteNodes, BgpSessionQuestion question) {
    if (!question.getNodeSpecifier().equals(AllNodesNodeSpecifier.INSTANCE)) {
      Node node = (Node) row.get(COL_NODE, Schema.NODE);
      if (node == null || !nodes.contains(node.getName())) {
        return false;
      }
    }
    if (!question.getRemoteNodeSpecifier().equals(AllNodesNodeSpecifier.INSTANCE)) {
      Node remoteNode = (Node) row.get(COL_REMOTE_NODE, Schema.NODE);
      if (remoteNode == null || !remoteNodes.contains(remoteNode.getName())) {
        return false;
      }
    }
    String typeName = (String) row.get(COL_SESSION_TYPE, Schema.STRING);
    SessionType type = typeName == null ? null : SessionType.valueOf(typeName);
    if (!question.matchesType(type)) {
      return false;
    }
    return true;
  }

  public abstract List<Row> getRows(BgpSessionQuestion question);
}
