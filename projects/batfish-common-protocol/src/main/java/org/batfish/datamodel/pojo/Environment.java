package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * The {@link Environment Environment} is an Object representation of the environment for BatFish
 * service.
 *
 * <p>Each {@link Environment Environment} contains a name and a summary of the environment {@link
 * #_name}.
 */
public class Environment {
  private static final String PROP_NAME = "name";
  private static final String PROP_EDGE_BLACKLIST_COUNT = "edgeBlacklistCount";
  private static final String PROP_INTERFACE_BLACKLIST_COUNT = "interfaceBlacklistCount";
  private static final String PROP_NODE_BLACKLIST_COUNT = "nodeBlacklistCount";
  private static final String PROP_ENVIRONMENT_BGP_TABLES_COUNT = "bgpTablesCount";
  private static final String PROP_ENVIRONMENT_ROUTING_TABLES_COUNT = "routingTablesCount";
  private static final String PROP_EXTERNAL_BGP_ANNOUNCEMENTS = "externalBgpAnnouncements";
  private static final String PROP_CREATED_AT = "createdAt";

  private final String _name;
  private final Date _createdAt;
  private final int _edgeBlacklistCount;
  private final int _interfaceBlacklistCount;
  private final int _nodeBlacklistCount;
  private final int _bgpTablesCount;
  private final int _routingTablesCount;
  private final String _externalBgpAnnouncements;

  @JsonCreator
  public Environment(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_CREATED_AT) Date createdAt,
      @JsonProperty(PROP_EDGE_BLACKLIST_COUNT) int edgeBlacklistCount,
      @JsonProperty(PROP_INTERFACE_BLACKLIST_COUNT) int interfaceBlacklistCount,
      @JsonProperty(PROP_NODE_BLACKLIST_COUNT) int nodeBlacklistCount,
      @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES_COUNT) int bgpTablesCount,
      @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES_COUNT) int routingTablesCount,
      @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS) @Nullable String externalBgpAnnouncements) {
    this._name = name;
    this._createdAt = createdAt;
    this._edgeBlacklistCount = edgeBlacklistCount;
    this._interfaceBlacklistCount = interfaceBlacklistCount;
    this._nodeBlacklistCount = nodeBlacklistCount;
    this._bgpTablesCount = bgpTablesCount;
    this._routingTablesCount = routingTablesCount;
    this._externalBgpAnnouncements = externalBgpAnnouncements;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_CREATED_AT)
  public Date getCreatedAt() {
    return _createdAt;
  }

  @JsonProperty(PROP_EDGE_BLACKLIST_COUNT)
  public int getEdgeBlacklistCount() {
    return _edgeBlacklistCount;
  }

  @JsonProperty(PROP_INTERFACE_BLACKLIST_COUNT)
  public int getInterfaceBlacklistCount() {
    return _interfaceBlacklistCount;
  }

  @JsonProperty(PROP_NODE_BLACKLIST_COUNT)
  public int getNodeBlacklistCount() {
    return _nodeBlacklistCount;
  }

  @JsonProperty(PROP_ENVIRONMENT_BGP_TABLES_COUNT)
  public int getBgpTablesCount() {
    return _bgpTablesCount;
  }

  @JsonProperty(PROP_ENVIRONMENT_ROUTING_TABLES_COUNT)
  public int getRoutingTablesCount() {
    return _routingTablesCount;
  }

  @JsonProperty(PROP_EXTERNAL_BGP_ANNOUNCEMENTS)
  public String getExternalBgpAnnouncements() {
    return _externalBgpAnnouncements;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Environment.class)
        .add(PROP_NAME, _name)
        .add(PROP_CREATED_AT, _createdAt)
        .add(PROP_EDGE_BLACKLIST_COUNT, _edgeBlacklistCount)
        .add(PROP_INTERFACE_BLACKLIST_COUNT, _interfaceBlacklistCount)
        .add(PROP_NODE_BLACKLIST_COUNT, _nodeBlacklistCount)
        .add(PROP_ENVIRONMENT_BGP_TABLES_COUNT, _bgpTablesCount)
        .add(PROP_ENVIRONMENT_ROUTING_TABLES_COUNT, _routingTablesCount)
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
        && Objects.equals(_createdAt, other._createdAt)
        && Objects.equals(_edgeBlacklistCount, other._edgeBlacklistCount)
        && Objects.equals(_interfaceBlacklistCount, other._interfaceBlacklistCount)
        && Objects.equals(_nodeBlacklistCount, other._nodeBlacklistCount)
        && Objects.equals(_bgpTablesCount, other._bgpTablesCount)
        && Objects.equals(_routingTablesCount, other._routingTablesCount)
        && Objects.equals(_externalBgpAnnouncements, other._externalBgpAnnouncements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _name,
        _createdAt,
        _edgeBlacklistCount,
        _interfaceBlacklistCount,
        _nodeBlacklistCount,
        _bgpTablesCount,
        _routingTablesCount,
        _externalBgpAnnouncements);
  }
}
