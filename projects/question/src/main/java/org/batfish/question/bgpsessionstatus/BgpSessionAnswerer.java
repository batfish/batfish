package org.batfish.question.bgpsessionstatus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
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

  public enum SessionStatus {
    ESTABLISHED,
    NOT_ESTABLISHED,
    NOT_COMPATIBLE
  }

  static boolean isCompatible(ConfiguredSessionStatus configuredStatus) {
    switch (configuredStatus) {
      case UNIQUE_MATCH:
      case DYNAMIC_LISTEN:
        return true;
      case LOCAL_IP_UNKNOWN_STATICALLY:
      case NO_LOCAL_IP:
      case NO_REMOTE_AS:
      case INVALID_LOCAL_IP:
      case UNKNOWN_REMOTE:
      case HALF_OPEN:
      case MULTIPLE_REMOTES:
        return false;
      default:
        throw new BatfishException("Unrecognized configured status: " + configuredStatus);
    }
  }

  //  static Optional<BgpSessionAnswerer> getBgpSessionInfo(
  //      BgpSessionQuestion question,
  //      Map<String, Configuration> configurations,
  //      Set<String> includeNodes1,
  //      Set<String> includeNodes2,
  //      Map<Ip, Set<String>> ipOwners,
  //      Set<Ip> allInterfaceIps,
  //      ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology,
  //      ValueGraph<BgpPeerConfigId, BgpSessionProperties> establishedBgpTopology,
  //      BgpPeerConfigId bgpPeerConfigId) {
  //    String hostname = bgpPeerConfigId.getHostname();
  //    String vrfName = bgpPeerConfigId.getVrfName();
  //    // Only match nodes we care about
  //    if (!includeNodes1.contains(hostname)) {
  //      return Optinal.empty();
  //    }
  //
  //    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
  //    BgpPeerConfig bgpPeerConfig = networkConfigurations.getBgpPeerConfig(bgpPeerConfigId);
  //    if (bgpPeerConfig == null) {
  //      return Optinal.empty();
  //    }
  //
  //    // Setup session info.
  //    SessionType sessionType =
  //        bgpPeerConfig instanceof BgpActivePeerConfig
  //            ? BgpSessionProperties.getSessionType((BgpActivePeerConfig) bgpPeerConfig)
  //            : SessionType.UNSET;
  //
  //    // Skip session types we don't care about
  //    if (!question.matchesType(sessionType)) {
  //      return Optinal.empty();
  //    }
  //
  //    BgpSessionAnswerer.Builder bsiBuilder =
  //        BgpSessionAnswerer.builder(
  //            hostname,
  //            vrfName,
  //            bgpPeerConfigId.getRemotePeerPrefix(),
  //            sessionType,
  //            bgpPeerConfig.getLocalAs());
  //
  //    ConfiguredSessionStatus configuredStatus;
  //    if (bgpPeerConfig instanceof BgpPassivePeerConfig) {
  //      configuredStatus = ConfiguredSessionStatus.DYNAMIC_LISTEN;
  //      bsiBuilder.withRemoteAs(((BgpPassivePeerConfig) bgpPeerConfig).getRemoteAs());
  //    } else if (bgpPeerConfig instanceof BgpActivePeerConfig) {
  //      BgpActivePeerConfig activePeerConfig = (BgpActivePeerConfig) bgpPeerConfig;
  //      configuredStatus = getLocallyBrokenStatus(activePeerConfig, sessionType);
  //      bsiBuilder.withRemoteAs(activePeerConfig.getRemoteAs());
  //    } else {
  //      throw new BatfishException("Unsupported type of BGP peer config (not active or passive)");
  //    }
  //
  //    if (configuredStatus == null) {
  //      /*
  //       * Nothing blatantly broken so far on the local side, keep checking.
  //       * Also at this point we know this is not a Dynamic bgp neighbor
  //       */
  //      Ip localIp = bgpPeerConfig.getLocalIp();
  //      bsiBuilder.withLocalIp(localIp);
  //      Optional<Interface> iface =
  //          CommonUtil.getActiveInterfaceWithIp(localIp, configurations.get(hostname));
  //      bsiBuilder.withLocalInterface(
  //          iface
  //              .map(anInterface -> new NodeInterfacePair(hostname, anInterface.getName()))
  //              .orElse(null));
  //
  //      BgpActivePeerConfig p2pBgpPeerConfig = (BgpActivePeerConfig) bgpPeerConfig;
  //      Ip remoteIp = p2pBgpPeerConfig.getPeerAddress();
  //
  //      if (!allInterfaceIps.contains(localIp)) {
  //        configuredStatus = ConfiguredSessionStatus.INVALID_LOCAL_IP;
  //      } else if (remoteIp == null || !allInterfaceIps.contains(remoteIp)) {
  //        configuredStatus = ConfiguredSessionStatus.UNKNOWN_REMOTE;
  //      } else {
  //        if (!node2RegexMatchesIp(remoteIp, ipOwners, includeNodes2)) {
  //          return Optinal.empty();
  //        }
  //        if (configuredBgpTopology.adjacentNodes(bgpPeerConfigId).isEmpty()) {
  //          configuredStatus = ConfiguredSessionStatus.HALF_OPEN;
  //          // degree > 2 because of directed edges. 1 edge in, 1 edge out == single connection
  //        } else if (configuredBgpTopology.degree(bgpPeerConfigId) > 2) {
  //          configuredStatus = ConfiguredSessionStatus.MULTIPLE_REMOTES;
  //        } else {
  //          BgpPeerConfigId remoteNeighbor =
  //              configuredBgpTopology.adjacentNodes(bgpPeerConfigId).iterator().next();
  //          bsiBuilder.withRemoteNode(remoteNeighbor.getHostname());
  //          configuredStatus = ConfiguredSessionStatus.UNIQUE_MATCH;
  //        }
  //      }
  //    }
  //
  //    bsiBuilder.withConfiguredStatus(configuredStatus);
  //
  //    //    if (establishedBgpTopology != null) {
  //    //      SessionStatus establishedStatus = SessionStatus.NOT_COMPATIBLE;
  //    //      if (isCompatible(configuredStatus)) {
  //    //        establishedStatus =
  //    //            establishedBgpTopology.edges().contains(edge)
  //    //                ? SessionStatus.ESTABLISHED
  //    //                : SessionStatus.NOT_ESTABLISHED;
  //    //      }
  //    //      bsiBuilder.withEstablishedStatus(establishedStatus);
  //    //    }
  //    return Optional.of(bsiBuilder.build());
  //  }

  static @Nonnull BgpPeerConfig getBgpPeerConfig(
      Map<String, Configuration> configurations, BgpPeerConfigId id) {
    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    return networkConfigurations.getBgpPeerConfig(id);
  }

  static SessionType getSessionType(BgpPeerConfig bgpPeerConfig) {
    return bgpPeerConfig instanceof BgpActivePeerConfig
        ? BgpSessionProperties.getSessionType((BgpActivePeerConfig) bgpPeerConfig)
        : SessionType.UNSET;
  }

  static @Nullable NodeInterfacePair getInterface(Configuration config, Ip localIp) {
    Optional<Interface> iface = CommonUtil.getActiveInterfaceWithIp(localIp, config);
    return iface
        .map(anInterface -> new NodeInterfacePair(config.getHostname(), iface.get().getName()))
        .orElse(null);
  }

  static SelfDescribingObject getRemoteIpEntry(@Nonnull Prefix remotePrefix) {
    return remotePrefix.getPrefixLength() == 32
        ? new SelfDescribingObject(Schema.IP, remotePrefix.getStartIp())
        : new SelfDescribingObject(Schema.PREFIX, remotePrefix);
  }

  static ConfiguredSessionStatus getConfiguredStatus(
      BgpPeerConfigId bgpPeerConfigId,
      BgpPeerConfig bgpPeerConfig,
      SessionType sessionType,
      Set<Ip> allInterfaceIps,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology) {
    if (bgpPeerConfig instanceof BgpPassivePeerConfig) {
      return ConfiguredSessionStatus.DYNAMIC_LISTEN;
    } else if (bgpPeerConfig instanceof BgpActivePeerConfig) {
      BgpActivePeerConfig activePeerConfig = (BgpActivePeerConfig) bgpPeerConfig;
      ConfiguredSessionStatus brokenStatus = getLocallyBrokenStatus(activePeerConfig, sessionType);
      if (brokenStatus != null) {
        return brokenStatus;
      }
    }
    /*
     * Nothing blatantly broken so far on the local side, keep checking.
     * Also at this point we know this is not a Dynamic bgp neighbor
     */
    Ip localIp = bgpPeerConfig.getLocalIp();

    BgpActivePeerConfig p2pBgpPeerConfig = (BgpActivePeerConfig) bgpPeerConfig;
    Ip remoteIp = p2pBgpPeerConfig.getPeerAddress();

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

  static boolean node2RegexMatchesIp(
      Ip ip, Map<Ip, Set<String>> ipOwners, Set<String> includeNodes2) {
    Set<String> owners = ipOwners.get(ip);
    if (owners == null) {
      throw new BatfishException("Expected at least one owner of ip: " + ip);
    }
    return !Sets.intersection(includeNodes2, owners).isEmpty();
  }

  public abstract List<Row> getRows(BgpSessionQuestion question);
}
