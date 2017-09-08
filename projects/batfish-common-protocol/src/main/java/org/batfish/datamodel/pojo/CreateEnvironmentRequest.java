package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * The {@link CreateEnvironmentRequest CreateEnvironmentRequest} is an Object representation of the
 * request used to create an environment and update an existing environment for BatFish service.
 */
public class CreateEnvironmentRequest {

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
  private final List<FileObject> _bgpTables;
  private final List<FileObject> _routingTables;
  private final String _externalBgpAnnouncements;

  @JsonCreator
  public CreateEnvironmentRequest(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_EDGE_BLACKLIST) @NotNull List<Edge> edgeBlacklist,
      @JsonProperty(PROP_INTERFACE_BLACKLIST) @NotNull List<NodeInterfacePair> interfaceBlacklist,
      @JsonProperty(PROP_NODE_BLACKLIST) @NotNull List<String> nodeBlacklist,
      @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES) @Nullable List<FileObject> bgpTables,
      @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES) @Nullable List<FileObject> routingTables,
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
  public List<FileObject> getBgpTables() {
    return _bgpTables;
  }

  @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES)
  public List<FileObject> getRoutingTables() {
    return _routingTables;
  }

  @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS)
  public String getExternalBgpAnnouncements() {
    return _externalBgpAnnouncements;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(CreateEnvironmentRequest.class)
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
    if (!(o instanceof CreateEnvironmentRequest)) {
      return false;
    }
    CreateEnvironmentRequest other = (CreateEnvironmentRequest) o;
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
}
