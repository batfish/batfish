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

  private SessionStatus _configuredStatus;

  private Integer _establishedNeighbors;

  private String _nodeName;

  private Ip _localIp;

  private Boolean _onLoopback;

  private String _remoteNode;

  private Prefix _remotePrefix;

  private SessionType _sessionType;

  private String _vrfName;

  @JsonCreator
  public BgpSessionInfo(
      @JsonProperty(PROP_NODE_NAME) String nodeName,
      @JsonProperty(PROP_VRF_NAME) String vrfName,
      @JsonProperty(PROP_REMOTE_PREFIX) Prefix remotePrefix,
      @JsonProperty(PROP_LOCAL_IP) Ip localIp,
      @JsonProperty(PROP_ON_LOOPBACK) Boolean onLoopback,
      @JsonProperty(PROP_REMOTE_NODE) String remoteNode,
      @JsonProperty(PROP_CONFIGURED_STATUS) SessionStatus staticStatus,
      @JsonProperty(PROP_ESTABLISHED_NEIGHBORS) Integer dynamicNeighbors,
      @JsonProperty(PROP_SESSION_TYPE) SessionType sessionType) {
    _nodeName = nodeName;
    _vrfName = vrfName;
    _remotePrefix = remotePrefix;
    _localIp = localIp;
    _onLoopback = onLoopback;
    _remoteNode = remoteNode;
    _configuredStatus = staticStatus;
    _establishedNeighbors = dynamicNeighbors;
    _sessionType = sessionType;
  }

  @JsonProperty(PROP_CONFIGURED_STATUS)
  public SessionStatus getConfiguredStatus() {
    return _configuredStatus;
  }

  @JsonProperty(PROP_ESTABLISHED_NEIGHBORS)
  public Integer getEstablishedNeighbors() {
    return _establishedNeighbors;
  }

  @JsonProperty(PROP_LOCAL_IP)
  public Ip getLocalIp() {
    return _localIp;
  }

  @JsonProperty(PROP_ON_LOOPBACK)
  public Boolean getOnLoopback() {
    return _onLoopback;
  }

  @JsonProperty(PROP_NODE_NAME)
  public String getNodeName() {
    return _nodeName;
  }

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

    public BgpSessionInfoBuilder(String _nodeName, String _vfrName, Prefix _remotePrefix) {
      this._nodeName = _nodeName;
      this._vrfName = _vfrName;
      this._remotePrefix = _remotePrefix;
    }

    public BgpSessionInfoBuilder with_configuredStatus(SessionStatus _configuredStatus) {
      this._configuredStatus = _configuredStatus;
      return this;
    }

    public BgpSessionInfoBuilder with_establishedNeighbors(Integer _establishedNeighbors) {
      this._establishedNeighbors = _establishedNeighbors;
      return this;
    }

    public BgpSessionInfoBuilder with_nodeName(String _nodeName) {
      this._nodeName = _nodeName;
      return this;
    }

    public BgpSessionInfoBuilder with_localIp(Ip _localIp) {
      this._localIp = _localIp;
      return this;
    }

    public BgpSessionInfoBuilder with_onLoopback(Boolean _onLoopback) {
      this._onLoopback = _onLoopback;
      return this;
    }

    public BgpSessionInfoBuilder with_remoteNode(String _remoteNode) {
      this._remoteNode = _remoteNode;
      return this;
    }

    public BgpSessionInfoBuilder with_remotePrefix(Prefix _remotePrefix) {
      this._remotePrefix = _remotePrefix;
      return this;
    }

    public BgpSessionInfoBuilder with_sessionType(SessionType _sessionType) {
      this._sessionType = _sessionType;
      return this;
    }

    public BgpSessionInfoBuilder with_vrfName(String _vrfName) {
      this._vrfName = _vrfName;
      return this;
    }

    public BgpSessionInfo build() {
      BgpSessionInfo bgpSessionInfo =
          new BgpSessionInfo(null, null, null, null, null, null, null, null, null);
      bgpSessionInfo._vrfName = this._vrfName;
      bgpSessionInfo._remoteNode = this._remoteNode;
      bgpSessionInfo._sessionType = this._sessionType;
      bgpSessionInfo._onLoopback = this._onLoopback;
      bgpSessionInfo._configuredStatus = this._configuredStatus;
      bgpSessionInfo._nodeName = this._nodeName;
      bgpSessionInfo._establishedNeighbors = this._establishedNeighbors;
      bgpSessionInfo._localIp = this._localIp;
      bgpSessionInfo._remotePrefix = this._remotePrefix;
      return bgpSessionInfo;
    }
  }
}
