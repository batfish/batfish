package org.batfish.datamodel.pojo;

import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * The {@link CreateEnvironmentRequest CreateEnvironmentRequest} is an Object representation of the
 * request used to create an environment and update an existing environment for BatFish service.
 */
public class CreateEnvironmentRequest {

  private final String _name;
  private final List<Edge> _edgeBlacklist;
  private final List<NodeInterfacePair> _interfaceBlacklist;
  private final List<String> _nodeBlacklist;
  private final List<FileObject> _bgpTables;
  private final List<FileObject> _routingTables;
  private final String _externalBgpAnnouncements;

  public CreateEnvironmentRequest(
      String name,
      @Nullable List<Edge> edgeBlacklist,
      @Nullable List<NodeInterfacePair> interfaceBlacklist,
      @Nullable List<String> nodeBlacklist,
      @Nullable List<FileObject> bgpTables,
      @Nullable List<FileObject> routingTables,
      @Nullable String externalBgpAnnouncements) {
    this._name = name;
    this._edgeBlacklist = edgeBlacklist;
    this._interfaceBlacklist = interfaceBlacklist;
    this._nodeBlacklist = nodeBlacklist;
    this._bgpTables = bgpTables;
    this._routingTables = routingTables;
    this._externalBgpAnnouncements = externalBgpAnnouncements;
  }

  public String getName() {
    return _name;
  }

  public List<Edge> getEdgeBlacklist() {
    return _edgeBlacklist;
  }

  public List<NodeInterfacePair> getInterfaceBlacklist() {
    return _interfaceBlacklist;
  }

  public List<String> getNodeBlacklist() {
    return _nodeBlacklist;
  }

  public List<FileObject> getBgpTables() {
    return _bgpTables;
  }

  public List<FileObject> getRoutingTables() {
    return _routingTables;
  }

  public String getExternalBgpAnnouncements() {
    return _externalBgpAnnouncements;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(CreateEnvironmentRequest.class)
        .add("name", _name)
        .add("edgeBlacklist", _edgeBlacklist)
        .add("interfaceBlacklist", _interfaceBlacklist)
        .add("nodeBlacklist", _nodeBlacklist)
        .add("bgpTables", _bgpTables)
        .add("routingTables", _routingTables)
        .add("externalBgpAnnouncements", _externalBgpAnnouncements)
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
