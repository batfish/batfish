package org.batfish.question.bgpsessionstatus;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Captures the configuration state of a BGP session. */
public class BgpSessionInfo implements Comparable<BgpSessionInfo> {

  public enum SessionStatus {
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

  private static final String PROP_CONFIGURED_STATUS = "configuredStatus";
  private static final String PROP_ESTABLISHED_NEIGHBORS = "establishedNeighbors";
  private static final String PROP_LOCAL_IP = "localIp";
  private static final String PROP_LOCAL_INTERFACE = "localInterface";
  private static final String PROP_NODE_NAME = "nodeName";
  private static final String PROP_REMOTE_PREFIX = "remotePrefix";
  private static final String PROP_REMOTE_NODE = "remoteNode";
  private static final String PROP_SESSION_TYPE = "sessionType";
  private static final String PROP_VRF_NAME = "vrfName";

  private final SessionStatus _configuredStatus;

  private final Integer _establishedNeighbors;

  @Nonnull private final String _nodeName;

  private final NodeInterfacePair _localInterface;

  private final Ip _localIp;

  private final String _remoteNode;

  @Nonnull private final Prefix _remotePrefix;

  @Nonnull private final SessionType _sessionType;

  @Nonnull private final String _vrfName;

  @JsonCreator
  private static BgpSessionInfo createBgpSessionInfo(
      @JsonProperty(PROP_CONFIGURED_STATUS) SessionStatus configuredStatus,
      @JsonProperty(PROP_ESTABLISHED_NEIGHBORS) Integer dynamicNeighbors,
      @JsonProperty(PROP_NODE_NAME) String nodeName,
      @JsonProperty(PROP_LOCAL_INTERFACE) NodeInterfacePair localInterface,
      @JsonProperty(PROP_LOCAL_IP) Ip localIp,
      @JsonProperty(PROP_REMOTE_NODE) String remoteNode,
      @JsonProperty(PROP_REMOTE_PREFIX) Prefix remotePrefix,
      @JsonProperty(PROP_SESSION_TYPE) SessionType sessionType,
      @JsonProperty(PROP_VRF_NAME) String vrfName) {
    return new BgpSessionInfo(
        configuredStatus,
        dynamicNeighbors,
        requireNonNull(nodeName, PROP_NODE_NAME + "must not be null"),
        localInterface,
        localIp,
        remoteNode,
        requireNonNull(remotePrefix, PROP_REMOTE_PREFIX + " must not be null"),
        requireNonNull(sessionType, PROP_SESSION_TYPE + " must not be null"),
        requireNonNull(vrfName, PROP_VRF_NAME + " must not be null"));
  }

  private BgpSessionInfo(
      SessionStatus configuredStatus,
      Integer dynamicNeighbors,
      @Nonnull String nodeName,
      NodeInterfacePair localInterface,
      Ip localIp,
      String remoteNode,
      @Nonnull Prefix remotePrefix,
      @Nonnull SessionType sessionType,
      @Nonnull String vrfName) {
    _nodeName = nodeName;
    _vrfName = vrfName;
    _remotePrefix = remotePrefix;
    _localInterface = localInterface;
    _localIp = localIp;
    _remoteNode = remoteNode;
    _configuredStatus = configuredStatus;
    _establishedNeighbors = dynamicNeighbors;
    _sessionType = sessionType;
  }

  @Nullable
  @JsonProperty(PROP_CONFIGURED_STATUS)
  public SessionStatus getConfiguredStatus() {
    return _configuredStatus;
  }

  @Nullable
  @JsonProperty(PROP_ESTABLISHED_NEIGHBORS)
  public Integer getEstablishedNeighbors() {
    return _establishedNeighbors;
  }

  @Nullable
  @JsonProperty(PROP_LOCAL_INTERFACE)
  public NodeInterfacePair getLocalInterface() {
    return _localInterface;
  }

  @Nullable
  @JsonProperty(PROP_LOCAL_IP)
  public Ip getLocalIp() {
    return _localIp;
  }

