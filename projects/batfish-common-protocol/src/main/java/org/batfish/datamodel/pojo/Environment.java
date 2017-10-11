package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * The {@link Environment Environment} is an Object representation of the environment for BatFish
 * service.
 *
 * <p>Each {@link Environment Environment} contains a name and all informations of the environment
 * {@link #_envName}.
 */
public class Environment {
  private static final String PROP_ENV_NAME = "envName";
  private static final String PROP_TESTRIG_NAME = "testrigName";
  private static final String PROP_EDGE_BLACKLIST = "edgeBlacklist";
  private static final String PROP_INTERFACE_BLACKLIST = "interfaceBlacklist";
  private static final String PROP_NODE_BLACKLIST = "nodeBlacklist";
  private static final String PROP_ENVIRONMENT_BGP_TABLES = "bgpTables";
  private static final String PROP_ENVIRONMENT_ROUTING_TABLES = "routingTables";
  private static final String PROP_EXTERNAL_BGP_ANNOUNCEMENTS = "externalBgpAnnouncements";

  private final String _envName;
  private final String _testrigName;
  private final Set<Edge> _edgeBlacklist;
  private final Set<NodeInterfacePair> _interfaceBlacklist;
  private final Set<String> _nodeBlacklist;
  private final Map<String, String> _bgpTables;
  private final Map<String, String> _routingTables;
  private final String _externalBgpAnnouncements;

  @JsonCreator
  public Environment(
      @JsonProperty(PROP_ENV_NAME) String envName,
      @JsonProperty(PROP_TESTRIG_NAME) String testrigName,
      @JsonProperty(PROP_EDGE_BLACKLIST) @Nullable Set<Edge> edgeBlacklist,
      @JsonProperty(PROP_INTERFACE_BLACKLIST) @Nullable Set<NodeInterfacePair> interfaceBlacklist,
      @JsonProperty(PROP_NODE_BLACKLIST) @Nullable Set<String> nodeBlacklist,
      @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES) @Nullable Map<String, String> bgpTables,
      @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES) @Nullable Map<String, String> routingTables,
      @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS) @Nullable String externalBgpAnnouncements) {
    this._envName = envName;
    this._testrigName = testrigName;
    this._edgeBlacklist = edgeBlacklist;
    this._interfaceBlacklist = interfaceBlacklist;
    this._nodeBlacklist = nodeBlacklist;
    this._bgpTables = bgpTables;
    this._routingTables = routingTables;
    this._externalBgpAnnouncements = externalBgpAnnouncements;
  }

  @JsonProperty(PROP_ENV_NAME)
  public String getEnvName() {
    return _envName;
  }

  @JsonProperty(PROP_TESTRIG_NAME)
  public String getTestrigName() {
    return _testrigName;
  }

  @JsonProperty(PROP_EDGE_BLACKLIST)
  public Set<Edge> getEdgeBlacklist() {
    return _edgeBlacklist;
  }

  @JsonProperty(PROP_INTERFACE_BLACKLIST)
  public Set<NodeInterfacePair> getInterfaceBlacklist() {
    return _interfaceBlacklist;
  }

  @JsonProperty(PROP_NODE_BLACKLIST)
  public Set<String> getNodeBlacklist() {
    return _nodeBlacklist;
  }

  @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES)
  public Map<String, String> getBgpTables() {
    return _bgpTables;
  }

  @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES)
  public Map<String, String> getRoutingTables() {
    return _routingTables;
  }

  @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS)
  public String getExternalBgpAnnouncements() {
    return _externalBgpAnnouncements;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Environment.class)
        .add(PROP_ENV_NAME, _envName)
        .add(PROP_TESTRIG_NAME, _testrigName)
        .add(PROP_EDGE_BLACKLIST, _edgeBlacklist)
        .add(PROP_INTERFACE_BLACKLIST, _interfaceBlacklist)
        .add(PROP_NODE_BLACKLIST, _nodeBlacklist)
        .add(PROP_ENVIRONMENT_BGP_TABLES, _bgpTables)
        .add(PROP_ENVIRONMENT_ROUTING_TABLES, _routingTables)
        .add(PROP_EXTERNAL_BGP_ANNOUNCEMENTS, _externalBgpAnnouncements)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Environment)) {
      return false;
    }
    Environment other = (Environment) o;
    return Objects.equals(_envName, other._envName)
        && Objects.equals(_edgeBlacklist, other._edgeBlacklist)
        && Objects.equals(_interfaceBlacklist, other._interfaceBlacklist)
        && Objects.equals(_nodeBlacklist, other._nodeBlacklist)
        && Objects.equals(_bgpTables, other._bgpTables)
        && Objects.equals(_routingTables, other._routingTables)
        && Objects.equals(_externalBgpAnnouncements, other._externalBgpAnnouncements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _envName,
        _edgeBlacklist,
        _interfaceBlacklist,
        _nodeBlacklist,
        _bgpTables,
        _routingTables,
        _externalBgpAnnouncements);
  }
}
