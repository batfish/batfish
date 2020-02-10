package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * The {@link Environment} contains the information about the snapshot that is external to
 * configuration -- device up/down states, BGP announcements, etc.
 */
public class Environment {
  private static final String PROP_TESTRIG_NAME = "testrigName";
  private static final String PROP_EDGE_BLACKLIST = "edgeBlacklist";
  private static final String PROP_INTERFACE_BLACKLIST = "interfaceBlacklist";
  private static final String PROP_NODE_BLACKLIST = "nodeBlacklist";
  private static final String PROP_ENVIRONMENT_BGP_TABLES = "bgpTables";
  private static final String PROP_ENVIRONMENT_ROUTING_TABLES = "routingTables";
  private static final String PROP_EXTERNAL_BGP_ANNOUNCEMENTS = "externalBgpAnnouncements";

  private final String _testrigName;
  private final SortedSet<Edge> _edgeBlacklist;
  private final SortedSet<NodeInterfacePair> _interfaceBlacklist;
  private final SortedSet<String> _nodeBlacklist;
  private final SortedMap<String, String> _bgpTables;
  private final SortedMap<String, String> _routingTables;
  private final SortedSet<BgpAdvertisement> _externalBgpAnnouncements;

  @JsonCreator
  public Environment(
      @JsonProperty(PROP_TESTRIG_NAME) String testrigName,
      @JsonProperty(PROP_EDGE_BLACKLIST) @Nullable SortedSet<Edge> edgeBlacklist,
      @JsonProperty(PROP_INTERFACE_BLACKLIST) @Nullable
          SortedSet<NodeInterfacePair> interfaceBlacklist,
      @JsonProperty(PROP_NODE_BLACKLIST) @Nullable SortedSet<String> nodeBlacklist,
      @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES) @Nullable SortedMap<String, String> bgpTables,
      @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES) @Nullable
          SortedMap<String, String> routingTables,
      @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS) @Nullable
          SortedSet<BgpAdvertisement> externalBgpAnnouncements) {
    _testrigName = testrigName;
    _edgeBlacklist = edgeBlacklist;
    _interfaceBlacklist = interfaceBlacklist;
    _nodeBlacklist = nodeBlacklist;
    _bgpTables = bgpTables;
    _routingTables = routingTables;
    _externalBgpAnnouncements = externalBgpAnnouncements;
  }

  @JsonProperty(PROP_TESTRIG_NAME)
  public String getTestrigName() {
    return _testrigName;
  }

  @JsonProperty(PROP_EDGE_BLACKLIST)
  public SortedSet<Edge> getEdgeBlacklist() {
    return _edgeBlacklist;
  }

  @JsonProperty(PROP_INTERFACE_BLACKLIST)
  public SortedSet<NodeInterfacePair> getInterfaceBlacklist() {
    return _interfaceBlacklist;
  }

  @JsonProperty(PROP_NODE_BLACKLIST)
  public SortedSet<String> getNodeBlacklist() {
    return _nodeBlacklist;
  }

  @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES)
  public SortedMap<String, String> getBgpTables() {
    return _bgpTables;
  }

  @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES)
  public SortedMap<String, String> getRoutingTables() {
    return _routingTables;
  }

  @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS)
  public SortedSet<BgpAdvertisement> getExternalBgpAnnouncements() {
    return _externalBgpAnnouncements;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Environment.class)
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
    return Objects.equals(_edgeBlacklist, other._edgeBlacklist)
        && Objects.equals(_interfaceBlacklist, other._interfaceBlacklist)
        && Objects.equals(_nodeBlacklist, other._nodeBlacklist)
        && Objects.equals(_bgpTables, other._bgpTables)
        && Objects.equals(_routingTables, other._routingTables)
        && Objects.equals(_externalBgpAnnouncements, other._externalBgpAnnouncements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _edgeBlacklist,
        _interfaceBlacklist,
        _nodeBlacklist,
        _bgpTables,
        _routingTables,
        _externalBgpAnnouncements);
  }
}