  @JsonProperty(PROP_NODE_NAME)
  public String getNodeName() {
    return _nodeName;
  }

  @Nullable
  @JsonProperty(PROP_REMOTE_NODE)
  public String getRemoteNode() {
    return _remoteNode;
  }

  @JsonProperty(PROP_REMOTE_PREFIX)
  public Prefix getRemotePrefix() {
    return _remotePrefix;
  }

  @JsonProperty(PROP_SESSION_TYPE)
  public SessionType getSessionType() {
    return _sessionType;
  }

  @JsonProperty(PROP_VRF_NAME)
  public String getVrfName() {
    return _vrfName;
  }

  @Override
  public int compareTo(@Nonnull BgpSessionInfo o) {
    return Comparator.comparing(BgpSessionInfo::getNodeName)
        .thenComparing(BgpSessionInfo::getVrfName)
        .thenComparing(BgpSessionInfo::getRemotePrefix)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BgpSessionInfo)) {
      return false;
    }
    BgpSessionInfo other = (BgpSessionInfo) o;
    return Objects.equals(_nodeName, other._nodeName)
        && Objects.equals(_vrfName, other._vrfName)
        && Objects.equals(_remotePrefix, other._remotePrefix)
        && Objects.equals(_sessionType, other._sessionType)
        && Objects.equals(_localInterface, other._localInterface)
        && Objects.equals(_configuredStatus, other._configuredStatus)
        && Objects.equals(_establishedNeighbors, other._establishedNeighbors)
        && Objects.equals(_localIp, other._localIp)
        && Objects.equals(_remoteNode, other._remoteNode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _nodeName,
        _vrfName,
        _remotePrefix,
        _sessionType,
        _localInterface,
        _configuredStatus,
        _establishedNeighbors,
        _localIp,
        _remoteNode);
  }

  @Override
  public String toString() {
    return String.format(
        "%s vrf=%s remote=%s type=%s localInterface=%s staticStatus=%s "
            + "dynamicNeighbors=%s localIp=%s remoteNode=%s",
        _nodeName,
        _vrfName,
        _remotePrefix,
        _sessionType,
        _localInterface,
        _configuredStatus,
        _establishedNeighbors,
        _localIp,
        _remoteNode);
  }

  public static Builder builder(
      @Nonnull String nodeName,
      @Nonnull String vrfName,
      @Nonnull Prefix remotePrefix,
      @Nonnull SessionType sessionType) {
    return new Builder(nodeName, vrfName, remotePrefix, sessionType);
  }

  public static final class Builder {
    private SessionStatus _configuredStatus;
    private Integer _establishedNeighbors;
    private final String _nodeName;
    private NodeInterfacePair _localInterface;
    private Ip _localIp;
    private String _remoteNode;
    private final Prefix _remotePrefix;
    private final SessionType _sessionType;
    private final String _vrfName;

    private Builder(String nodeName, String vfrName, Prefix remotePrefix, SessionType sessionType) {
      _nodeName = nodeName;
      _vrfName = vfrName;
      _remotePrefix = remotePrefix;
      _sessionType = sessionType;
    }

    public Builder withConfiguredStatus(SessionStatus configuredStatus) {
      _configuredStatus = configuredStatus;
      return this;
    }

    public Builder withEstablishedNeighbors(Integer establishedNeighbors) {
      _establishedNeighbors = establishedNeighbors;
      return this;
    }

    public Builder withLocalIp(Ip localIp) {
      _localIp = localIp;
      return this;
    }

    public Builder withLocalInterface(NodeInterfacePair localInterface) {
      _localInterface = localInterface;
      return this;
    }

    public Builder withRemoteNode(String remoteNode) {
      _remoteNode = remoteNode;
      return this;
    }

    public BgpSessionInfo build() {
      return new BgpSessionInfo(
          _configuredStatus,
          _establishedNeighbors,
          _nodeName,
          _localInterface,
          _localIp,
          _remoteNode,
          _remotePrefix,
          _sessionType,
          _vrfName);
    }
  }
}
