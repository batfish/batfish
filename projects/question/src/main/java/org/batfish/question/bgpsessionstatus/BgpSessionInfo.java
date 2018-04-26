package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class BgpSessionInfo implements Comparable<BgpSessionInfo> {

  public enum SessionType {
    IBGP,
    EBGP_SINGLEHOP,
    EBGP_MULTIHOP
  }

  public enum SessionStatus {
    // ordered by how we evaluate status
    PASSIVE,
    MISSING_LOCAL_IP,
    UNKNOWN_LOCAL_IP,
    UNKNOWN_REMOTE_IP,
    HALF_OPEN,
    MULTIPLE_REMOTES,
    UNIQUE_MATCH,
  }

  private static final String PROP_DYNAMIC_NEIGHBORS = "dynamicNeighbors";
  private static final String PROP_LOCAL_IP = "localIp";
  private static final String PROP_NODE_NAME = "nodeName";
  private static final String PROP_ON_LOOPBACK = "onLoopback";
  private static final String PROP_REMOTE_PREFIX = "remotePrefix";
  private static final String PROP_REMOTE_NODE = "remoteNode";
  private static final String PROP_SESSION_TYPE = "sessionType";
  private static final String PROP_STATIC_STATUS = "status";
  private static final String PROP_VRF_NAME = "vrfName";

  @JsonProperty(PROP_DYNAMIC_NEIGHBORS)
  Integer _dynamicNeighbors;

  @JsonProperty(PROP_NODE_NAME)
  private String _nodeName;

  @JsonProperty(PROP_VRF_NAME)
  private String _vrfName;

  @JsonProperty(PROP_REMOTE_PREFIX)
  private Prefix _remotePrefix;

  @JsonProperty(PROP_LOCAL_IP)
  Ip _localIp;

  @JsonProperty(PROP_ON_LOOPBACK)
  Boolean _onLoopback;

  @JsonProperty(PROP_REMOTE_NODE)
  String _remoteNode;

  @JsonProperty(PROP_SESSION_TYPE)
  SessionType _sessionType;

  @JsonProperty(PROP_STATIC_STATUS)
  SessionStatus _staticStatus;

  @JsonCreator
  public BgpSessionInfo(
      @JsonProperty(PROP_NODE_NAME) String nodeName,
      @JsonProperty(PROP_VRF_NAME) String vrfName,
      @JsonProperty(PROP_REMOTE_PREFIX) Prefix remotePrefix,
      @JsonProperty(PROP_LOCAL_IP) Ip localIp,
      @JsonProperty(PROP_ON_LOOPBACK) Boolean onLoopback,
      @JsonProperty(PROP_REMOTE_NODE) String remoteNode,
      @JsonProperty(PROP_STATIC_STATUS) SessionStatus staticStatus,
      @JsonProperty(PROP_DYNAMIC_NEIGHBORS) Integer dynamicNeighbors,
      @JsonProperty(PROP_SESSION_TYPE) SessionType sessionType) {
    _nodeName = nodeName;
    _vrfName = vrfName;
    _remotePrefix = remotePrefix;
    _localIp = localIp;
    _onLoopback = onLoopback;
    _remoteNode = remoteNode;
    _staticStatus = staticStatus;
    _dynamicNeighbors = dynamicNeighbors;
    _sessionType = sessionType;
  }

  BgpSessionInfo(String hostname, String vrfName, Prefix remotePrefix) {
    this(hostname, vrfName, remotePrefix, null, null, null, null, null, null);
  }

  @JsonProperty(PROP_NODE_NAME)
  public String getNodeName() {
    return _nodeName;
  }

  @JsonProperty(PROP_VRF_NAME)
  public String getVrfName() {
    return _vrfName;
  }

  @JsonProperty(PROP_REMOTE_PREFIX)
  public Prefix getRemotePrefix() {
    return _remotePrefix;
  }

  @Override
  public int compareTo(@Nonnull BgpSessionInfo o) {
    return Comparator.comparing(BgpSessionInfo::getNodeName)
        .thenComparing(BgpSessionInfo::getVrfName)
        .thenComparing(BgpSessionInfo::getRemotePrefix)
        .compare(this, o);
  }

  @Override
  public String toString() {
    return String.format(
        "%s vrf=%s remote=%s type=%s loopback=%s staticStatus=%s "
            + "dynamicNeighbors=%s localIp=%s remoteNode=%s",
        _nodeName,
        _vrfName,
        _remotePrefix,
        _sessionType,
        _onLoopback,
        _staticStatus,
        _dynamicNeighbors,
        _localIp,
        _remoteNode);
  }
}
