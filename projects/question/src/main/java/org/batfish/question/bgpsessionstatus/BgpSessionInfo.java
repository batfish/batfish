package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    DYNAMIC_LISTEN,
    NO_LOCAL_IP,
    INVALID_LOCAL_IP,
    UNKNOWN_REMOTE,
    HALF_OPEN,
    MULTIPLE_REMOTES,
    UNIQUE_MATCH,
  }

  private static final String PROP_CONFIGURED_STATUS = "configuredStatus";
  private static final String PROP_ESTABLISHED_NEIGHBORS = "establishedNeighbors";
  private static final String PROP_LOCAL_IP = "localIp";
  private static final String PROP_NODE_NAME = "nodeName";
  private static final String PROP_ON_LOOPBACK = "onLoopback";
  private static final String PROP_REMOTE_PREFIX = "remotePrefix";
  private static final String PROP_REMOTE_NODE = "remoteNode";
  private static final String PROP_SESSION_TYPE = "sessionType";
  private static final String PROP_VRF_NAME = "vrfName";

  private final SessionStatus _configuredStatus;

  private final Integer _establishedNeighbors;

  @Nonnull private final String _nodeName;

  private final Ip _localIp;

  private final Boolean _onLoopback;

  private final String _remoteNode;

  @Nonnull private final Prefix _remotePrefix;

  @Nonnull private final SessionType _sessionType;

  @Nonnull private final String _vrfName;

  @JsonCreator
  public BgpSessionInfo(
      @JsonProperty(PROP_CONFIGURED_STATUS) SessionStatus configuredStatus,
      @JsonProperty(PROP_ESTABLISHED_NEIGHBORS) Integer dynamicNeighbors,
      @Nonnull @JsonProperty(PROP_NODE_NAME) String nodeName,
      @JsonProperty(PROP_LOCAL_IP) Ip localIp,
      @JsonProperty(PROP_ON_LOOPBACK) Boolean onLoopback,
      @JsonProperty(PROP_REMOTE_NODE) String remoteNode,
      @Nonnull @JsonProperty(PROP_REMOTE_PREFIX) Prefix remotePrefix,
      @Nonnull @JsonProperty(PROP_SESSION_TYPE) SessionType sessionType,
      @Nonnull @JsonProperty(PROP_VRF_NAME) String vrfName) {
    _nodeName = nodeName;
    _vrfName = vrfName;
    _remotePrefix = remotePrefix;
    _localIp = localIp;
    _onLoopback = onLoopback;
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
  @JsonProperty(PROP_LOCAL_IP)
  public Ip getLocalIp() {
    return _localIp;
  }

  @Nullable
  @JsonProperty(PROP_ON_LOOPBACK)
  public Boolean getOnLoopback() {
    return _onLoopback;
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
        && Objects.equals(_onLoopback, other._onLoopback)
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
        _onLoopback,
        _configuredStatus,
        _establishedNeighbors,
        _localIp,
        _remoteNode);
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
        _configuredStatus,
        _establishedNeighbors,
        _localIp,
        _remoteNode);
  }

  public static final class BgpSessionInfoBuilder {
    private SessionStatus _configuredStatus;
    private Integer _establishedNeighbors;
    private String _nodeName;
    private Ip _localIp;
    private Boolean _onLoopback;
    private String _remoteNode;
    private Prefix _remotePrefix;
    private SessionType _sessionType;
    private String _vrfName;

    public BgpSessionInfoBuilder(
        String nodeName, String vfrName, Prefix remotePrefix, SessionType sessionType) {
      _nodeName = nodeName;
      _vrfName = vfrName;
      _remotePrefix = remotePrefix;
      _sessionType = sessionType;
    }

    public BgpSessionInfoBuilder withConfiguredStatus(SessionStatus configuredStatus) {
      _configuredStatus = configuredStatus;
      return this;
    }

    public BgpSessionInfoBuilder withEstablishedNeighbors(Integer establishedNeighbors) {
      _establishedNeighbors = establishedNeighbors;
      return this;
    }

    public BgpSessionInfoBuilder withLocalIp(Ip localIp) {
      _localIp = localIp;
      return this;
    }

    public BgpSessionInfoBuilder withOnLoopback(Boolean onLoopback) {
      _onLoopback = onLoopback;
      return this;
    }

    public BgpSessionInfoBuilder withRemoteNode(String remoteNode) {
      _remoteNode = remoteNode;
      return this;
    }

    public BgpSessionInfo build() {
      return new BgpSessionInfo(
          _configuredStatus,
          _establishedNeighbors,
          _nodeName,
          _localIp,
          _onLoopback,
          _remoteNode,
          _remotePrefix,
          _sessionType,
          _vrfName);
    }
  }
}
