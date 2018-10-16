package org.batfish.question.bgpsessionstatus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;

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

  public enum ConfiguredSessionStatus {
    // ordered by how we evaluate status
    DYNAMIC_LISTEN,
    LOCAL_IP_UNKNOWN_STATICALLY,
    NO_LOCAL_IP,
    NO_REMOTE_AS,
    INVALID_LOCAL_IP,
    UNKNOWN_REMOTE,
    HALF_OPEN,
    MULTIPLE_REMOTES,
    UNIQUE_MATCH,
  }

  static @Nonnull BgpPeerConfig getBgpPeerConfig(
      Map<String, Configuration> configurations, BgpPeerConfigId id) {
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    return networkConfigurations.getBgpPeerConfig(id);
  }

  static @Nullable NodeInterfacePair getInterface(Configuration config, Ip localIp) {
    Optional<Interface> iface = CommonUtil.getActiveInterfaceWithIp(localIp, config);
    return iface
        .map(anInterface -> new NodeInterfacePair(config.getHostname(), iface.get().getName()))
        .orElse(null);
  }

  static ConfiguredSessionStatus getConfiguredStatus(
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
    } else if (remoteIp == null || !allInterfaceIps.contains(remoteIp)) {
      return ConfiguredSessionStatus.UNKNOWN_REMOTE;
    } else if (configuredBgpTopology.adjacentNodes(bgpPeerConfigId).isEmpty()) {
      return ConfiguredSessionStatus.HALF_OPEN;
      // degree > 2 because of directed edges. 1 edge in, 1 edge out == single connection
    } else if (configuredBgpTopology.degree(bgpPeerConfigId) > 2) {
      return ConfiguredSessionStatus.MULTIPLE_REMOTES;
    }
    return ConfiguredSessionStatus.UNIQUE_MATCH;
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
    } else if (neighbor.getRemoteAs() == null) {
      return ConfiguredSessionStatus.NO_REMOTE_AS;
    }
    return null;
  }

  public abstract List<Row> getRows(BgpSessionQuestion question);
}
