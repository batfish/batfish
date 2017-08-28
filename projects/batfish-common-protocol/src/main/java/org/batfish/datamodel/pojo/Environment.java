package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * The {@link Environment Environment} is an Object representation of the environment for BatFish
 * service.
 *
 * <p>Each {@link Environment Environment} contains a name and all informations of the environment
 * {@link #_name}.
 */
public class Environment {
  private static final String PROP_NAME = "name";
  private static final String PROP_EDGE_BLACKLIST = "edgeBlacklist";
  private static final String PROP_INTERFACE_BLACKLIST = "interfaceBlacklist";
  private static final String PROP_NODE_BLACKLIST = "nodeBlacklist";
  private static final String PROP_ENVIRONMENT_BGP_TABLES = "bgpTables";
  private static final String PROP_ENVIRONMENT_ROUTING_TABLES = "routingTables";
  private static final String PROP_EXTERNAL_BGP_ANNOUNCEMENTS = "externalBgpAnnouncements";

  private final String _name;
  private final List<Edge> _edgeBlacklist;
  private final List<NodeInterfacePair> _interfaceBlacklist;
  private final List<String> _nodeBlacklist;
  private final Map<String, String> _bgpTables;
  private final Map<String, String> _routingTables;
  private final String _externalBgpAnnouncements;

  @JsonCreator
  public Environment(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_EDGE_BLACKLIST) @Nullable List<Edge> edgeBlacklist,
      @JsonProperty(PROP_INTERFACE_BLACKLIST) @Nullable List<NodeInterfacePair> interfaceBlacklist,
      @JsonProperty(PROP_NODE_BLACKLIST) @Nullable List<String> nodeBlacklist,
      @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES) @Nullable Map<String, String> bgpTables,
      @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES) @Nullable Map<String, String> routingTables,
      @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS) @Nullable String externalBgpAnnouncements) {
    this._name = name;
    this._edgeBlacklist = edgeBlacklist;
    this._interfaceBlacklist = interfaceBlacklist;
    this._nodeBlacklist = nodeBlacklist;
    this._bgpTables = bgpTables;
    this._routingTables = routingTables;
    this._externalBgpAnnouncements = externalBgpAnnouncements;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_EDGE_BLACKLIST)
  public List<Edge> getEdgeBlacklist() {
    return _edgeBlacklist;
  }

  @JsonProperty(PROP_INTERFACE_BLACKLIST)
  public List<NodeInterfacePair> getInterfaceBlacklist() {
    return _interfaceBlacklist;
  }

  @JsonProperty(PROP_NODE_BLACKLIST)
  public List<String> getNodeBlacklist() {
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
        .add(PROP_NAME, _name)
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
    return Objects.equals(_name, other._name)
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
        _name,
        _edgeBlacklist,
        _interfaceBlacklist,
        _nodeBlacklist,
        _bgpTables,
        _routingTables,
        _externalBgpAnnouncements);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String _name;
    private List<Edge> _edgeBlacklist;
    private List<NodeInterfacePair> _interfaceBlacklist;
    private List<String> _nodeBlacklist;
    private Map<String, String> _bgpTables;
    private Map<String, String> _routingTables;
    private String _externalBgpAnnouncements;

    private Builder() {}

    public Environment build() {
      return new Environment(
          _name,
          _edgeBlacklist,
          _interfaceBlacklist,
          _nodeBlacklist,
          _bgpTables,
          _routingTables,
          _externalBgpAnnouncements);
    }

    public Builder setName(String name) {
      this._name = name;
      return this;
    }

    public Builder setEdgeBlacklist(List<Edge> edgeBlacklist) {
      _edgeBlacklist = edgeBlacklist;
      return this;
    }

    public Builder setInterfaceBlacklist(List<NodeInterfacePair> interfaceBlacklist) {
      _interfaceBlacklist = interfaceBlacklist;
      return this;
    }

    public Builder setNodeBlacklist(List<String> nodeBlacklist) {
      _nodeBlacklist = nodeBlacklist;
      return this;
    }

    public Builder setBgpTables(Map<String, String> bgpTables) {
      _bgpTables = bgpTables;
      return this;
    }

    public Builder setRoutingTables(Map<String, String> routingTables) {
      _routingTables = routingTables;
      return this;
    }

    public Builder setExternalBgpAnnouncements(String externalBgpAnnouncements) {
      _externalBgpAnnouncements = externalBgpAnnouncements;
      return this;
    }
  }
}
