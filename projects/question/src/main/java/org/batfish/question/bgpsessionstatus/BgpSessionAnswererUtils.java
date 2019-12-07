package org.batfish.question.bgpsessionstatus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ValueGraph;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.datamodel.table.Row;
import org.batfish.specifier.AllNodesNodeSpecifier;

/**
 * Shared utility methods for {@link BgpSessionCompatibilityAnswerer} and {@link
 * BgpSessionStatusAnswerer}.
 */
public final class BgpSessionAnswererUtils {

  /* Common column names for both BGP session questions*/
  public static final String COL_ADDRESS_FAMILIES = "Address_Families";
  public static final String COL_LOCAL_INTERFACE = "Local_Interface";
  public static final String COL_LOCAL_AS = "Local_AS";
  public static final String COL_LOCAL_IP = "Local_IP";
  public static final String COL_NODE = "Node";
  public static final String COL_REMOTE_AS = "Remote_AS";
  public static final String COL_REMOTE_NODE = "Remote_Node";
  public static final String COL_REMOTE_INTERFACE = "Remote_Interface";
  public static final String COL_REMOTE_IP = "Remote_IP";
  public static final String COL_SESSION_TYPE = "Session_Type";
  public static final String COL_VRF = "VRF";

  static @Nonnull BgpPeerConfig getBgpPeerConfig(
      Map<String, Configuration> configurations, BgpPeerConfigId id) {
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    return networkConfigurations.getBgpPeerConfig(id);
  }

  static @Nonnull ConfiguredSessionStatus getConfiguredStatus(
      BgpPeerConfigId peerId,
      BgpActivePeerConfig activePeerConfig,
      SessionType sessionType,
      Map<Ip, Map<String, Set<String>>> ipVrfOwners,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology) {
    ConfiguredSessionStatus brokenStatus = getLocallyBrokenStatus(activePeerConfig, sessionType);
    if (brokenStatus != null) {
      return brokenStatus;
    }
    // Nothing blatantly broken so far on the local side, keep checking.
    Ip localIp = activePeerConfig.getLocalIp();
    Ip remoteIp = activePeerConfig.getPeerAddress();

    if (!ipVrfOwners
        .getOrDefault(localIp, ImmutableMap.of())
        .getOrDefault(peerId.getHostname(), ImmutableSet.of())
        .contains(peerId.getVrfName())) {
      return ConfiguredSessionStatus.INVALID_LOCAL_IP;
    } else if (!ipVrfOwners.containsKey(remoteIp)) {
      return ConfiguredSessionStatus.UNKNOWN_REMOTE;
    } else if (configuredBgpTopology.adjacentNodes(peerId).isEmpty()) {
      return ConfiguredSessionStatus.HALF_OPEN;
    } else if (configuredBgpTopology.outDegree(peerId) > 1) {
      return ConfiguredSessionStatus.MULTIPLE_REMOTES;
    }
    return ConfiguredSessionStatus.UNIQUE_MATCH;
  }

  static @Nonnull ConfiguredSessionStatus getConfiguredStatus(
      BgpPeerConfigId bgpPeerConfigId,
      BgpUnnumberedPeerConfig unnumPeerConfig,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology) {
    // Not checking for local interface because that's nonnull (for now at least, we're not aware of
    // a way to configure an unnumbered peer without an interface)
    if (unnumPeerConfig.getLocalAs() == null) {
      return ConfiguredSessionStatus.NO_LOCAL_AS;
    } else if (unnumPeerConfig.getRemoteAsns().isEmpty()) {
      return ConfiguredSessionStatus.NO_REMOTE_AS;
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
  static boolean matchesNodesAndType(
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
    return question.matchesType(
        SessionType.parse((String) row.get(COL_SESSION_TYPE, Schema.STRING)));
  }

  private BgpSessionAnswererUtils() {}
}
